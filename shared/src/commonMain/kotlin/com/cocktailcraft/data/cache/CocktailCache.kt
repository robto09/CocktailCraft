package com.cocktailcraft.data.cache

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.mayakapps.kache.Kache
import com.mayakapps.kache.KacheStrategy
import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.datetime.Clock
import com.cocktailcraft.util.CocktailDebugLogger

/**
 * Manages caching of cocktail data for offline access using the Kache library.
 */
class CocktailCache(
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
    private val cocktailCache = Kache.lru<String, Cocktail>(
        maxSize = MAX_CACHED_COCKTAILS
    )

    // In-memory cache for recently viewed cocktails
    private val recentlyViewedCache = Kache.lru<String, Cocktail>(
        maxSize = MAX_RECENTLY_VIEWED
    )
    
    init {
        CocktailDebugLogger.log("🗄️ CocktailCache init()")
        // Load persisted cocktails into memory cache on initialization
        loadPersistedCocktails()
    }
    
    private fun loadPersistedCocktails() {
        CocktailDebugLogger.log("📂 Loading persisted cocktails...")
        try {
            // Load all cached cocktails from persistent storage
            val cachedJson = settings.getStringOrNull(ALL_COCKTAILS_KEY)
            if (!cachedJson.isNullOrBlank()) {
                val cocktails = json.decodeFromString<List<Cocktail>>(cachedJson)
                CocktailDebugLogger.log("   ✅ Loaded ${cocktails.size} cocktails from persistent storage")
                cocktails.forEach { cocktail ->
                    cocktailCache.put(cocktail.id, cocktail)
                }
            } else {
                CocktailDebugLogger.log("   ⚠️ No persisted cocktails found")
            }
            
            // Load recently viewed cocktails
            val recentJson = settings.getStringOrNull(RECENTLY_VIEWED_KEY)
            if (!recentJson.isNullOrBlank()) {
                val recentCocktails = json.decodeFromString<List<Cocktail>>(recentJson)
                CocktailDebugLogger.log("   ✅ Loaded ${recentCocktails.size} recently viewed cocktails")
                recentCocktails.forEach { cocktail ->
                    recentlyViewedCache.put(cocktail.id, cocktail)
                }
            } else {
                CocktailDebugLogger.log("   ⚠️ No recently viewed cocktails found")
            }
        } catch (e: Exception) {
            // Log error but don't crash - caching is not critical
            CocktailDebugLogger.log("   ❌ Failed to load persisted cocktails: ${e.message}")
        }
    }
    
    private fun persistCocktails() {
        try {
            // Persist all cached cocktails
            val allCocktails = cocktailCache.snapshot().values.toList()
            CocktailDebugLogger.log("💾 Persisting ${allCocktails.size} cocktails to storage")
            if (allCocktails.isNotEmpty()) {
                val json = json.encodeToString(allCocktails)
                settings.putString(ALL_COCKTAILS_KEY, json)
                CocktailDebugLogger.log("   ✅ Successfully persisted cocktails")
            }
        } catch (e: Exception) {
            CocktailDebugLogger.log("   ❌ Failed to persist cocktails: ${e.message}")
        }
    }
    
    private fun persistRecentlyViewed() {
        try {
            // Persist recently viewed cocktails
            val recentCocktails = recentlyViewedCache.snapshot().values.toList()
            if (recentCocktails.isNotEmpty()) {
                val json = json.encodeToString(recentCocktails)
                settings.putString(RECENTLY_VIEWED_KEY, json)
            }
        } catch (e: Exception) {
            CocktailDebugLogger.log("Failed to persist recently viewed: ${e.message}")
        }
    }
    
    /**
     * Cache a cocktail for offline access.
     */
    suspend fun cacheCocktail(cocktail: Cocktail) {
        cocktailCache.put(cocktail.id, cocktail)
        persistCocktails() // Persist to storage
    }
    
    /**
     * Get a cached cocktail by ID.
     */
    suspend fun getCachedCocktail(id: String): Cocktail? {
        return cocktailCache.get(id)
    }
    
    /**
     * Get all cached cocktails.
     */
    suspend fun getAllCachedCocktails(): List<Cocktail> {
        val cocktails = cocktailCache.snapshot().values.toList()
        CocktailDebugLogger.log("📦 CocktailCache.getAllCachedCocktails() returning ${cocktails.size} items")
        return cocktails
    }
    
    /**
     * Add a cocktail to the recently viewed list.
     */
    suspend fun addToRecentlyViewed(cocktail: Cocktail) {
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
        val recent = recentlyViewedCache.snapshot().values.toList().reversed()
        CocktailDebugLogger.log("👀 CocktailCache.getRecentlyViewedCocktails() returning ${recent.size} items")
        return recent
    }
    
    /**
     * Clear all cached cocktails.
     */
    fun clearCache() {
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
        return cocktailCache.get(id) != null
    }
    
    /**
     * Get the number of cached cocktails.
     */
    fun getCachedCocktailCount(): Int {
        return cocktailCache.size
    }
}
