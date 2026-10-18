package com.cocktailcraft.data.cache

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.local.CocktailCodec
import com.cocktailcraft.data.local.CocktailDecodeMemo
import com.cocktailcraft.data.local.CocktailEntity
import com.cocktailcraft.data.local.CurrentOwner
import com.cocktailcraft.data.local.FixedOwner
import com.cocktailcraft.data.local.LegacyCocktailSerializer
import com.cocktailcraft.data.local.OwnerIds
import com.cocktailcraft.data.local.claimUnassignedUserData
import com.cocktailcraft.data.local.toDomain
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * Entry format of the legacy Settings-backed cache: a cocktail plus the
 * epoch-millis timestamp of when it was cached. Only read during the one-time
 * import into the database. [cachedAtMs] has a serialization default so JSON
 * written before the field existed still deserializes.
 */
@Serializable
internal data class CachedCocktailEntry(
    @Serializable(with = LegacyCocktailSerializer::class)
    val cocktail: CocktailEntity,
    val cachedAtMs: Long = 0L
)

/** A cached cocktail plus when its row was last written (epoch millis). */
internal data class CachedCocktailRow(
    val cocktail: Cocktail,
    val cachedAtMs: Long
)

/** A stored cocktail row before decoding: its id, payload and full_details flag. */
internal class StoredCocktail(val id: String, val payload: String, val hasFullDetails: Boolean)

/** Row mapper for the queries selecting (id, payload, full_details). */
internal val storedCocktail: (String, String, Long) -> StoredCocktail =
    { id, payload, fullDetails -> StoredCocktail(id, payload, fullDetails != 0L) }

/** What [CocktailCache] merges an incoming record into. */
private class StoredState(val category: String?, val payload: String, val hasFullDetails: Boolean)

/**
 * The local cocktail catalog, backed by SQLDelight: the cached_cocktail
 * table (one row per cocktail) and category_page (which cocktails each
 * fetched category page lists, in order, and when it was fetched). It is the
 * single source of truth for every cocktail the app shows: repositories write
 * network results here and the UI reads them back through the observe*
 * flows, which emit again after every write. Favorites, the cart and the
 * recently viewed list refer to its rows by id rather than keeping copies
 * ([storeAndReference]), so a detail refresh shows up everywhere.
 *
 * Each cocktail is one row, so caching or viewing a cocktail is a single-row
 * upsert instead of rewriting the whole cache.
 *
 * Entries expire after [AppConfig.cacheExpirationMs], but only while the app is
 * effectively online — see [shouldApplyExpiry].
 *
 * Eviction matrix (SH-11): this is the only cocktail store; there is no
 * in-memory copy with its own expiry. Eviction is LRU (capped at
 * [AppConfig.maxOfflineCocktails]) plus the TTL expiry above — there is no
 * user-facing "clear cache" path. Neither ever drops a pinned row: one a
 * stored category page lists, or one in a favorites list, cart or recently
 * viewed list (see CachedCocktail.sq). So a browsed category stays whole
 * offline, and caching search results can't push it out.
 *
 * The cocktails are shared by everyone using the device; the recently viewed
 * list belongs to [currentOwner] (see UserDataOwnership.kt).
 *
 * The database is opened lazily on first access, on [ioDispatcher] — a Koin
 * `single` must not block whichever thread first resolves it. That first
 * access also imports (once) any cache written by older app versions into
 * Settings; see [importLegacySettingsCache].
 */
