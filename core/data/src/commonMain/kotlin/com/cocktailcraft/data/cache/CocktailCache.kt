package com.cocktailcraft.data.cache

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
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
    val cocktail: Cocktail,
    val cachedAtMs: Long = 0L
)

/** A cached cocktail plus when its row was last written (epoch millis). */
internal data class CachedCocktailRow(
    val cocktail: Cocktail,
    val cachedAtMs: Long
)

/**
 * The local cocktail catalog, backed by SQLDelight: the cached_cocktail
 * table (one row per cocktail) and category_page (which cocktails each
 * fetched category page lists, in order, and when it was fetched). It is the
 * single source of truth for every cocktail the app shows: repositories write
 * network results here and the UI reads them back through the observe*
 * flows, which emit again after every write.
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
 * user-facing "clear cache" path.
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
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val offlineModePolicy: OfflineModePolicy? = null
) {
    constructor(
        database: CocktailCraftDatabase,
        settings: Settings,
        json: Json,
        appConfig: AppConfig,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        offlineModePolicy: OfflineModePolicy? = null
    ) : this(lazyOf(database), settings, json, appConfig, ioDispatcher, offlineModePolicy)

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

    private val cocktailSerializer = Cocktail.serializer()

    // Guards every database operation. Besides serialising writes, it keeps
    // the access stamps below strictly increasing.
    private val dbMutex = Mutex()

    // Last stamp handed out for cached_cocktail.last_accessed /
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

    private fun encode(cocktail: Cocktail): String = json.encodeToString(cocktailSerializer, cocktail)

    private fun decodeOrNull(payload: String): Cocktail? =
        try {
            json.decodeFromString(cocktailSerializer, payload)
        } catch (e: Exception) {
            Logger.w(e) { "Dropping unreadable cached cocktail: ${e.message}" }
            null
        }

    /**
     * Runs [block] against the database on [ioDispatcher], one operation at a
     * time. Caching is not critical: a database failure is logged and
     * [fallback] returned instead of crashing the caller.
     */
    private suspend fun <T> withDatabase(
        fallback: T,
        block: suspend CocktailCraftDatabase.() -> T
    ): T {
        ensureLoaded()
        return withContext(ioDispatcher) {
            try {
                dbMutex.withLock { database.value.block() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e(e) { "Cocktail cache database operation failed: ${e.message}" }
                fallback
            }
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
            dbMutex.withLock {
                val db = database.value
                lastStamp = maxOf(
                    db.cachedCocktailQueries.maxLastAccessed().executeAsOne().value_ ?: 0L,
                    db.recentlyViewedCocktailQueries.maxViewedAt().executeAsOne().value_ ?: 0L
                )
                importLegacySettingsCache(db)
                if (applyExpiry) {
                    db.cachedCocktailQueries.deleteCachedBefore(expiryCutoff())
                }
                // The cap is configuration; honour a lowered value on startup.
                db.cachedCocktailQueries.trimToCapacity(appConfig.maxOfflineCocktails.toLong())
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
                db.cachedCocktailQueries.insertIfAbsent(
                    id = entry.cocktail.id,
                    category = normalizedCategory(entry.cocktail),
                    payload = encode(entry.cocktail),
                    cached_at = entry.cachedAtMs,
                    last_accessed = nextStamp()
                )
            }
            recent.forEach { cocktail ->
                db.recentlyViewedCocktailQueries.insertIfAbsent(
                    id = cocktail.id,
                    payload = encode(cocktail),
                    viewed_at = nextStamp()
                )
            }
            db.cachedCocktailQueries.trimToCapacity(appConfig.maxOfflineCocktails.toLong())
            db.recentlyViewedCocktailQueries.trimToCapacity(MAX_RECENTLY_VIEWED.toLong())
        }

        settings.remove(ALL_COCKTAILS_KEY)
        settings.remove(RECENTLY_VIEWED_KEY)
        Logger.i {
            "Imported ${entries.size} cached and ${recent.size} recently viewed cocktails from Settings"
        }
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
                json.decodeFromString(ListSerializer(cocktailSerializer), raw)
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
            json.decodeFromString(ListSerializer(cocktailSerializer), raw)
        } catch (e: Exception) {
            Logger.w(e) { "Discarding unreadable legacy recently viewed list" }
            emptyList()
        }
    }

    /**
     * (id, payload) rows for [ids], one `IN` query per [MAX_IDS_PER_QUERY]
     * ids. Call inside a transaction so the chunks see one snapshot.
     */
    private fun CocktailCraftDatabase.selectPayloads(ids: Collection<String>): List<Pair<String, String>> =
        ids.distinct().chunked(MAX_IDS_PER_QUERY).flatMap { chunk ->
            cachedCocktailQueries.selectByIds(chunk) { id, payload -> id to payload }.executeAsList()
        }

    /**
     * Upserts [cocktails] in order and applies the LRU cap. A list-endpoint
     * placeholder never replaces a cached full record (it would put "Tap to
     * view ingredients" back on cards); the full record is kept and refreshed.
     * Likewise a record without a category (the ingredient and alcoholic
     * filters omit it) keeps the category already stored for that id, so it
     * doesn't drop out of the category page it was cached for. Existing
     * records are read in one batch, not one SELECT per row. Call inside a
     * transaction.
     */
    private fun CocktailCraftDatabase.upsertCocktails(cocktails: List<Cocktail>) {
        val now = nowMs()
        val lookupIds = cocktails
            .filter { !it.hasFullDetails || normalizedCategory(it) == null }
            .map { it.id }
        val existing: Map<String, Cocktail> = selectPayloads(lookupIds)
            .mapNotNull { (id, payload) -> decodeOrNull(payload)?.let { id to it } }
            .toMap()
        cocktails.forEach { incoming ->
            val cocktail = merge(incoming, existing[incoming.id])
            cachedCocktailQueries.upsert(
                id = cocktail.id,
                category = normalizedCategory(cocktail),
                payload = encode(cocktail),
                cached_at = now,
                last_accessed = nextStamp()
            )
        }
        cachedCocktailQueries.trimToCapacity(appConfig.maxOfflineCocktails.toLong())
    }

    /** What to store for [incoming] given the row already cached for its id. */
    private fun merge(incoming: Cocktail, existing: Cocktail?): Cocktail {
        val base = if (incoming.hasFullDetails) incoming else existing?.takeIf { it.hasFullDetails } ?: incoming
        val category = normalizedCategory(base)
            ?: normalizedCategory(incoming)
            ?: existing?.let(::normalizedCategory)
        return if (category == base.category) base else base.copy(category = category)
    }

    /**
     * Decodes (id, payload) rows, deleting any row whose payload can no
     * longer be decoded so it isn't retried on every read.
     */
    private fun decodeRows(
        rows: List<Pair<String, String>>,
        deleteRow: (String) -> Unit
    ): List<Cocktail> = rows.mapNotNull { (id, payload) ->
        decodeOrNull(payload) ?: run {
            deleteRow(id)
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
     * A failing query is logged and ends the stream, so collectors keep their
     * last state instead of crashing the scope they collect in.
     */
    private fun <T : Any, R> observeQuery(
        query: CocktailCraftDatabase.() -> Query<T>,
        transform: (List<T>) -> R
    ): Flow<R> =
        flow {
            ensureLoaded()
            emitAll(database.value.query().asFlow().mapToList(ioDispatcher))
        }
            .map(transform)
            .flowOn(ioDispatcher)
            .catch { e -> Logger.e(e) { "Observing the cocktail cache failed: ${e.message}" } }

    private fun decodeReadOnly(rows: List<Pair<String, String>>): List<Cocktail> =
        rows.mapNotNull { (_, payload) -> decodeOrNull(payload) }

    /** The cached cocktail [id] (null when not cached), re-emitted after every write. */
    fun observeCocktail(id: String): Flow<Cocktail?> =
        observeQuery({ cachedCocktailQueries.selectById(id) }) { rows ->
            rows.firstOrNull()?.let { decodeOrNull(it.payload) }
        }

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
                        query = { categoryPageQueries.selectPage(category) { id, payload -> id to payload } },
                        transform = ::decodeReadOnly
                    )
                } else {
                    observeCategoryColumn(category, includeUncategorized)
                }
            }
            .distinctUntilChanged()

    private fun observeCategoryColumn(category: String, includeUncategorized: Boolean): Flow<List<Cocktail>> =
        observeQuery({
            if (includeUncategorized) {
                cachedCocktailQueries.selectByCategoryOrUncategorized(category) { id, payload -> id to payload }
            } else {
                cachedCocktailQueries.selectByCategory(category) { id, payload -> id to payload }
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
                upsertCocktails(cocktails)
                categoryPageQueries.deleteCategory(category)
                cocktails.distinctBy { it.id }.forEachIndexed { position, cocktail ->
                    categoryPageQueries.insert(
                        category = category,
                        cocktail_id = cocktail.id,
                        position = position.toLong(),
                        fetched_at = fetchedAtMs
                    )
                }
            }
        }
    }

    /**
     * When [category]'s page was fetched, or null if it never was (or a
     * cocktail it lists has since been evicted, so it no longer holds the
     * whole page).
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
                decodeOrNull(row.payload)?.let { CachedCocktailRow(it, row.cached_at) }
            }
        }

    /**
     * Cache a cocktail for offline access.
     */
    suspend fun cacheCocktail(cocktail: Cocktail) {
        cacheCocktails(listOf(cocktail))
    }

    /**
     * Cache a batch of cocktails for offline access in one transaction.
     * Insertion order matches sequential [cacheCocktail] calls, so LRU
     * eviction behaves identically.
     */
    suspend fun cacheCocktails(cocktails: List<Cocktail>) {
        if (cocktails.isEmpty()) return
        withDatabase(Unit) {
            transaction { upsertCocktails(cocktails) }
        }
    }

    /**
     * Get a cached cocktail by ID. Entries older than
     * [AppConfig.cacheExpirationMs] are evicted (online only) and not returned.
     * A hit counts as an access for LRU eviction.
     */
    suspend fun getCachedCocktail(id: String): Cocktail? {
        val applyExpiry = shouldApplyExpiry()
        return withDatabase(null) {
            val row = cachedCocktailQueries.selectById(id).executeAsOneOrNull()
                ?: return@withDatabase null
            if (applyExpiry && row.cached_at < expiryCutoff()) {
                cachedCocktailQueries.deleteById(id)
                return@withDatabase null
            }
            val cocktail = decodeOrNull(row.payload)
            if (cocktail == null) {
                cachedCocktailQueries.deleteById(id)
            } else {
                cachedCocktailQueries.touch(lastAccessed = nextStamp(), id = id)
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
            val rows = cachedCocktailQueries.selectAll { id, payload -> id to payload }.executeAsList()
            decodeRows(rows) { cachedCocktailQueries.deleteById(it) }
        }
        Logger.d { "CocktailCache.getAllCachedCocktails() returning ${live.size} items" }
        return live
    }

    /**
     * Add a cocktail to the recently viewed list (capped at
     * [MAX_RECENTLY_VIEWED]); it is also cached for offline access if it
     * isn't already.
     */
    suspend fun addToRecentlyViewed(cocktail: Cocktail) {
        withDatabase(Unit) {
            transaction {
                recentlyViewedCocktailQueries.upsert(
                    id = cocktail.id,
                    payload = encode(cocktail),
                    viewed_at = nextStamp()
                )
                recentlyViewedCocktailQueries.trimToCapacity(MAX_RECENTLY_VIEWED.toLong())

                if (cachedCocktailQueries.exists(cocktail.id).executeAsOne() > 0L) {
                    cachedCocktailQueries.touch(lastAccessed = nextStamp(), id = cocktail.id)
                } else {
                    upsertCocktails(listOf(cocktail))
                }
            }
        }
    }

    /**
     * Get the list of recently viewed cocktails, most recent first.
     */
    suspend fun getRecentlyViewedCocktails(): List<Cocktail> {
        val recent = withDatabase(emptyList()) {
            val rows = recentlyViewedCocktailQueries
                .selectMostRecentFirst { id, payload -> id to payload }
                .executeAsList()
            decodeRows(rows) { recentlyViewedCocktailQueries.deleteById(it) }
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
