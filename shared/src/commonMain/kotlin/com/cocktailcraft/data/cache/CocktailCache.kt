package com.cocktailcraft.data.cache

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import co.touchlab.kermit.Logger
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple LRU cache implementation
 */
internal class SimpleLruCache<K, V>(private val maxSize: Int) {
    private val cache = mutableMapOf<K, V>()
    private val accessOrder = mutableListOf<K>()
    private val mutex = Mutex()
    
    suspend fun put(key: K, value: V) {
        mutex.withLock {
            // Remove key from access order if it exists
            accessOrder.remove(key)
            // Add key to end (most recently used)
            accessOrder.add(key)
            cache[key] = value
            
            // Evict least recently used if over capacity
            if (cache.size > maxSize) {
                val lruKey = accessOrder.removeAt(0)
                cache.remove(lruKey)
            }
        }
    }
    
    suspend fun get(key: K): V? {
        return mutex.withLock {
            cache[key]?.also {
                // Move to end (most recently used)
                accessOrder.remove(key)
                accessOrder.add(key)
            }
        }
    }
    
    suspend fun snapshot(): Map<K, V> {
        return mutex.withLock {
            cache.toMap()
        }
    }
    
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
            accessOrder.clear()
        }
    }
    
    suspend fun size(): Int {
        return mutex.withLock { cache.size }
    }

    suspend fun remove(key: K) {
        mutex.withLock {
            accessOrder.remove(key)
            cache.remove(key)
        }
    }
}

/**
 * Persisted cache entry: a cocktail plus the epoch-millis timestamp of when it
 * was cached. [cachedAtMs] has a serialization default so JSON written before
 * the field existed still deserializes.
 */
@Serializable
internal data class CachedCocktailEntry(
    val cocktail: Cocktail,
    val cachedAtMs: Long = 0L
)

/**
 * Manages caching of cocktail data for offline access using a simple LRU cache.
 *
 * Entries expire after [AppConfig.cacheExpirationMs], but only while the app is
 * effectively online — see [shouldApplyExpiry].
 */