internal class CocktailCache(
    private val database: Lazy<CocktailCraftDatabase>,
    private val settings: Settings,
    private val json: Json,
    private val appConfig: AppConfig,
    private val currentOwner: CurrentOwner,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val offlineModePolicy: OfflineModePolicy? = null
) {
    constructor(
        database: CocktailCraftDatabase,
        settings: Settings,
        json: Json,
        appConfig: AppConfig,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        offlineModePolicy: OfflineModePolicy? = null,
        currentOwner: CurrentOwner = FixedOwner()
    ) : this(lazyOf(database), settings, json, appConfig, currentOwner, ioDispatcher, offlineModePolicy)

    companion object {
        // Settings keys of the legacy JSON-blob cache. internal (not private)
        // so tests can seed fixtures under the real keys.
        internal const val ALL_COCKTAILS_KEY = "all_cached_cocktails"
        internal const val RECENTLY_VIEWED_KEY = "recently_viewed_cocktails"
        internal const val MAX_RECENTLY_VIEWED = 20

        // Ids bound per `IN` query: below the 999-parameter limit of the
        // framework SQLite on older devices.
        private const val MAX_IDS_PER_QUERY = 500
    }

    private val codec = CocktailCodec(json)

    // Guards every database operation. Besides serialising writes, it keeps
    // the access stamps below strictly increasing.
    private val dbMutex = Mutex()

    // Last stamp handed out for cocktail_access.last_accessed /
    // recently_viewed_cocktail.viewed_at. See [nextStamp].
    private var lastStamp = 0L

    private val loadMutex = Mutex()
    private var loaded = false

    private fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * Wall-clock millis, bumped when needed so consecutive calls are strictly
     * increasing. Rows written in one batch (same millisecond) therefore keep
     * their insertion order for LRU eviction and the recently viewed list.
     */
    private fun nextStamp(): Long {
        lastStamp = maxOf(nowMs(), lastStamp + 1)
        return lastStamp
    }

    /**
     * Expiry only applies while the app is online. This cache backs offline
     * browsing: evicting stale entries when there is no connectivity would
     * empty the offline experience with no way to refetch, so stale entries
     * keep being served until we are back online. The offline decision itself
     * is owned by [OfflineModePolicy] (AR-7) — the cache only stores data; it
     * does not re-derive policy from raw NetworkMonitor reads.
     */
    private suspend fun shouldApplyExpiry(): Boolean =
        offlineModePolicy?.let { !it.isOffline() } ?: true

    private fun expiryCutoff(now: Long = nowMs()): Long = now - appConfig.cacheExpirationMs

    private fun normalizedCategory(cocktail: Cocktail): String? =
        cocktail.category?.takeIf { it.isNotBlank() }

    private fun decodeOrNull(row: StoredCocktail): Cocktail? = codec.decodeOrNull(row.payload, row.hasFullDetails)

    /**
     * Runs [block] against the database on [ioDispatcher], one operation at a
     * time. Caching is not critical: a database failure is logged and
     * [fallback] returned instead of crashing the caller.
     */
    private suspend fun <T> withDatabase(
        fallback: T,
        block: suspend CocktailCraftDatabase.() -> T
    ): T =
        try {
            withDatabaseOrThrow(block)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Cocktail cache database operation failed: ${e.message}" }
            fallback
        }

    /** Like [withDatabase], for callers that report a failure themselves. */
    private suspend fun <T> withDatabaseOrThrow(block: suspend CocktailCraftDatabase.() -> T): T {
        ensureLoaded()
        return withContext(ioDispatcher) {
            dbMutex.withLock { database.value.block() }
        }
    }

    private suspend fun ensureLoaded() {
        if (loaded) return
        loadMutex.withLock {
            if (!loaded) {
                load()
                loaded = true
            }
        }
    }

    private suspend fun load(): Unit = withContext(ioDispatcher) {
        try {
            val applyExpiry = shouldApplyExpiry()
            val ownerId = currentOwner.ownerId()
            dbMutex.withLock {
                val db = database.value
                lastStamp = maxOf(
                    db.cachedCocktailQueries.maxLastAccessed().executeAsOne().value_ ?: 0L,
                    db.recentlyViewedCocktailQueries.maxViewedAt().executeAsOne().value_ ?: 0L
                )
                importLegacySettingsCache(db)
                // Recently viewed entries from that import or an upgrade
                // belong to whoever is signed in (LocalDatabase does the same)
                db.claimUnassignedUserData(ownerId)
                if (applyExpiry) {
                    db.cachedCocktailQueries.deleteCachedBefore(expiryCutoff())
                }
                // The cap is configuration; honour a lowered value on startup.
                db.trim()
                Logger.d {
                    "Offline cocktail cache ready with " +
                        "${db.cachedCocktailQueries.count().executeAsOne()} cocktails"
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Log but don't crash - caching is not critical. Legacy Settings
            // keys are only removed after a successful import, so the import
            // is retried on the next launch.
            Logger.e(e) { "Failed to open the offline cocktail cache: ${e.message}" }
        }
    }

    /**
     * One-time import of the cache older app versions kept as JSON blobs in
     * [Settings] ([ALL_COCKTAILS_KEY] and [RECENTLY_VIEWED_KEY]).
     *
     * Rows are written with INSERT OR IGNORE in a single transaction, and the
     * keys are only removed once that transaction commits. If the app dies
     * part-way the transaction rolls back and the next launch retries; if it
     * dies between the commit and the key removal, re-importing is a no-op
     * for rows that already exist, so nothing is duplicated or overwritten.
     */
    private fun importLegacySettingsCache(db: CocktailCraftDatabase) {
        val cocktailsJson = settings.getStringOrNull(ALL_COCKTAILS_KEY)
        val recentJson = settings.getStringOrNull(RECENTLY_VIEWED_KEY)
        if (cocktailsJson == null && recentJson == null) return

        val entries = cocktailsJson?.let(::decodeLegacyEntries).orEmpty()
        val recent = recentJson?.let(::decodeLegacyRecentlyViewed).orEmpty()

        db.transaction {
            // Legacy lists are oldest-first, so ascending stamps keep both
            // the LRU order and the recently viewed order.
            entries.forEach { entry ->
                db.insertLegacyCocktail(entry.cocktail.toDomain(), entry.cachedAtMs)
            }
            val now = nowMs()
            recent.forEach { cocktail ->
                // The cocktail itself, if the legacy cache had lost it
                db.insertLegacyCocktail(cocktail, cachedAtMs = now)
                db.recentlyViewedCocktailQueries.insertIfAbsent(
                    owner_id = OwnerIds.UNASSIGNED,
                    cocktail_id = cocktail.id,
                    viewed_at = nextStamp()
                )
            }
            db.recentlyViewedCocktailQueries.trimToCapacity(
                ownerId = OwnerIds.UNASSIGNED,
                capacity = MAX_RECENTLY_VIEWED.toLong()
            )
            db.trim()
        }

        settings.remove(ALL_COCKTAILS_KEY)
        settings.remove(RECENTLY_VIEWED_KEY)
        Logger.i {
            "Imported ${entries.size} cached and ${recent.size} recently viewed cocktails from Settings"
        }
    }

    /** Stores a legacy record unless the database has it already (full details win). */
    private fun CocktailCraftDatabase.insertLegacyCocktail(cocktail: Cocktail, cachedAtMs: Long) {
        val payload = codec.encode(cocktail)
        val fullDetails = if (cocktail.hasFullDetails) 1L else 0L
        cachedCocktailQueries.insertIfAbsent(
            id = cocktail.id,
            category = normalizedCategory(cocktail),
            payload = payload,
            full_details = fullDetails,
            cached_at = cachedAtMs
        )
        if (cocktail.hasFullDetails) cachedCocktailQueries.upgradeToFull(payload = payload, id = cocktail.id)
        cachedCocktailQueries.touchIfAbsent(id = cocktail.id, lastAccessed = nextStamp())
    }

    /**
     * Decodes the legacy [ALL_COCKTAILS_KEY] blob: a list of
     * [CachedCocktailEntry], or the even older pre-TTL plain list of
     * cocktails, which is stamped "now" so an upgrade doesn't wipe it.
     * Undecodable data is discarded (it could never be read anyway).
     */
    private fun decodeLegacyEntries(raw: String): List<CachedCocktailEntry> {
        if (raw.isBlank()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(CachedCocktailEntry.serializer()), raw)
        } catch (e: Exception) {
            try {
                val now = nowMs()
                json.decodeFromString(ListSerializer(LegacyCocktailSerializer), raw)
                    .map { CachedCocktailEntry(it, now) }
            } catch (e: Exception) {
                Logger.w(e) { "Discarding unreadable legacy cocktail cache" }
                emptyList()
            }
        }
    }

    private fun decodeLegacyRecentlyViewed(raw: String): List<Cocktail> {
        if (raw.isBlank()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(LegacyCocktailSerializer), raw).map { it.toDomain() }
        } catch (e: Exception) {
            Logger.w(e) { "Discarding unreadable legacy recently viewed list" }
            emptyList()
        }
    }

    /**
     * The stored state of [ids], one `IN` query per [MAX_IDS_PER_QUERY] ids.
     * Call inside a transaction so the chunks see one snapshot.
     */
    private fun CocktailCraftDatabase.selectStored(ids: Collection<String>): Map<String, StoredState> =
        ids.distinct().chunked(MAX_IDS_PER_QUERY).flatMap { chunk ->
            cachedCocktailQueries.selectStored(chunk) { id, category, payload, fullDetails ->
                id to StoredState(category, payload, fullDetails != 0L)
            }.executeAsList()
        }.toMap()

    /**
     * Stores [cocktails] in order and marks each as just used (LRU). Call
     * inside a transaction. Existing records are read in one batch, not one
     * SELECT per row.
     *
     * [fetched] says where the records come from. Fetched from the API, they
     * are the newest data and are written (cached_at = now), except a
     * partial record that would replace a full one or change nothing. Handed
     * back by the app (adding a favorite, a view), they are copies of stored
     * data or older, so they are written only where nothing is stored or to
     * upgrade a partial record to a full one, never passing for a fresh fetch.
     *
     * A partial record never replaces a cached full one (cards would lose
     * the details again). A record without a category (the ingredient and
     * alcoholic filters omit it) keeps the category already stored for that
     * id, so it doesn't drop out of the category page it was cached for.
     */
    private fun CocktailCraftDatabase.storeCocktails(cocktails: List<Cocktail>, fetched: Boolean) {
        if (cocktails.isEmpty()) return
        val now = nowMs()
        val stored = selectStored(cocktails.map { it.id })
        cocktails.forEach { incoming ->
            val existing = stored[incoming.id]
            val category = normalizedCategory(incoming) ?: existing?.category
            val cocktail = if (category == incoming.category) incoming else incoming.copy(category = category)
            val payload = payloadToWrite(cocktail, existing, fetched)
            if (payload != null) {
                cachedCocktailQueries.upsert(
                    id = cocktail.id,
                    category = category,
                    payload = payload,
                    full_details = if (cocktail.hasFullDetails) 1L else 0L,
                    cached_at = now
                )
            } else if (existing?.category == null && category != null) {
                cachedCocktailQueries.fillCategory(category = category, id = cocktail.id)
            }
            cachedCocktailQueries.touch(id = cocktail.id, lastAccessed = nextStamp())
        }
    }

    /** The payload [storeCocktails] writes for [cocktail], or null to keep [existing]. */
    private fun payloadToWrite(cocktail: Cocktail, existing: StoredState?, fetched: Boolean): String? = when {
        existing == null -> codec.encode(cocktail)
        cocktail.hasFullDetails && (fetched || !existing.hasFullDetails) -> codec.encode(cocktail)
        fetched && !existing.hasFullDetails -> codec.encode(cocktail).takeIf { it != existing.payload }
        else -> null
    }

    private fun CocktailCraftDatabase.trim() {
        cachedCocktailQueries.trimToCapacity(appConfig.maxOfflineCocktails.toLong())
    }

    /**
     * Decodes stored rows, deleting any row whose payload can no longer be
     * decoded so it isn't retried on every read.
     */
    private fun decodeRows(
        rows: List<StoredCocktail>,
        deleteRow: (String) -> Unit
    ): List<Cocktail> = rows.mapNotNull { row ->
        decodeOrNull(row) ?: run {
            deleteRow(row.id)
            null
        }
    }

    // --- Observation: the catalog read path ---

    /**
     * Emits the rows of [query], mapped by [transform], and again after every
     * write to the tables it reads. Querying and decoding run on [ioDispatcher].
     * Unlike the suspend reads these never delete or touch rows (a read that
     * writes would re-trigger itself), and expired rows are still served:
     * stale data stays on screen until a refresh replaces it.
     *
     * [transform] gets a [CocktailDecodeMemo] of its own for each collector,
     * so a re-run only decodes the rows that changed.
     *
     * A failing query is logged and ends the stream, so collectors keep their
     * last state instead of crashing the scope they collect in.
     */
    private fun <T : Any, R> observeQuery(
        query: CocktailCraftDatabase.() -> Query<T>,
        transform: CocktailDecodeMemo.(List<T>) -> R
    ): Flow<R> =
        flow {
            ensureLoaded()
            val memo = CocktailDecodeMemo(codec)
            emitAll(
                database.value.query().asFlow().mapToList(ioDispatcher)
                    .map { rows -> memo.emission { transform(rows) } }
            )
        }
            .flowOn(ioDispatcher)
            .catch { e -> Logger.e(e) { "Observing the cocktail cache failed: ${e.message}" } }

    private fun CocktailDecodeMemo.decodeReadOnly(rows: List<StoredCocktail>): List<Cocktail> =
        rows.mapNotNull { decode(it.id, it.payload, it.hasFullDetails) }

    /** The cached cocktail [id] (null when not cached), re-emitted after every write. */
    fun observeCocktail(id: String): Flow<Cocktail?> =
        observeQuery({ cachedCocktailQueries.selectById(id) }) { rows ->
            rows.firstOrNull()?.let { decode(id, it.payload, it.full_details != 0L) }
        }
            // A write to another cocktail re-runs the query too
            .distinctUntilChanged()

    /**
     * The stored page of [category], re-emitted after every write to the
     * cocktails or the page. Once the category has been fetched
     * ([cacheCategoryPage]) it lists the fetched cocktails in the API's
     * order, from category_page. Before that it lists the cocktails whose
     * category column matches, by name; with [includeUncategorized], rows
     * cached without a category (they predate category stamping) stand in
     * when none matches, so they aren't stranded offline.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeCategoryPage(
        category: String,
        includeUncategorized: Boolean = false
    ): Flow<List<Cocktail>> =
        observeQuery({ categoryPageQueries.hasPage(category) }) { rows -> rows.firstOrNull() == true }
            .distinctUntilChanged()
            .flatMapLatest { hasPage ->
                if (hasPage) {
                    observeQuery(
                        query = { categoryPageQueries.selectPage(category, storedCocktail) },
                        transform = { rows -> decodeReadOnly(rows) }
                    )
                } else {
                    observeCategoryColumn(category, includeUncategorized)
                }
            }
            .distinctUntilChanged()

    private fun observeCategoryColumn(category: String, includeUncategorized: Boolean): Flow<List<Cocktail>> =
        observeQuery({
            if (includeUncategorized) {
                cachedCocktailQueries.selectByCategoryOrUncategorized(category, storedCocktail)
            } else {
                cachedCocktailQueries.selectByCategory(category, storedCocktail)
            }
        }) { rows ->
            val decoded = decodeReadOnly(rows)
            decoded.filter { it.category == category }
                .ifEmpty { decoded }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }

    /**
     * Stores a freshly fetched category page in one transaction: the
     * cocktails (as [cacheCocktails] does) and, replacing the previous page,
     * their ids in order with [fetchedAtMs]. Observers never see the page
     * list ids whose rows aren't written yet.
     */
    suspend fun cacheCategoryPage(category: String, cocktails: List<Cocktail>, fetchedAtMs: Long) {
        withDatabase(Unit) {
            transaction {
                storeCocktails(cocktails, fetched = true)
                categoryPageQueries.deleteCategory(category)
                cocktails.distinctBy { it.id }.forEachIndexed { position, cocktail ->
                    categoryPageQueries.insert(
                        category = category,
                        cocktail_id = cocktail.id,
                        position = position.toLong(),
                        fetched_at = fetchedAtMs
                    )
                }
                trim()
            }
        }
    }

    /**
     * When [category]'s page was fetched, or null if it never was (or a
     * cocktail it lists is missing, so it no longer holds the whole page).
     */
    suspend fun categoryPageFetchedAt(category: String): Long? =
        withDatabase(null) {
            val status = categoryPageQueries.pageStatus(category).executeAsOne()
            status.fetched_at?.takeIf { status.listed > 0 && status.stored == status.listed }
        }

    /**
     * The cached row for [id] with its write time, without counting as an
     * access or applying expiry. For freshness checks.
     */
    suspend fun peekCachedCocktail(id: String): CachedCocktailRow? =
        withDatabase(null) {
            cachedCocktailQueries.selectById(id).executeAsOneOrNull()?.let { row ->
                codec.decodeOrNull(row.payload, row.full_details != 0L)?.let { CachedCocktailRow(it, row.cached_at) }
            }
        }

    /**
     * Cache a cocktail fetched from the API for offline access.
     */
    suspend fun cacheCocktail(cocktail: Cocktail) {
        cacheCocktails(listOf(cocktail))
    }

    /**
     * Cache a batch of cocktails fetched from the API in one transaction.
     * Insertion order matches sequential [cacheCocktail] calls, so LRU
     * eviction behaves identically.
     */
    suspend fun cacheCocktails(cocktails: List<Cocktail>) {
        if (cocktails.isEmpty()) return
        withDatabase(Unit) {
            transaction {
                storeCocktails(cocktails, fetched = true)
                trim()
            }
        }
    }

    /**
     * Stores [cocktails] (where nothing better is stored) and runs [reference]
     * — the favorites or cart write that refers to them — in the same
     * transaction, so a reference never points at a row that isn't there and
     * eviction never sees the rows unpinned. Failures propagate.
     */
    suspend fun <T> storeAndReference(
        cocktails: List<Cocktail>,
        reference: CocktailCraftDatabase.() -> T
    ): T = withDatabaseOrThrow {
        transactionWithResult {
            storeCocktails(cocktails, fetched = false)
            reference().also { trim() }
        }
    }

    /**
     * Get a cached cocktail by ID. Entries older than
     * [AppConfig.cacheExpirationMs] are evicted (online only, and only when
     * nothing pins them) and not returned. A hit counts as an access for LRU
     * eviction; recording it doesn't re-run queries observing the cocktails.
     */
    suspend fun getCachedCocktail(id: String): Cocktail? {
        val applyExpiry = shouldApplyExpiry()
        return withDatabase(null) {
            val row = cachedCocktailQueries.selectById(id).executeAsOneOrNull()
                ?: return@withDatabase null
            val cutoff = expiryCutoff()
            if (applyExpiry && row.cached_at < cutoff &&
                cachedCocktailQueries.deleteIfExpired(id = id, cutoff = cutoff).value > 0L
            ) {
                return@withDatabase null
            }
            val cocktail = codec.decodeOrNull(row.payload, row.full_details != 0L)
            if (cocktail == null) {
                cachedCocktailQueries.deleteById(id)
            } else {
                cachedCocktailQueries.touch(id = id, lastAccessed = nextStamp())
            }
            cocktail
        }
    }

    /**
     * Get all cached cocktails, dropping expired entries (online only).
     * Least recently used first.
     */
    suspend fun getAllCachedCocktails(): List<Cocktail> {
        val applyExpiry = shouldApplyExpiry()
        val live = withDatabase(emptyList()) {
            if (applyExpiry) cachedCocktailQueries.deleteCachedBefore(expiryCutoff())
            val rows = cachedCocktailQueries.selectAll(storedCocktail).executeAsList()
            decodeRows(rows) { cachedCocktailQueries.deleteById(it) }
        }
        Logger.d { "CocktailCache.getAllCachedCocktails() returning ${live.size} items" }
        return live
    }

    /**
     * Add a cocktail to the current owner's recently viewed list (capped at
     * [MAX_RECENTLY_VIEWED]); it is also cached for offline access if it
     * isn't already.
     */
    suspend fun addToRecentlyViewed(cocktail: Cocktail) {
        val ownerId = currentOwner.ownerId()
        withDatabase(Unit) {
            transaction {
                storeCocktails(listOf(cocktail), fetched = false)
                recentlyViewedCocktailQueries.upsert(
                    owner_id = ownerId,
                    cocktail_id = cocktail.id,
                    viewed_at = nextStamp()
                )
                recentlyViewedCocktailQueries.trimToCapacity(ownerId = ownerId, capacity = MAX_RECENTLY_VIEWED.toLong())
                trim()
            }
        }
    }

    /**
     * Get the current owner's recently viewed cocktails, most recent first.
     */
    suspend fun getRecentlyViewedCocktails(): List<Cocktail> {
        val ownerId = currentOwner.ownerId()
        val recent = withDatabase(emptyList()) {
            val rows = recentlyViewedCocktailQueries.selectMostRecentFirst(ownerId, storedCocktail).executeAsList()
            decodeRows(rows) { recentlyViewedCocktailQueries.deleteById(ownerId, it) }
        }
        Logger.d { "CocktailCache.getRecentlyViewedCocktails() returning ${recent.size} items" }
        return recent
    }

    /**
     * Check if a cocktail is cached (and not expired).
     */
    suspend fun isCocktailCached(id: String): Boolean {
        return getCachedCocktail(id) != null
    }

    /**
     * Get the number of cached cocktails.
     */
    suspend fun getCachedCocktailCount(): Int =
        withDatabase(0) { cachedCocktailQueries.count().executeAsOne().toInt() }
}
