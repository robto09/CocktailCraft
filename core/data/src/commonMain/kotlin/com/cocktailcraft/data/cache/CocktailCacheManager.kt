package com.cocktailcraft.data.cache

import com.cocktailcraft.domain.model.Cocktail
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe singleton that manages in-memory per-category cocktail caching.
 *
 * Extracted from CocktailRepositoryImpl's companion object to provide:
 * - Proper dependency injection via Koin
 * - Thread-safe access to mutable state via Mutex
 * - Persistence across repository instances (singleton scope)
 *
 * Eviction matrix (SH-11): this class holds the VOLATILE side — per-category
 * lists with a 5-minute freshness window (the API rate limiter is ApiRateLimiter) — while
 * [CocktailCache] holds the PERSISTENT side (Settings-backed LRU, 24h TTL,
 * offline-aware). The two hold overlapping cocktail data with deliberately
 * independent expiry; if a purge path is ever (re)introduced it must empty
 * BOTH layers together, never just one.
 */
internal class CocktailCacheManager {

    private val mutex = Mutex()

    // Category-specific cache
    private val _categoryCacheMap = mutableMapOf<String, List<Cocktail>>()
    private val _categoryLastFetchMap = mutableMapOf<String, Long>()

    // Constants
    companion object {
        // Short freshness window for the volatile in-memory caches only.
        // The persistent CocktailCache has its own, much longer lifetime
        // (AppConfig.cacheExpirationMs, 24 h) — the two are deliberately separate.
        const val CACHE_VALIDITY_MS = 5 * 60 * 1000 // 5 minutes
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
}
