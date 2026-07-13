package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import kotlin.time.Clock

/**
 * Lazily fetches cocktails by category with caching and rate limiting.
 *
 * Extracted from the former CocktailRepositoryImpl monolith because this
 * fetch-and-cache path is shared by two focused repositories:
 * [CocktailSearchRepositoryImpl.filterByCategory] and
 * [CocktailCatalogRepositoryImpl.getCocktailsSortedByNewest].
 *
 * API Limitations & Workarounds:
 * - Filter endpoints (filter.php) only return partial data (id, name, thumbnail)
 * - Full cocktail details require individual lookup.php calls
 * - The free API has rate limits, so we cache aggressively
 */
internal class CocktailCategoryFetcher(
    private val remote: CocktailRemoteDataSource,
    private val cocktailCache: CocktailCache,
    private val cacheManager: CocktailCacheManager,
    private val offlineRepository: CocktailOfflineRepository
) {

    /**
     * Fetch cocktails for [category], preferring fresh category cache, then
     * cached fallbacks when the API fails. A genuine failure with no cached
     * data to fall back on PROPAGATES as an exception — callers wrap it in
     * Result.Error so the UI can show an error/retry instead of silently
     * rendering an empty list. An empty return means the category is empty.
     */
    suspend fun fetchCocktailsByCategory(category: String): List<Cocktail> {
        // First check category-specific cache
        val categoryCachedCocktails = cacheManager.getCategoryCache(category)
        val categoryLastFetch = cacheManager.getCategoryLastFetchTime(category)
        val categoryCacheAge = Clock.System.now().toEpochMilliseconds() - categoryLastFetch

        // Return cached data if available and fresh
        if (categoryCachedCocktails.isNotEmpty() && categoryCacheAge < CocktailCacheManager.CACHE_VALIDITY_MS) {
            return categoryCachedCocktails.sortedByDescending { it.dateAdded }
        }

        // Check if we're offline
        if (offlineRepository.isOffline()) {
            if (categoryCachedCocktails.isNotEmpty()) {
                return categoryCachedCocktails.sortedByDescending { it.dateAdded }
            }
            return cocktailCache.getAllCachedCocktails()
                .filter { it.category == category }
                .sortedByDescending { it.dateAdded }
        }

        // Respect the minimum interval between API calls (retry/backoff is
        // Ktor's job — SH-7)
        remote.awaitRateLimitWindow()

        // Check API connectivity
        if (!remote.ping()) {
            Logger.w { "API unreachable" }
            if (categoryCachedCocktails.isNotEmpty()) {
                return categoryCachedCocktails.sortedByDescending { it.dateAdded }
            }
            throw Exception("API is not reachable")
        }

        remote.noteApiCall()

        try {
            val basicDtos = remote.filterByCategoryRaw(category)

            // Prefer full cached details over the filter endpoint's partial data
            val enrichedCocktails = basicDtos.map { basicDto ->
                if (!basicDto.instructions.isNullOrBlank()) {
                    remote.mapToDomain(basicDto)
                } else {
                    cocktailCache.getCachedCocktail(basicDto.id) ?: remote.mapToDomain(basicDto)
                }
            }

            cacheManager.setCategoryCache(category, enrichedCocktails)
            cocktailCache.cacheCocktails(enrichedCocktails)

            return enrichedCocktails.sortedByDescending { it.dateAdded }
        } catch (e: Exception) {
            Logger.e { "API call failed: ${e.message}" }

            // If we have cached data, use it
            if (categoryCachedCocktails.isNotEmpty()) {
                return categoryCachedCocktails.sortedByDescending { it.dateAdded }
            }

            // Try general cache as last resort
            val generalCache = cocktailCache.getAllCachedCocktails()
                .filter { it.category == category }
            if (generalCache.isNotEmpty()) {
                return generalCache.sortedByDescending { it.dateAdded }
            } else {
                throw e
            }
        }
    }
}