internal class CocktailCache(
    private val settings: Settings,
    private val json: Json,
    private val appConfig: AppConfig,
    private val networkMonitor: NetworkMonitor? = null
) {
    companion object {
        private const val CACHE_PREFIX = "cocktail_cache_"
        private const val RECENTLY_VIEWED_KEY = "recently_viewed_cocktails"
        private const val ALL_COCKTAILS_KEY = "all_cached_cocktails"
        private const val CACHE_METADATA_PREFIX = "cache_metadata_"
        private const val MAX_RECENTLY_VIEWED = 20
    }

    // In-memory cache for cocktails using LRU strategy
    private val cocktailCache = SimpleLruCache<String, CachedCocktailEntry>(appConfig.maxOfflineCocktails)

    // In-memory cache for recently viewed cocktails
    private val recentlyViewedCache = SimpleLruCache<String, Cocktail>(MAX_RECENTLY_VIEWED)
    
    // Persisted entries are loaded lazily on first access — a Koin `single`
    // must not block whichever thread first resolves it.
    private val loadMutex = Mutex()
    private var loaded = false

    private suspend fun ensureLoaded() {
        if (loaded) return
        loadMutex.withLock {
            if (!loaded) {
                loadPersistedCocktails()
                loaded = true
            }
        }
    }

    private fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()

    private fun isExpired(entry: CachedCocktailEntry, now: Long = nowMs()): Boolean =
        now - entry.cachedAtMs > appConfig.cacheExpirationMs

    /**
     * Expiry only applies while the app is effectively online. This cache backs
     * offline browsing: evicting stale entries when there is no connectivity (or
     * the user forced offline mode) would empty the offline experience with no
     * way to refetch, so stale entries keep being served until we are back online.
     */
    private suspend fun shouldApplyExpiry(): Boolean {
        if (settings.getBoolean(appConfig.offlineModeEnabledKey, false)) return false
        return networkMonitor?.isOnline?.first() ?: true
    }

    private suspend fun loadPersistedCocktails() {
        Logger.d { "Loading persisted cocktails..." }
        try {
            // Load all cached cocktails from persistent storage
            val cachedJson = settings.getStringOrNull(ALL_COCKTAILS_KEY)
            if (!cachedJson.isNullOrBlank()) {
                var migratedFromLegacy = false
                val entries = try {
                    json.decodeFromString<List<CachedCocktailEntry>>(cachedJson)
                } catch (e: Exception) {
                    // Pre-TTL format: a plain list of cocktails without timestamps.
                    // Stamp them "now" so an app upgrade doesn't wipe the offline cache.
                    migratedFromLegacy = true
                    val now = nowMs()
                    json.decodeFromString<List<Cocktail>>(cachedJson).map { CachedCocktailEntry(it, now) }
                }
                val (fresh, expired) = if (shouldApplyExpiry()) {
                    val now = nowMs()
                    entries.partition { !isExpired(it, now) }
                } else {
                    entries to emptyList()
                }
                Logger.d { "Loaded ${fresh.size} cocktails from persistent storage (${expired.size} expired)" }
                fresh.forEach { entry ->
                    cocktailCache.put(entry.cocktail.id, entry)
                }
                if (expired.isNotEmpty() || migratedFromLegacy) {
                    // Rewrite storage so expired entries stay gone and legacy
                    // data is upgraded to the timestamped format.
                    if (fresh.isEmpty()) settings.remove(ALL_COCKTAILS_KEY) else persistCocktails()
                }
            } else {
                Logger.d { "No persisted cocktails found" }
            }

            // Load recently viewed cocktails
            val recentJson = settings.getStringOrNull(RECENTLY_VIEWED_KEY)
            if (!recentJson.isNullOrBlank()) {
                val recentCocktails = json.decodeFromString<List<Cocktail>>(recentJson)
                Logger.d { "Loaded ${recentCocktails.size} recently viewed cocktails" }
                recentCocktails.forEach { cocktail ->
                    recentlyViewedCache.put(cocktail.id, cocktail)
                }
            } else {
                Logger.d { "No recently viewed cocktails found" }
            }
        } catch (e: Exception) {
            // Log error but don't crash - caching is not critical
            Logger.e(e) { "Failed to load persisted cocktails: ${e.message}" }
        }
    }
    
    private suspend fun persistCocktails() {
        try {
            // Persist all cached cocktails
            val allEntries = cocktailCache.snapshot().values.toList()
            Logger.d { "Persisting ${allEntries.size} cocktails to storage" }
            if (allEntries.isNotEmpty()) {
                val jsonString = json.encodeToString(allEntries)
                settings.putString(ALL_COCKTAILS_KEY, jsonString)
                Logger.d { "Successfully persisted cocktails" }
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to persist cocktails: ${e.message}" }
        }
    }
    
    private suspend fun persistRecentlyViewed() {
        try {
            // Persist recently viewed cocktails
            val recentCocktails = recentlyViewedCache.snapshot().values.toList()
            if (recentCocktails.isNotEmpty()) {
                val jsonString = json.encodeToString(recentCocktails)
                settings.putString(RECENTLY_VIEWED_KEY, jsonString)
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to persist recently viewed: ${e.message}" }
        }
    }
    
    /**
     * Cache a cocktail for offline access.
     */
    suspend fun cacheCocktail(cocktail: Cocktail) {
        ensureLoaded()
        cocktailCache.put(cocktail.id, CachedCocktailEntry(cocktail, nowMs()))
        persistCocktails() // Persist to storage
    }

    /**
     * Get a cached cocktail by ID. Entries older than
     * [AppConfig.cacheExpirationMs] are evicted (online only) and not returned.
     */
    suspend fun getCachedCocktail(id: String): Cocktail? {
        ensureLoaded()
        val entry = cocktailCache.get(id) ?: return null
        if (isExpired(entry) && shouldApplyExpiry()) {
            cocktailCache.remove(id)
            return null
        }
        return entry.cocktail
    }

    /**
     * Get all cached cocktails, dropping expired entries (online only).
     */
    suspend fun getAllCachedCocktails(): List<Cocktail> {
        ensureLoaded()
        val entries = cocktailCache.snapshot().values.toList()
        val live = if (shouldApplyExpiry()) {
            val now = nowMs()
            val (fresh, expired) = entries.partition { !isExpired(it, now) }
            expired.forEach { cocktailCache.remove(it.cocktail.id) }
            fresh
        } else {
            entries
        }
        Logger.d { "CocktailCache.getAllCachedCocktails() returning ${live.size} items" }
        return live.map { it.cocktail }
    }

    /**
     * Add a cocktail to the recently viewed list.
     */
    suspend fun addToRecentlyViewed(cocktail: Cocktail) {
        ensureLoaded()
        recentlyViewedCache.put(cocktail.id, cocktail)
        // Also add to main cache if not already there
        if (cocktailCache.get(cocktail.id) == null) {
            cocktailCache.put(cocktail.id, CachedCocktailEntry(cocktail, nowMs()))
            persistCocktails()
        }
        persistRecentlyViewed() // Persist to storage
    }
    
    /**
     * Get the list of recently viewed cocktails.
     */
    suspend fun getRecentlyViewedCocktails(): List<Cocktail> {
        ensureLoaded()
        val recent = recentlyViewedCache.snapshot().values.toList().reversed()
        Logger.d { "CocktailCache.getRecentlyViewedCocktails() returning ${recent.size} items" }
        return recent
    }
    
    /**
     * Clear all cached cocktails.
     */
    suspend fun clearCache() {
        loadMutex.withLock { loaded = true } // nothing worth loading over a clear
        cocktailCache.clear()
        recentlyViewedCache.clear()
        // Clear from persistent storage
        settings.remove(ALL_COCKTAILS_KEY)
        settings.remove(RECENTLY_VIEWED_KEY)
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
    suspend fun getCachedCocktailCount(): Int {
        ensureLoaded()
        return cocktailCache.size()
    }
}