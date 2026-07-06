package com.cocktailcraft.data.cache

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import co.touchlab.kermit.Logger
import kotlin.time.Clock
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
}

/**
 * Manages caching of cocktail data for offline access using a simple LRU cache.
 */
internal class CocktailCache(
    private val settings: Settings,
    private val json: Json,
    private val appConfig: AppConfig
) {
    companion object {
        private const val CACHE_PREFIX = "cocktail_cache_"
        private const val RECENTLY_VIEWED_KEY = "recently_viewed_cocktails"
        private const val ALL_COCKTAILS_KEY = "all_cached_cocktails"
        private const val CACHE_METADATA_PREFIX = "cache_metadata_"
        private const val MAX_RECENTLY_VIEWED = 20
        private const val MAX_CACHED_COCKTAILS = 100
    }
    
    // In-memory cache for cocktails using LRU strategy
    private val cocktailCache = SimpleLruCache<String, Cocktail>(MAX_CACHED_COCKTAILS)

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

    private suspend fun loadPersistedCocktails() {
        Logger.d { "Loading persisted cocktails..." }
        try {
            // Load all cached cocktails from persistent storage
            val cachedJson = settings.getStringOrNull(ALL_COCKTAILS_KEY)
            if (!cachedJson.isNullOrBlank()) {
                val cocktails = json.decodeFromString<List<Cocktail>>(cachedJson)
                Logger.d { "Loaded ${cocktails.size} cocktails from persistent storage" }
                cocktails.forEach { cocktail ->
                    cocktailCache.put(cocktail.id, cocktail)
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
            val allCocktails = cocktailCache.snapshot().values.toList()
            Logger.d { "Persisting ${allCocktails.size} cocktails to storage" }
            if (allCocktails.isNotEmpty()) {
                val jsonString = json.encodeToString(allCocktails)
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
        cocktailCache.put(cocktail.id, cocktail)
        persistCocktails() // Persist to storage
    }
    
    /**
     * Get a cached cocktail by ID.
     */
    suspend fun getCachedCocktail(id: String): Cocktail? {
        ensureLoaded()
        return cocktailCache.get(id)
    }
    
    /**
     * Get all cached cocktails.
     */
    suspend fun getAllCachedCocktails(): List<Cocktail> {
        ensureLoaded()
        val cocktails = cocktailCache.snapshot().values.toList()
        Logger.d { "CocktailCache.getAllCachedCocktails() returning ${cocktails.size} items" }
        return cocktails
    }
    
    /**
     * Add a cocktail to the recently viewed list.
     */
    suspend fun addToRecentlyViewed(cocktail: Cocktail) {
        ensureLoaded()
        recentlyViewedCache.put(cocktail.id, cocktail)
        // Also add to main cache if not already there
        if (cocktailCache.get(cocktail.id) == null) {
            cocktailCache.put(cocktail.id, cocktail)
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
     * Check if a cocktail is cached.
     */
    suspend fun isCocktailCached(id: String): Boolean {
        ensureLoaded()
        return cocktailCache.get(id) != null
    }
    
    /**
     * Get the number of cached cocktails.
     */
    suspend fun getCachedCocktailCount(): Int {
        ensureLoaded()
        return cocktailCache.size()
    }
}