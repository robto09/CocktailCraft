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
 * Eviction matrix (SH-11): this class holds the VOLATILE side — per-category
 * lists with a 5-minute freshness window plus rate-limit bookkeeping — while
 * [CocktailCache] holds the PERSISTENT side (Settings-backed LRU, 24h TTL,
 * offline-aware). The two hold overlapping cocktail data with independent
 * expiry, so the single eviction entry point,
 * CocktailOfflineRepositoryImpl.clearCache(), always purges BOTH via
 * [clearDataCaches] + [CocktailCache.clearCache]. Never add an eviction path
 * that clears only one of the two.
 */
internal class CocktailCacheManager {

    private val mutex = Mutex()

    // Category-specific cache
    private val _categoryCacheMap = mutableMapOf<String, List<Cocktail>>()
    private val _categoryLastFetchMap = mutableMapOf<String, Long>()

    // Rate limit tracking: only the proactive min-interval throttle lives
    // here; retry/backoff is owned solely by Ktor's HttpRequestRetry (SH-7).
    private var _lastApiCallTime: Long = 0

    // Constants
    companion object {
        // Short freshness window for the volatile in-memory caches only.
        // The persistent CocktailCache has its own, much longer lifetime
        // (AppConfig.cacheExpirationMs, 24 h) — the two are deliberately separate.
        const val CACHE_VALIDITY_MS = 5 * 60 * 1000 // 5 minutes
        const val MIN_API_CALL_INTERVAL_MS = 1000L // 1 second between calls
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
        _categoryLastFetchMap[category] = kotlin.time.Clock.System.now().toEpochMilliseconds()
    }

    // --- Rate limiting ---

    suspend fun getLastApiCallTime(): Long = mutex.withLock {
        _lastApiCallTime
    }

    suspend fun setLastApiCallTime(value: Long) = mutex.withLock {
        _lastApiCallTime = value
    }

    /**
     * Purge the volatile per-category data caches. Part of the user-facing
     * "Clear Cache" path (SH-3), which must empty BOTH cache layers — this one
     * and the persistent [CocktailCache] (SH-11). Rate-limit bookkeeping is
     * deliberately preserved: clearing data must not grant a fresh API budget.
     */
    suspend fun clearDataCaches() = mutex.withLock {
        _categoryCacheMap.clear()
        _categoryLastFetchMap.clear()
    }
}

