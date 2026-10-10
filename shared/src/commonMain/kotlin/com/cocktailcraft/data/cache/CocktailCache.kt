package com.cocktailcraft.data.cache

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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

/**
 * Persistent offline cache of cocktails, backed by SQLDelight.
 *
 * Each cocktail is one row, so caching or viewing a cocktail is a single-row
 * upsert instead of rewriting the whole cache.
 *
 * Entries expire after [AppConfig.cacheExpirationMs], but only while the app is
 * effectively online — see [shouldApplyExpiry].
 *
 * Eviction matrix (SH-11): this is the PERSISTENT layer. See
 * [CocktailCacheManager] for the separate short-lived (5 min) in-memory
 * category cache and rate-limit state. Eviction is LRU (capped at
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

    /** Upserts [cocktails] in order and applies the LRU cap. Call inside a transaction. */
    private fun CocktailCraftDatabase.upsertCocktails(cocktails: List<Cocktail>) {
        val now = nowMs()
        cocktails.forEach { cocktail ->
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
     * Cached cocktails whose category is [category], dropping expired entries
     * (online only). With [includeUncategorized], entries cached without a
     * category (they predate category stamping) are included too.
     */
    suspend fun getCachedCocktailsByCategory(
        category: String,
        includeUncategorized: Boolean = false
    ): List<Cocktail> {
        val applyExpiry = shouldApplyExpiry()
        return withDatabase(emptyList()) {
            if (applyExpiry) cachedCocktailQueries.deleteCachedBefore(expiryCutoff())
            val query = if (includeUncategorized) {
                cachedCocktailQueries.selectByCategoryOrUncategorized(category) { id, payload -> id to payload }
            } else {
                cachedCocktailQueries.selectByCategory(category) { id, payload -> id to payload }
            }
            decodeRows(query.executeAsList()) { cachedCocktailQueries.deleteById(it) }
        }
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
