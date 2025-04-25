package com.cocktailcraft.data.cache

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock

/**
 * Manages caching of cocktail data for offline access.
 */
class CocktailCache(
    private val settings: Settings,
    private val json: Json,
    private val appConfig: AppConfig
) {
    companion object {
        private const val CACHE_PREFIX = "cocktail_cache_"
        private const val RECENTLY_VIEWED_KEY = "recently_viewed_cocktails"
        private const val CACHE_METADATA_PREFIX = "cache_metadata_"
        private const val MAX_RECENTLY_VIEWED = 20
    }
    
    /**
     * Cache a cocktail for offline access.
     */
    fun cacheCocktail(cocktail: Cocktail) {
        try {
            // Cache the cocktail data
            val cocktailJson = json.encodeToString(cocktail)
            settings.putString("$CACHE_PREFIX${cocktail.id}", cocktailJson)
            
            // Store cache metadata (timestamp)
            val timestamp = Clock.System.now().toEpochMilliseconds()
            settings.putLong("$CACHE_METADATA_PREFIX${cocktail.id}", timestamp)
            
            // Update recently viewed list
            addToRecentlyViewed(cocktail.id)
        } catch (e: Exception) {
            println("Error caching cocktail: ${e.message}")
        }
    }
    
    /**
     * Get a cached cocktail by ID.
     * Returns null if the cocktail is not in the cache or if the cache has expired.
     */
    fun getCachedCocktail(id: String): Cocktail? {
        try {
            val cocktailJson = settings.getStringOrNull("$CACHE_PREFIX$id") ?: return null
            
            // Check if cache has expired
            val timestamp = settings.getLongOrNull("$CACHE_METADATA_PREFIX$id") ?: 0L
            val currentTime = Clock.System.now().toEpochMilliseconds()
            
            if (currentTime - timestamp > appConfig.cacheExpirationMs) {
                // Cache expired, remove it
                settings.remove("$CACHE_PREFIX$id")
                settings.remove("$CACHE_METADATA_PREFIX$id")
                return null
            }
            
            return json.decodeFromString<Cocktail>(cocktailJson)
        } catch (e: Exception) {
            println("Error retrieving cached cocktail: ${e.message}")
            return null
        }
    }
    
    /**
     * Get all cached cocktails.
     */
    fun getAllCachedCocktails(): List<Cocktail> {
        val cachedCocktails = mutableListOf<Cocktail>()
        
        try {
            // Get all keys that start with the cache prefix
            val allKeys = settings.keys.filter { it.startsWith(CACHE_PREFIX) }
            
            for (key in allKeys) {
                val id = key.removePrefix(CACHE_PREFIX)
                val cocktail = getCachedCocktail(id)
                if (cocktail != null) {
                    cachedCocktails.add(cocktail)
                }
            }
        } catch (e: Exception) {
            println("Error retrieving all cached cocktails: ${e.message}")
        }
        
        return cachedCocktails
    }
    
    /**
     * Add a cocktail ID to the recently viewed list.
     */
    private fun addToRecentlyViewed(id: String) {
        try {
            val recentlyViewed = getRecentlyViewedIds().toMutableList()
            
            // Remove if already in the list (to move it to the front)
            recentlyViewed.remove(id)
            
            // Add to the front of the list
            recentlyViewed.add(0, id)
            
            // Trim the list if it exceeds the maximum size
            if (recentlyViewed.size > MAX_RECENTLY_VIEWED) {
                recentlyViewed.removeAt(recentlyViewed.lastIndex)
            }
            
            // Save the updated list
            settings.putString(RECENTLY_VIEWED_KEY, json.encodeToString(recentlyViewed))
        } catch (e: Exception) {
            println("Error updating recently viewed list: ${e.message}")
        }
    }
    
    /**
     * Get the list of recently viewed cocktail IDs.
     */
    fun getRecentlyViewedIds(): List<String> {
        return try {
            val recentlyViewedJson = settings.getStringOrNull(RECENTLY_VIEWED_KEY) ?: "[]"
            json.decodeFromString<List<String>>(recentlyViewedJson)
        } catch (e: Exception) {
            println("Error retrieving recently viewed list: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get the list of recently viewed cocktails.
     */
    fun getRecentlyViewedCocktails(): List<Cocktail> {
        val recentlyViewedIds = getRecentlyViewedIds()
        return recentlyViewedIds.mapNotNull { getCachedCocktail(it) }
    }
    
    /**
     * Clear all cached cocktails.
     */
    fun clearCache() {
        try {
            val allKeys = settings.keys.filter { 
                it.startsWith(CACHE_PREFIX) || it.startsWith(CACHE_METADATA_PREFIX) 
            }
            
            for (key in allKeys) {
                settings.remove(key)
            }
            
            // Also clear recently viewed list
            settings.remove(RECENTLY_VIEWED_KEY)
        } catch (e: Exception) {
            println("Error clearing cache: ${e.message}")
        }
    }
    
    /**
     * Check if a cocktail is cached.
     */
    fun isCocktailCached(id: String): Boolean {
        return settings.hasKey("$CACHE_PREFIX$id")
    }
    
    /**
     * Get the number of cached cocktails.
     */
    fun getCachedCocktailCount(): Int {
        return settings.keys.count { it.startsWith(CACHE_PREFIX) }
    }
}
