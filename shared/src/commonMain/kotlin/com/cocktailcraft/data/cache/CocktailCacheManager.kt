package com.cocktailcraft.data.cache

import com.cocktailcraft.domain.model.Cocktail
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe singleton that manages in-memory cocktail caching and API rate limiting.
 *
 * Extracted from CocktailRepositoryImpl's companion object to provide:
 * - Proper dependency injection via Koin
 * - Thread-safe access to mutable state via Mutex
 * - Persistence across repository instances (singleton scope)
 *
 * Note: This is separate from [CocktailCache] which handles persistent (Settings-based) caching.
 * This class manages volatile in-memory caches and rate-limit tracking only.
 */
internal class CocktailCacheManager {

    private val mutex = Mutex()

    // Global cache to persist across repository instances
    private var _globalCocktailsCache: List<Cocktail> = emptyList()
    private var _globalLastFetchTime: Long = 0

    // Category-specific cache
    private val _categoryCacheMap = mutableMapOf<String, List<Cocktail>>()
    private val _categoryLastFetchMap = mutableMapOf<String, Long>()

    // Rate limit tracking
    private var _lastApiCallTime: Long = 0
    private var _rateLimitBackoffMs: Long = 0

    // Constants
    companion object {
        const val CACHE_VALIDITY_MS = 5 * 60 * 1000 // 5 minutes
        const val MIN_API_CALL_INTERVAL_MS = 1000L // 1 second between calls
        const val MAX_BACKOFF_MS = 30000L // Max 30 seconds backoff
    }

    // --- Global cocktails cache ---

    suspend fun getGlobalCocktailsCache(): List<Cocktail> = mutex.withLock {
        _globalCocktailsCache
    }

    suspend fun setGlobalCocktailsCache(value: List<Cocktail>) = mutex.withLock {
        _globalCocktailsCache = value
    }

    suspend fun getGlobalLastFetchTime(): Long = mutex.withLock {
        _globalLastFetchTime
    }

    suspend fun setGlobalLastFetchTime(value: Long) = mutex.withLock {
        _globalLastFetchTime = value
    }

    // --- Category cache ---

    suspend fun getCategoryCache(category: String): List<Cocktail> = mutex.withLock {
        _categoryCacheMap[category] ?: emptyList()
    }

    suspend fun getCategoryLastFetchTime(category: String): Long = mutex.withLock {
        _categoryLastFetchMap[category] ?: 0L
    }

    suspend fun setCategoryCache(category: String, cocktails: List<Cocktail>) = mutex.withLock {
        _categoryCacheMap[category] = cocktails
        _categoryLastFetchMap[category] = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }

    // --- Rate limiting ---

    suspend fun getLastApiCallTime(): Long = mutex.withLock {
        _lastApiCallTime
    }

    suspend fun setLastApiCallTime(value: Long) = mutex.withLock {
        _lastApiCallTime = value
    }

    suspend fun getRateLimitBackoffMs(): Long = mutex.withLock {
        _rateLimitBackoffMs
    }

    suspend fun setRateLimitBackoffMs(value: Long) = mutex.withLock {
        _rateLimitBackoffMs = value
    }

    suspend fun resetBackoff() = mutex.withLock {
        _rateLimitBackoffMs = 0
    }

    suspend fun applyExponentialBackoff() = mutex.withLock {
        _rateLimitBackoffMs = minOf(
            if (_rateLimitBackoffMs == 0L) 2000L else _rateLimitBackoffMs * 2,
            MAX_BACKOFF_MS
        )
    }

    /**
     * Update the global cocktails cache by transforming each entry.
     * Useful for updating individual cocktails in the cache.
     */
    suspend fun updateGlobalCocktailsCache(transform: (Cocktail) -> Cocktail) = mutex.withLock {
        _globalCocktailsCache = _globalCocktailsCache.map(transform)
    }
}

