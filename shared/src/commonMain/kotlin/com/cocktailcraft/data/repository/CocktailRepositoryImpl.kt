package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import kotlinx.coroutines.delay
import kotlin.time.Clock

/**
 * Search/detail/catalog repository over TheCocktailDB with aggressive caching.
 *
 * API Limitations & Workarounds:
 * - Filter endpoints (filter.php) only return partial data (id, name, thumbnail)
 * - Full cocktail details require individual lookup.php calls
 * - Price, rating, popularity are not provided by the API — the remote data
 *   source derives deterministic demo values from the cocktail id
 * - The free API has rate limits, so we cache aggressively
 *
 * Favorites and offline-mode concerns live in their own implementations
 * ([CocktailFavoritesRepositoryImpl], [CocktailOfflineRepositoryImpl]) and are
 * exposed here via interface delegation only so the composite
 * [CocktailRepository] binding keeps working.
 */
internal class CocktailRepositoryImpl(
    private val remote: CocktailRemoteDataSource,
    private val cocktailCache: CocktailCache,
    private val cacheManager: CocktailCacheManager,
    private val appConfig: AppConfig,
    private val offlineRepository: CocktailOfflineRepositoryImpl,
    favoritesRepository: CocktailFavoritesRepository
) : CocktailRepository,
    CocktailFavoritesRepository by favoritesRepository,
    CocktailOfflineRepository by offlineRepository {

    private suspend fun isOffline(): Boolean = offlineRepository.isOffline()

    override suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>> {
        return try {
            Result.Success(remote.searchByName(name))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to search cocktails by name")
        }
    }

    override suspend fun getCocktailById(id: String): Result<Cocktail?> {
        return try {
            // A cached cocktail with full details can be served immediately
            val cachedCocktail = cocktailCache.getCachedCocktail(id)
            if (cachedCocktail != null && cachedCocktail.hasFullDetails) {
                cocktailCache.addToRecentlyViewed(cachedCocktail)
                return Result.Success(cachedCocktail)
            }

            // If we're offline, return whatever we have cached
            if (isOffline()) {
                return Result.Success(cachedCocktail)
            }

            // If online, fetch full details from API
            try {
                delay(200) // Avoid rate limiting on rapid successive calls
                val cocktail = remote.getById(id)
                if (cocktail != null) {
                    cocktailCache.cacheCocktail(cocktail)
                    cocktailCache.addToRecentlyViewed(cocktail)
                    cacheManager.updateGlobalCocktailsCache { if (it.id == id) cocktail else it }
                    Result.Success(cocktail)
                } else {
                    Result.Success(cachedCocktail)
                }
            } catch (e: Exception) {
                // On API error, return cached version
                Result.Success(cachedCocktail)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cocktail by ID")
        }
    }

    override suspend fun refreshCocktailById(id: String): Result<Cocktail?> {
        return try {
            if (isOffline()) {
                Result.Success(cocktailCache.getCachedCocktail(id))
            } else {
                delay(200) // Avoid rate limiting
                val cocktail = remote.getById(id)
                if (cocktail != null) {
                    cocktailCache.cacheCocktail(cocktail)
                    cocktailCache.addToRecentlyViewed(cocktail)
                    cacheManager.updateGlobalCocktailsCache { if (it.id == id) cocktail else it }
                    Result.Success(cocktail)
                } else {
                    Result.Success(cocktailCache.getCachedCocktail(id))
                }
            }
        } catch (e: Exception) {
            Logger.e { "Error refreshing cocktail $id: ${e.message}" }
            Result.Error(e.message ?: "Failed to refresh cocktail")
        }
    }

    override suspend fun getRandomCocktail(): Result<Cocktail?> {
        return try {
            Result.Success(remote.getRandom())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get random cocktail")
        }
    }

    override suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>> {
        return try {
            Result.Success(remote.filterByIngredient(ingredient))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by ingredient")
        }
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>> {
        return try {
            Result.Success(remote.filterByAlcoholic(alcoholic))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by alcoholic")
        }
    }

    override suspend fun filterByCategory(category: String): Result<List<Cocktail>> {
        return try {
            Result.Success(fetchCocktailsByCategory(category))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by category")
        }
    }

    /**
     * Lazily fetch cocktails by category with caching and rate limiting.
     */
    private suspend fun fetchCocktailsByCategory(category: String): List<Cocktail> {
        try {
            // First check category-specific cache
            val categoryCachedCocktails = cacheManager.getCategoryCache(category)
            val categoryLastFetch = cacheManager.getCategoryLastFetchTime(category)
            val categoryCacheAge = Clock.System.now().toEpochMilliseconds() - categoryLastFetch

            // Return cached data if available and fresh
            if (categoryCachedCocktails.isNotEmpty() && categoryCacheAge < CocktailCacheManager.CACHE_VALIDITY_MS) {
                return categoryCachedCocktails.sortedByDescending { it.dateAdded }
            }

            // Check if we're offline
            if (isOffline()) {
                if (categoryCachedCocktails.isNotEmpty()) {
                    return categoryCachedCocktails.sortedByDescending { it.dateAdded }
                }
                return cocktailCache.getAllCachedCocktails()
                    .filter { it.category == category }
                    .sortedByDescending { it.dateAdded }
            }

            // Apply rate limiting with exponential backoff
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
                remote.noteSuccess()

                // Prefer full cached details over the filter endpoint's partial data
                val enrichedCocktails = basicDtos.map { basicDto ->
                    if (!basicDto.instructions.isNullOrBlank()) {
                        remote.mapToDomain(basicDto)
                    } else {
                        cocktailCache.getCachedCocktail(basicDto.id) ?: remote.mapToDomain(basicDto)
                    }
                }

                cacheManager.setCategoryCache(category, enrichedCocktails)
                enrichedCocktails.forEach { cocktailCache.cacheCocktail(it) }

                return enrichedCocktails.sortedByDescending { it.dateAdded }
            } catch (e: Exception) {
                Logger.e { "API call failed: ${e.message}" }
                remote.noteFailure(e)

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
        } catch (e: Exception) {
            Logger.e { "fetchCocktailsByCategory failed: ${e.message}" }
            return emptyList()
        }
    }

    override suspend fun getCategories(): Result<List<String>> {
        return try {
            Result.Success(remote.getCategories())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get categories")
        }
    }

    override suspend fun getIngredients(): Result<List<String>> {
        return try {
            Result.Success(remote.getIngredients())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get ingredients")
        }
    }

    override suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>> {
        return try {
            Result.Success(fetchCocktailsByCategory(CocktailCategories.DEFAULT))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cocktails sorted by newest")
        }
    }

    override fun getCocktailImageUrl(cocktail: Cocktail): String {
        if (!cocktail.imageUrl.isNullOrBlank()) {
            return cocktail.imageUrl
        }
        return if (cocktail.id.isNotBlank()) {
            "${appConfig.imageBaseUrl}/${appConfig.cocktailsImagePath}/${cocktail.id}.jpg"
        } else {
            ""
        }
    }

    /**
     * Advanced search over the 4 supported filters.
     *
     * TheCocktailDB cannot combine filters server-side, but every filter.php call
     * returns a complete id set for its dimension, so combining = intersecting id
     * sets client-side. One rate-limited call per active filter; the first
     * successful result (priority order below, preferring full name-search records)
     * is the base list, narrowed to ids present in every other active set. Any
     * active call failing surfaces as [Result.Error] — a filter is never silently
     * dropped.
     */
    override suspend fun advancedSearch(filters: SearchFilters): Result<List<Cocktail>> {
        return try {
            if (isOffline()) {
                return Result.Success(applyFiltersInMemory(cocktailCache.getAllCachedCocktails(), filters))
            }

            // Nothing active → same default list as category browsing.
            if (filters.query.isBlank() && !filters.hasActiveFilters()) {
                return filterByCategory(CocktailCategories.DEFAULT)
            }

            // One call per active filter, in priority order. getOrThrow bubbles a
            // failed call out to the catch below as Result.Error.
            val activeSets = mutableListOf<List<Cocktail>>()
            if (filters.query.isNotBlank()) {
                activeSets.add(searchCocktailsByName(filters.query).getOrThrow())
            }
            if (filters.category != null) {
                activeSets.add(filterByCategory(filters.category).getOrThrow())
            }
            if (filters.ingredient != null) {
                activeSets.add(filterByIngredient(filters.ingredient).getOrThrow())
            }
            if (filters.alcoholic != null) {
                activeSets.add(filterByAlcoholic(filters.alcoholic).getOrThrow())
            }

            // Base list = first active set; keep only ids present in every other set.
            val base = activeSets.first()
            val result = activeSets.drop(1).fold(base) { acc, set ->
                val ids = set.mapTo(HashSet()) { it.id }
                acc.filter { it.id in ids }
            }

            // Cache the results for offline access
            result.forEach { cocktailCache.cacheCocktail(it) }

            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to perform advanced search")
        }
    }

    /** Offline path: filter the in-memory cache on the 4 supported fields. */
    private fun applyFiltersInMemory(cocktails: List<Cocktail>, filters: SearchFilters): List<Cocktail> {
        var result = cocktails
        val query = filters.query
        val category = filters.category
        val ingredient = filters.ingredient
        val alcoholic = filters.alcoholic

        if (query.isNotBlank()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }
        if (category != null) {
            result = result.filter { it.category == category }
        }
        if (ingredient != null) {
            result = result.filter { cocktail ->
                cocktail.ingredients.any { it.name.contains(ingredient, ignoreCase = true) }
            }
        }
        if (alcoholic != null) {
            // Full records store TheCocktailDB's display strings ("Alcoholic" /
            // "Non alcoholic"); the underscore form only exists as a URL token.
            result = result.filter {
                val value = it.alcoholic?.replace('_', ' ')
                if (alcoholic) value.equals("Alcoholic", ignoreCase = true)
                else value.equals("Non alcoholic", ignoreCase = true)
            }
        }
        return result
    }
}
