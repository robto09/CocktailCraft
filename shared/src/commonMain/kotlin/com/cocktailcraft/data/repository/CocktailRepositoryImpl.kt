package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.usecase.FilterCocktailsUseCase
import com.cocktailcraft.domain.util.Result
import co.touchlab.kermit.Logger
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import com.cocktailcraft.domain.config.AppConfig
import kotlinx.datetime.Clock

/**
 * Implementation of CocktailRepository that handles API interactions and data caching.
 * 
 * API Limitations & Workarounds:
 * - Filter endpoints (filter.php) only return partial data (id, name, thumbnail)
 * - Full cocktail details require individual lookup.php calls
 * - Price, rating, popularity are not provided by API - we generate demo values
 * - Advanced filtering (multi-ingredient, taste profiles) requires local filtering
 * - The free API has rate limits, so we cache aggressively
 */
internal class CocktailRepositoryImpl(
    private val api: CocktailApi,
    private val settings: Settings,
    private val appConfig: AppConfig,
    private val networkMonitor: NetworkMonitor,
    private val cocktailCache: CocktailCache,
    private val cacheManager: CocktailCacheManager
) : CocktailRepository {

    // Flag to force offline mode (user preference)
    private var forceOfflineMode: Boolean
        get() = settings.getBoolean(appConfig.offlineModeEnabledKey, false)
        set(value) = settings.putBoolean(appConfig.offlineModeEnabledKey, value)

    // Check if we're currently offline
    private suspend fun isOffline(): Boolean {
        val networkOnline = networkMonitor.isOnline.first()
        val result = forceOfflineMode || !networkOnline
        Logger.d { "isOffline check: forceOfflineMode=$forceOfflineMode, networkOnline=$networkOnline, result=$result" }
        return result
    }

    override suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>> {
        return try {
            Result.Success(api.searchCocktailsByName(name).map { dto ->
                mapDtoToCocktail(dto)
            })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to search cocktails by name")
        }
    }

    override suspend fun searchCocktailsByFirstLetter(letter: Char): Result<List<Cocktail>> {
        return try {
            Result.Success(api.searchCocktailsByFirstLetter(letter).map { dto ->
                mapDtoToCocktail(dto)
            })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to search cocktails by first letter")
        }
    }

    override suspend fun getCocktailById(id: String): Result<Cocktail?> {
        return try {
            // First check if we have a cached cocktail with full details
            val cachedCocktail = cocktailCache.getCachedCocktail(id)

            // If we have a cached cocktail with ingredients (full details), return it
            if (cachedCocktail != null && cachedCocktail.hasFullDetails) {
                // Add to recently viewed when accessed from cache
                cocktailCache.addToRecentlyViewed(cachedCocktail)
                return Result.Success(cachedCocktail)
            }

            // If we're offline, return whatever we have cached
            if (isOffline()) {
                return Result.Success(cachedCocktail)
            }

            // If online, fetch full details from API
            try {
                // Add a small delay to avoid rate limiting if called rapidly
                delay(200)

                val dto = api.getCocktailById(id)

                if (dto != null) {
                    val cocktail = mapDtoToCocktail(dto)

                    // Cache the full cocktail details
                    cocktailCache.cacheCocktail(cocktail)

                    // Add to recently viewed
                    cocktailCache.addToRecentlyViewed(cocktail)

                    // Update the in-memory cache as well
                    cacheManager.updateGlobalCocktailsCache {
                        if (it.id == id) cocktail else it
                    }

                    Result.Success(cocktail)
                } else {
                    // If API returns null, use cached version
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
                val dto = api.getCocktailById(id)

                if (dto != null) {
                    val cocktail = mapDtoToCocktail(dto)
                    cocktailCache.cacheCocktail(cocktail)
                    cocktailCache.addToRecentlyViewed(cocktail)
                    cacheManager.updateGlobalCocktailsCache {
                        if (it.id == id) cocktail else it
                    }
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
            val dto = api.getRandomCocktail()
            Result.Success(if (dto != null) mapDtoToCocktail(dto) else null)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get random cocktail")
        }
    }

    override suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>> {
        return try {
            Result.Success(api.filterByIngredient(ingredient).map { dto ->
                mapDtoToCocktail(dto)
            })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by ingredient")
        }
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>> {
        return try {
            Result.Success(api.filterByAlcoholic(alcoholic).map { dto ->
                mapDtoToCocktail(dto)
            })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by alcoholic")
        }
    }

    override suspend fun filterByCategory(category: String): Result<List<Cocktail>> {
        Logger.d { "filterByCategory() called with category: $category" }
        return try {
            Result.Success(fetchCocktailsByCategory(category))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by category")
        }
    }

    /**
     * Lazily fetch cocktails by category with caching and rate limiting
     */
    private suspend fun fetchCocktailsByCategory(category: String): List<Cocktail> {
        Logger.d { "fetchCocktailsByCategory() called for: $category" }

        try {
            // First check category-specific cache
            val categoryCachedCocktails = cacheManager.getCategoryCache(category)
            val categoryLastFetch = cacheManager.getCategoryLastFetchTime(category)
            val categoryCacheAge = Clock.System.now().toEpochMilliseconds() - categoryLastFetch

            Logger.d { "Category cache size: ${categoryCachedCocktails.size}" }
            Logger.d { "Category cache age: ${categoryCacheAge/1000}s" }

            // Return cached data if available and fresh
            if (categoryCachedCocktails.isNotEmpty() && categoryCacheAge < CocktailCacheManager.CACHE_VALIDITY_MS) {
                Logger.d { "Category cache is fresh, returning ${categoryCachedCocktails.size} cached cocktails" }
                return categoryCachedCocktails.sortedByDescending { it.dateAdded }
            }

            // Check if we're offline
            if (isOffline()) {
                Logger.d { "Offline mode - using cache only" }
                if (categoryCachedCocktails.isNotEmpty()) {
                    return categoryCachedCocktails.sortedByDescending { it.dateAdded }
                }
                val generalCache = cocktailCache.getAllCachedCocktails()
                    .filter { it.category == category }
                return generalCache.sortedByDescending { it.dateAdded }
            }

            // Apply rate limiting with exponential backoff
            val timeSinceLastCall = Clock.System.now().toEpochMilliseconds() - cacheManager.getLastApiCallTime()
            val requiredInterval = maxOf(CocktailCacheManager.MIN_API_CALL_INTERVAL_MS, cacheManager.getRateLimitBackoffMs())

            if (timeSinceLastCall < requiredInterval) {
                val waitTime = requiredInterval - timeSinceLastCall
                Logger.d { "Rate limiting: waiting ${waitTime}ms before API call" }
                delay(waitTime)
            }

            // Check API connectivity
            if (!pingApiInternal()) {
                Logger.w { "API unreachable" }
                if (categoryCachedCocktails.isNotEmpty()) {
                    return categoryCachedCocktails.sortedByDescending { it.dateAdded }
                }
                throw Exception("API is not reachable")
            }

            Logger.d { "Fetching $category cocktails from API..." }
            cacheManager.setLastApiCallTime(Clock.System.now().toEpochMilliseconds())

            try {
                val basicCocktails = api.filterByCategory(category)

                // Reset backoff on successful call
                cacheManager.resetBackoff()

                // Map and cache cocktails
                val enrichedCocktails = basicCocktails.map { basicDto ->
                    try {
                        // Check if we already have full data
                        if (!basicDto.instructions.isNullOrBlank()) {
                            mapDtoToCocktail(basicDto)
                        } else {
                            // Try to get from cache first
                            val cachedCocktail = cocktailCache.getCachedCocktail(basicDto.id)
                            if (cachedCocktail != null) {
                                cachedCocktail
                            } else {
                                mapDtoToCocktail(basicDto)
                            }
                        }
                    } catch (e: Exception) {
                        mapDtoToCocktail(basicDto)
                    }
                }

                Logger.d { "Fetched ${enrichedCocktails.size} cocktails for $category" }

                // Update category cache
                cacheManager.setCategoryCache(category, enrichedCocktails)

                // Cache individual cocktails
                enrichedCocktails.forEach { cocktail ->
                    cocktailCache.cacheCocktail(cocktail)
                }

                return enrichedCocktails.sortedByDescending { it.dateAdded }

            } catch (e: Exception) {
                Logger.e { "API call failed: ${e.message}" }

                // Check if it's a rate limit error
                if (e.message?.contains("429") == true || e.message?.contains("Too Many Requests") == true) {
                    Logger.w { "Rate limited - applying exponential backoff" }
                    cacheManager.applyExponentialBackoff()
                    Logger.d { "Next backoff: ${cacheManager.getRateLimitBackoffMs()}ms" }
                }

                // If we have cached data, use it
                if (categoryCachedCocktails.isNotEmpty()) {
                    Logger.d { "Using stale cache due to API error" }
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

    override suspend fun filterByGlass(glass: String): Result<List<Cocktail>> {
        return try {
            Result.Success(api.filterByGlass(glass).map { dto ->
                mapDtoToCocktail(dto)
            })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to filter by glass")
        }
    }

    override suspend fun getCategories(): Result<List<String>> {
        return try {
            Result.Success(api.getCategories().map { it.name })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get categories")
        }
    }

    override suspend fun getGlasses(): Result<List<String>> {
        return try {
            Result.Success(api.getGlasses().map { it.name })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get glasses")
        }
    }

    override suspend fun getIngredients(): Result<List<String>> {
        return try {
            Result.Success(api.getIngredients().map { it.name })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get ingredients")
        }
    }

    override suspend fun getAlcoholicFilters(): Result<List<String>> {
        return try {
            Result.Success(api.getAlcoholicFilters().map { it.name })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get alcoholic filters")
        }
    }

    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> {
        return try {
            val favoriteIds = settings.getStringOrNull(appConfig.favoritesStorageKey)
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

            val favoriteCocktails = mutableListOf<Cocktail>()

            // If we're offline, only use cached favorites
            if (isOffline()) {
                for (id in favoriteIds) {
                    val cachedCocktail = cocktailCache.getCachedCocktail(id)
                    if (cachedCocktail != null) {
                        favoriteCocktails.add(cachedCocktail)
                    }
                }
                return Result.Success(favoriteCocktails)
            }

            // If online, get favorites from API and cache them
            for (id in favoriteIds) {
                val cocktailResult = getCocktailById(id)
                val cocktail = cocktailResult.getOrNull()
                if (cocktail != null) {
                    favoriteCocktails.add(cocktail)
                    cocktailCache.cacheCocktail(cocktail)
                }
            }

            Result.Success(favoriteCocktails)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get favorite cocktails")
        }
    }

    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        return try {
            val currentFavorites = settings.getStringOrNull(appConfig.favoritesStorageKey)
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.toMutableList()
                ?: mutableListOf()

            if (!currentFavorites.contains(cocktail.id)) {
                currentFavorites.add(cocktail.id)
                settings.putString(appConfig.favoritesStorageKey, currentFavorites.joinToString(","))
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add to favorites")
        }
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> {
        return try {
            val currentFavorites = settings.getStringOrNull(appConfig.favoritesStorageKey)
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.toMutableList()
                ?: mutableListOf()

            currentFavorites.remove(cocktail.id)
            settings.putString(appConfig.favoritesStorageKey, currentFavorites.joinToString(","))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to remove from favorites")
        }
    }

    override suspend fun isCocktailFavorite(id: String): Result<Boolean> {
        return try {
            val favoriteIds = settings.getStringOrNull(appConfig.favoritesStorageKey)
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

            Result.Success(favoriteIds.contains(id))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to check if cocktail is favorite")
        }
    }

    override suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>> {
        Logger.d { "CocktailRepositoryImpl.getCocktailsSortedByNewest() called" }
        Logger.d { "LAZY LOADING: Only fetching 'Cocktail' category initially" }
        return try {
            Result.Success(fetchCocktailsByCategory("Cocktail"))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cocktails sorted by newest")
        }
    }

    override suspend fun getCocktailsSortedByPriceLowToHigh(): Result<List<Cocktail>> {
        return getCocktailsSortedByNewest().map { list -> list.sortedBy { it.price } }
    }

    override suspend fun getCocktailsSortedByPriceHighToLow(): Result<List<Cocktail>> {
        return getCocktailsSortedByNewest().map { list -> list.sortedByDescending { it.price } }
    }

    override suspend fun getCocktailsSortedByPopularity(): Result<List<Cocktail>> {
        return getCocktailsSortedByNewest().map { list -> list.sortedByDescending { it.popularity } }
    }

    override suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Result<List<Cocktail>> {
        return getCocktailsSortedByNewest().map { list -> list.filter { it.price in minPrice..maxPrice } }
    }

    private fun mapDtoToCocktail(dto: CocktailDto): Cocktail {
        return Cocktail(
            id = dto.id,
            name = dto.name,
            instructions = dto.instructions ?: Cocktail.PLACEHOLDER_INSTRUCTIONS,
            imageUrl = dto.imageUrl,
            price = generateRandomPrice(),
            ingredients = dto.getIngredients().ifEmpty {
                // If no ingredients (from filter endpoint), add placeholder
                listOf(CocktailIngredient(Cocktail.PLACEHOLDER_INGREDIENT_NAME, ""))
            },
            rating = generateRandomRating(),
            category = dto.category,
            glass = dto.glass,
            alcoholic = dto.alcoholic ?: "Unknown",
            dateAdded = parseDateToTimestamp(dto.dateModified),
            popularity = generateRandomPopularity()
        )
    }

    // Helper functions for demo data
    private fun generateRandomPrice(): Double {
        return (500..1500).random() / 100.0 // Random price between $5.00 and $15.00
    }

    private fun generateRandomRating(): Float {
        return (30..50).random() / 10.0f // Random rating between 3.0 and 5.0
    }

    private fun generateRandomPopularity(): Int {
        return (1..100).random() // Random popularity score between 1 and 100
    }

    private fun parseDateToTimestamp(dateStr: String?): Long {
        return try {
            // If date string is null or empty, return current timestamp
            if (dateStr.isNullOrBlank()) {
                Clock.System.now().toEpochMilliseconds()
            } else {
                // Parse the date string (format: "YYYY-MM-DD HH:mm:ss")
                // For simplicity, we'll just use current timestamp if parsing fails
                Clock.System.now().toEpochMilliseconds()
            }
        } catch (e: Exception) {
            Clock.System.now().toEpochMilliseconds()
        }
    }

    // Add a method to get consistent cocktail image URLs with fallbacks
    override fun getCocktailImageUrl(cocktail: Cocktail): String {
        // Return the direct imageUrl if available
        if (!cocktail.imageUrl.isNullOrBlank()) {
            return cocktail.imageUrl
        }

        // If no image URL, construct one from the ID if possible
        return if (cocktail.id.isNotBlank()) {
            "${appConfig.imageBaseUrl}/${appConfig.cocktailsImagePath}/${cocktail.id}.jpg"
        } else {
            // Return an empty string as fallback - UI will handle displaying a placeholder
            ""
        }
    }

    // Implement the interface method
    override suspend fun checkApiConnectivity(): Result<Boolean> {
        return try {
            Result.Success(pingApiInternal())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to check API connectivity")
        }
    }

    /**
     * Get recently viewed cocktails.
     */
    override suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> {
        return try {
            Result.Success(cocktailCache.getRecentlyViewedCocktails())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get recently viewed cocktails")
        }
    }

    /**
     * Set the offline mode preference.
     */
    override fun setOfflineMode(enabled: Boolean) {
        forceOfflineMode = enabled
    }

    /**
     * Check if offline mode is enabled.
     */
    override fun isOfflineModeEnabled(): Boolean {
        return forceOfflineMode
    }

    /**
     * Get cocktails by category for recommendations.
     */
    override suspend fun getCocktailsByCategory(category: String): Result<List<Cocktail>> {
        return try {
            if (isOffline()) {
                Result.Success(cocktailCache.getAllCachedCocktails()
                    .filter { it.category == category }
                    .take(5))
            } else {
                filterByCategory(category)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cocktails by category")
        }
    }

    /**
     * Get cocktails by ingredient for recommendations.
     */
    override suspend fun getCocktailsByIngredient(ingredient: String): Result<List<Cocktail>> {
        return try {
            if (isOffline()) {
                Result.Success(cocktailCache.getAllCachedCocktails()
                    .filter { cocktail ->
                        cocktail.ingredients.any {
                            it.name.contains(ingredient, ignoreCase = true)
                        }
                    }
                    .take(5))
            } else {
                filterByIngredient(ingredient)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cocktails by ingredient")
        }
    }

    /**
     * Get cocktails by alcoholic filter for recommendations.
     */
    override suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): Result<List<Cocktail>> {
        return try {
            if (isOffline()) {
                Result.Success(cocktailCache.getAllCachedCocktails()
                    .filter { it.alcoholic == alcoholicFilter }
                    .take(5))
            } else {
                val isAlcoholic = alcoholicFilter.equals("Alcoholic", ignoreCase = true)
                filterByAlcoholic(isAlcoholic)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cocktails by alcoholic filter")
        }
    }

    /**
     * Advanced search implementation that combines multiple filters
     */
    override suspend fun advancedSearch(filters: com.cocktailcraft.domain.model.SearchFilters): Result<List<Cocktail>> {
        return try {
            // Check if we're offline
            if (isOffline()) {
                val cachedCocktails = cocktailCache.getAllCachedCocktails()
                return Result.Success(FilterCocktailsUseCase.applyFiltersToList(cachedCocktails, filters))
            }

            // Start with a base list of cocktails - unwrap Result for internal use
            val cocktails = when {
                filters.query.isNotBlank() -> searchCocktailsByName(filters.query).getOrNull() ?: emptyList()
                filters.category != null -> filterByCategory(filters.category).getOrNull() ?: emptyList()
                filters.ingredient != null -> filterByIngredient(filters.ingredient).getOrNull() ?: emptyList()
                filters.alcoholic != null -> filterByAlcoholic(filters.alcoholic).getOrNull() ?: emptyList()
                filters.glass != null -> filterByGlass(filters.glass).getOrNull() ?: emptyList()
                else -> getCocktailsSortedByNewest().getOrNull() ?: emptyList()
            }

            // Apply all remaining filters via the use case (single source of truth)
            val filteredCocktails = FilterCocktailsUseCase.applyFiltersToList(cocktails, filters)

            // Cache the results for offline access
            filteredCocktails.forEach { cocktail ->
                cocktailCache.cacheCocktail(cocktail)
            }

            Result.Success(filteredCocktails)
        } catch (e: Exception) {
            // Try to use cache as fallback
            try {
                val cachedCocktails = cocktailCache.getAllCachedCocktails()
                val filteredCocktails = FilterCocktailsUseCase.applyFiltersToList(cachedCocktails, filters)
                Result.Success(filteredCocktails)
            } catch (cacheError: Exception) {
                Result.Error(e.message ?: "Failed to perform advanced search")
            }
        }
    }

    // Helper method to check API connectivity
    private suspend fun pingApiInternal(): Boolean {
        return try {
            // Don't ping if we're in offline mode
            if (forceOfflineMode) {
                return false
            }

            // Skip ping if we're in backoff period
            val currentBackoff = cacheManager.getRateLimitBackoffMs()
            if (currentBackoff > 0) {
                val timeSinceLastCall = Clock.System.now().toEpochMilliseconds() - cacheManager.getLastApiCallTime()
                if (timeSinceLastCall < currentBackoff) {
                    Logger.d { "In rate limit backoff period, skipping ping" }
                    return false
                }
            }

            // Check if we've made recent API calls to avoid rate limiting
            val timeSinceLastCall = Clock.System.now().toEpochMilliseconds() - cacheManager.getLastApiCallTime()
            if (timeSinceLastCall < CocktailCacheManager.MIN_API_CALL_INTERVAL_MS) {
                Logger.d { "Skipping ping to avoid rate limit (last call ${timeSinceLastCall}ms ago)" }
                return true // Assume API is reachable
            }

            cacheManager.setLastApiCallTime(Clock.System.now().toEpochMilliseconds())
            val isConnected = api.pingApi()

            // Reset backoff on successful ping
            if (isConnected) {
                cacheManager.resetBackoff()
            }

            isConnected
        } catch (e: Exception) {
            // If it's a rate limit error, consider API as reachable but throttled
            if (e.message?.contains("429") == true || e.message?.contains("Too Many Requests") == true) {
                Logger.w { "API rate limited but reachable" }
                // Apply exponential backoff
                cacheManager.applyExponentialBackoff()
                return true
            }
            false
        }
    }
}