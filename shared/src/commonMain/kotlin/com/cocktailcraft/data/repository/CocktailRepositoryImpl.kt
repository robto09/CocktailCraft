package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.util.CocktailDebugLogger
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import com.cocktailcraft.domain.config.AppConfig

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
class CocktailRepositoryImpl(
    private val api: CocktailApi,
    private val settings: Settings,
    private val appConfig: AppConfig,
    private val networkMonitor: NetworkMonitor,
    private val cocktailCache: CocktailCache
) : CocktailRepository {

    // Store all fetched cocktails for pagination (use companion object for persistence)
    private var allCocktailsCache: List<Cocktail> 
        get() = globalCocktailsCache
        set(value) { globalCocktailsCache = value }
    
    private var lastFetchTime: Long
        get() = globalLastFetchTime
        set(value) { globalLastFetchTime = value }

    // Flag to force offline mode (user preference)
    private var forceOfflineMode: Boolean
        get() = settings.getBoolean(appConfig.offlineModeEnabledKey, false)
        set(value) = settings.putBoolean(appConfig.offlineModeEnabledKey, value)

    // Check if we're currently offline
    private suspend fun isOffline(): Boolean {
        val networkOnline = networkMonitor.isOnline.first()
        val result = forceOfflineMode || !networkOnline
        CocktailDebugLogger.log("   🔍 isOffline check: forceOfflineMode=$forceOfflineMode, networkOnline=$networkOnline, result=$result")
        return result
    }

    override suspend fun searchCocktailsByName(name: String): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.searchCocktailsByName(name).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun searchCocktailsByFirstLetter(letter: Char): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.searchCocktailsByFirstLetter(letter).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailById(id: String): Flow<Cocktail?> = flow {
        try {
            // First check if we have a cached cocktail with full details
            val cachedCocktail = cocktailCache.getCachedCocktail(id)
            
            // If we have a cached cocktail with ingredients (full details), return it
            if (cachedCocktail != null && 
                cachedCocktail.ingredients.isNotEmpty() && 
                cachedCocktail.ingredients.first().name != "Tap to view ingredients") {
                // Add to recently viewed when accessed from cache
                cocktailCache.addToRecentlyViewed(cachedCocktail)
                emit(cachedCocktail)
                return@flow
            }
            
            // If we're offline, return whatever we have cached
            if (isOffline()) {
                emit(cachedCocktail)
                return@flow
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
                    allCocktailsCache = allCocktailsCache.map { 
                        if (it.id == id) cocktail else it 
                    }

                    emit(cocktail)
                } else {
                    // If API returns null, use cached version
                    emit(cachedCocktail)
                }
            } catch (e: Exception) {
                // On API error, return cached version
                CocktailDebugLogger.log("Error fetching cocktail details for $id: ${e.message}")
                emit(cachedCocktail)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun getRandomCocktail(): Flow<Cocktail?> = flow {
        try {
            val dto = api.getRandomCocktail()
            if (dto != null) {
                emit(mapDtoToCocktail(dto))
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun filterByIngredient(ingredient: String): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByIngredient(ingredient).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByAlcoholic(alcoholic).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun filterByCategory(category: String): Flow<List<Cocktail>> = flow {
        CocktailDebugLogger.log("🏷️ filterByCategory() called with category: $category")
        
        // Use the new lazy loading method
        fetchCocktailsByCategory(category).collect { cocktails ->
            emit(cocktails)
        }
    }
    
    /**
     * Lazily fetch cocktails by category with caching and rate limiting
     */
    private suspend fun fetchCocktailsByCategory(category: String): Flow<List<Cocktail>> = flow {
        CocktailDebugLogger.log("🔄 fetchCocktailsByCategory() called for: $category")
        
        try {
            // First check category-specific cache
            val categoryCachedCocktails = categoryCacheMap[category] ?: emptyList()
            val categoryLastFetch = categoryLastFetchMap[category] ?: 0L
            val categoryCacheAge = System.currentTimeMillis() - categoryLastFetch
            
            CocktailDebugLogger.log("   - Category cache size: ${categoryCachedCocktails.size}")
            CocktailDebugLogger.log("   - Category cache age: ${categoryCacheAge/1000}s")
            
            // Emit cached data immediately if available
            if (categoryCachedCocktails.isNotEmpty()) {
                CocktailDebugLogger.log("   ✅ Emitting ${categoryCachedCocktails.size} cached cocktails for $category")
                emit(categoryCachedCocktails.sortedByDescending { it.dateAdded })
                
                // If cache is fresh, don't fetch from API
                if (categoryCacheAge < CACHE_VALIDITY_MS) {
                    CocktailDebugLogger.log("   ✅ Category cache is fresh, skipping API call")
                    return@flow
                }
            }
            
            // Check if we're offline
            if (isOffline()) {
                CocktailDebugLogger.log("   📴 Offline mode - using cache only")
                // Try general cache if category cache is empty
                if (categoryCachedCocktails.isEmpty()) {
                    val generalCache = cocktailCache.getAllCachedCocktails()
                        .filter { it.category == category }
                    if (generalCache.isNotEmpty()) {
                        emit(generalCache.sortedByDescending { it.dateAdded })
                    }
                }
                return@flow
            }
            
            // Apply rate limiting with exponential backoff
            val timeSinceLastCall = System.currentTimeMillis() - lastApiCallTime
            val requiredInterval = maxOf(MIN_API_CALL_INTERVAL_MS, rateLimitBackoffMs)
            
            if (timeSinceLastCall < requiredInterval) {
                val waitTime = requiredInterval - timeSinceLastCall
                CocktailDebugLogger.log("   ⏳ Rate limiting: waiting ${waitTime}ms before API call")
                delay(waitTime)
            }
            
            // Check API connectivity
            if (!pingApiInternal()) {
                CocktailDebugLogger.log("   ❌ API unreachable")
                if (categoryCachedCocktails.isNotEmpty()) {
                    return@flow
                }
                throw Exception("API is not reachable")
            }
            
            CocktailDebugLogger.log("   📡 Fetching $category cocktails from API...")
            lastApiCallTime = System.currentTimeMillis()
            
            try {
                val basicCocktails = api.filterByCategory(category)
                
                // Reset backoff on successful call
                rateLimitBackoffMs = 0
                
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
                                // If not in cache, just use basic data
                                // Full details will be fetched when user clicks on the cocktail
                                mapDtoToCocktail(basicDto)
                            }
                        }
                    } catch (e: Exception) {
                        mapDtoToCocktail(basicDto)
                    }
                }
                
                CocktailDebugLogger.log("   ✅ Fetched ${enrichedCocktails.size} cocktails for $category")
                
                // Update category cache
                categoryCacheMap[category] = enrichedCocktails
                categoryLastFetchMap[category] = System.currentTimeMillis()
                
                // Cache individual cocktails
                enrichedCocktails.forEach { cocktail ->
                    cocktailCache.cacheCocktail(cocktail)
                }
                
                emit(enrichedCocktails.sortedByDescending { it.dateAdded })
                
            } catch (e: Exception) {
                CocktailDebugLogger.log("   ❌ API call failed: ${e.message}")
                
                // Check if it's a rate limit error
                if (e.message?.contains("429") == true || e.message?.contains("Too Many Requests") == true) {
                    CocktailDebugLogger.log("   ⚠️ Rate limited - applying exponential backoff")
                    // Apply exponential backoff
                    rateLimitBackoffMs = minOf(
                        if (rateLimitBackoffMs == 0L) 2000L else rateLimitBackoffMs * 2,
                        MAX_BACKOFF_MS
                    )
                    CocktailDebugLogger.log("   ⏰ Next backoff: ${rateLimitBackoffMs}ms")
                }
                
                // If we have cached data, use it
                if (categoryCachedCocktails.isNotEmpty()) {
                    CocktailDebugLogger.log("   ✅ Using stale cache due to API error")
                    return@flow
                }
                
                // Try general cache as last resort
                val generalCache = cocktailCache.getAllCachedCocktails()
                    .filter { it.category == category }
                if (generalCache.isNotEmpty()) {
                    emit(generalCache.sortedByDescending { it.dateAdded })
                } else {
                    throw e
                }
            }
        } catch (e: Exception) {
            CocktailDebugLogger.log("   ❌ fetchCocktailsByCategory failed: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun filterByGlass(glass: String): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByGlass(glass).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCategories(): Flow<List<String>> = flow {
        try {
            val categories = api.getCategories().map { it.name }
            emit(categories)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getGlasses(): Flow<List<String>> = flow {
        try {
            val glasses = api.getGlasses().map { it.name }
            emit(glasses)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getIngredients(): Flow<List<String>> = flow {
        try {
            val ingredients = api.getIngredients().map { it.name }
            emit(ingredients)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getAlcoholicFilters(): Flow<List<String>> = flow {
        try {
            val filters = api.getAlcoholicFilters().map { it.name }
            emit(filters)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFavoriteCocktails(): Flow<List<Cocktail>> = flow {
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
            emit(favoriteCocktails)
            return@flow
        }

        // If online, get favorites from API and cache them
        for (id in favoriteIds) {
            getCocktailById(id).collect { cocktail ->
                if (cocktail != null) {
                    favoriteCocktails.add(cocktail)
                    // Ensure favorites are cached
                    cocktailCache.cacheCocktail(cocktail)
                }
            }
        }

        emit(favoriteCocktails)
    }

    override suspend fun addToFavorites(cocktail: Cocktail) {
        val currentFavorites = settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.toMutableList()
            ?: mutableListOf()

        if (!currentFavorites.contains(cocktail.id)) {
            currentFavorites.add(cocktail.id)
            settings.putString(appConfig.favoritesStorageKey, currentFavorites.joinToString(","))
        }
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail) {
        val currentFavorites = settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.toMutableList()
            ?: mutableListOf()

        currentFavorites.remove(cocktail.id)
        settings.putString(appConfig.favoritesStorageKey, currentFavorites.joinToString(","))
    }

    override suspend fun isCocktailFavorite(id: String): Flow<Boolean> = flow {
        val favoriteIds = settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        emit(favoriteIds.contains(id))
    }

    override suspend fun getCocktailsSortedByNewest(): Flow<List<Cocktail>> = flow {
        CocktailDebugLogger.log("📚 CocktailRepositoryImpl.getCocktailsSortedByNewest() called")
        CocktailDebugLogger.log("   🎯 LAZY LOADING: Only fetching 'Cocktail' category initially")
        
        // Instead of fetching all categories, just fetch "Cocktail" category
        fetchCocktailsByCategory("Cocktail").collect { cocktails ->
            emit(cocktails)
        }
    }

    override suspend fun getCocktailsSortedByPriceLowToHigh(): Flow<List<Cocktail>> = flow {
        try {
            // Use the same method as getCocktailsSortedByNewest to get all cocktails
            getCocktailsSortedByNewest().collect { allCocktails ->
                emit(allCocktails.sortedBy { it.price })
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsSortedByPriceHighToLow(): Flow<List<Cocktail>> = flow {
        try {
            // Use the same method as getCocktailsSortedByNewest to get all cocktails
            getCocktailsSortedByNewest().collect { allCocktails ->
                emit(allCocktails.sortedByDescending { it.price })
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsSortedByPopularity(): Flow<List<Cocktail>> = flow {
        try {
            // Use the same method as getCocktailsSortedByNewest to get all cocktails
            getCocktailsSortedByNewest().collect { allCocktails ->
                emit(allCocktails.sortedByDescending { it.popularity })
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Cocktail>> = flow {
        try {
            // Use the same method as getCocktailsSortedByNewest to get all cocktails
            getCocktailsSortedByNewest().collect { allCocktails ->
                emit(allCocktails.filter { it.price in minPrice..maxPrice })
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private fun mapDtoToCocktail(dto: CocktailDto): Cocktail {
        return Cocktail(
            id = dto.id,
            name = dto.name,
            instructions = dto.instructions ?: "Tap to view recipe",
            imageUrl = dto.imageUrl,
            price = generateRandomPrice(),
            ingredients = dto.getIngredients().ifEmpty { 
                // If no ingredients (from filter endpoint), add placeholder
                listOf(CocktailIngredient("Tap to view ingredients", ""))
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
                System.currentTimeMillis()
            } else {
                // Parse the date string (format: "YYYY-MM-DD HH:mm:ss")
                // For simplicity, we'll just use current timestamp if parsing fails
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
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
    override suspend fun checkApiConnectivity(): Flow<Boolean> = flow {
        emit(pingApiInternal())
    }

    /**
     * Get recently viewed cocktails.
     */
    override suspend fun getRecentlyViewedCocktails(): Flow<List<Cocktail>> = flow {
        val recentlyViewedCocktails = cocktailCache.getRecentlyViewedCocktails()
        emit(recentlyViewedCocktails)
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
     * This implementation uses the filterByCategory flow and collects the first result.
     */
    override suspend fun getCocktailsByCategory(category: String): List<Cocktail> {
        return try {
            // Check if we're offline
            if (isOffline()) {
                // Use cached cocktails when offline
                cocktailCache.getAllCachedCocktails()
                    .filter { it.category == category }
                    .take(5)
            } else {
                filterByCategory(category).first()
            }
        } catch (e: Exception) {
            // Return empty list on error
            emptyList()
        }
    }

    /**
     * Get cocktails by ingredient for recommendations.
     * This implementation uses the filterByIngredient flow and collects the first result.
     */
    override suspend fun getCocktailsByIngredient(ingredient: String): List<Cocktail> {
        return try {
            // Check if we're offline
            if (isOffline()) {
                // Use cached cocktails when offline
                cocktailCache.getAllCachedCocktails()
                    .filter { cocktail ->
                        cocktail.ingredients.any {
                            it.name.contains(ingredient, ignoreCase = true)
                        }
                    }
                    .take(5)
            } else {
                filterByIngredient(ingredient).first()
            }
        } catch (e: Exception) {
            // Return empty list on error
            emptyList()
        }
    }

    /**
     * Get cocktails by alcoholic filter for recommendations.
     */
    override suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): List<Cocktail> {
        return try {
            // Check if we're offline
            if (isOffline()) {
                // Use cached cocktails when offline
                cocktailCache.getAllCachedCocktails()
                    .filter { it.alcoholic == alcoholicFilter }
                    .take(5)
            } else {
                // Use the existing filter method but convert boolean to string filter
                val isAlcoholic = alcoholicFilter.equals("Alcoholic", ignoreCase = true)
                filterByAlcoholic(isAlcoholic).first()
            }
        } catch (e: Exception) {
            // Return empty list on error
            emptyList()
        }
    }

    /**
     * Advanced search implementation that combines multiple filters
     */
    override suspend fun advancedSearch(filters: com.cocktailcraft.domain.model.SearchFilters): Flow<List<Cocktail>> = flow {
        try {
            // Check if we're offline
            if (isOffline()) {
                // Use cached cocktails when offline
                val cachedCocktails = cocktailCache.getAllCachedCocktails()
                val filteredCocktails = applyFiltersToList(cachedCocktails, filters)
                emit(filteredCocktails)
                return@flow
            }

            // Start with a base list of cocktails
            val cocktails = when {
                // If we have a text query, start with that
                filters.query.isNotBlank() ->
                    searchCocktailsByName(filters.query).first()

                // If we have a category, start with that
                filters.category != null ->
                    filterByCategory(filters.category).first()

                // If we have an ingredient, start with that
                filters.ingredient != null ->
                    filterByIngredient(filters.ingredient).first()

                // If we have alcoholic filter, start with that
                filters.alcoholic != null ->
                    filterByAlcoholic(filters.alcoholic).first()

                // If we have glass filter, start with that
                filters.glass != null ->
                    filterByGlass(filters.glass).first()

                // If no primary filter, get all cocktails
                else ->
                    getCocktailsSortedByNewest().first()
            }

            // Apply all remaining filters to the result
            val filteredCocktails = applyFiltersToList(cocktails, filters)

            // Cache the results for offline access
            filteredCocktails.forEach { cocktail ->
                cocktailCache.cacheCocktail(cocktail)
            }

            emit(filteredCocktails)
        } catch (e: Exception) {
            // Try to use cache as fallback
            val cachedCocktails = cocktailCache.getAllCachedCocktails()
            val filteredCocktails = applyFiltersToList(cachedCocktails, filters)

            if (filteredCocktails.isNotEmpty()) {
                emit(filteredCocktails)
            } else {
                emit(emptyList())
            }
        }
    }

    /**
     * Helper method to apply filters to a list of cocktails
     */
    private fun applyFiltersToList(cocktails: List<Cocktail>, filters: com.cocktailcraft.domain.model.SearchFilters): List<Cocktail> {
        var result = cocktails

        // Apply category filter if not used as primary filter
        if (filters.category != null && filters.query.isNotBlank()) {
            result = result.filter { it.category == filters.category }
        }

        // Apply ingredient filter if not used as primary filter
        if (filters.ingredient != null && (filters.query.isNotBlank() || filters.category != null)) {
            result = result.filter { cocktail ->
                cocktail.ingredients.any { it.name.contains(filters.ingredient, ignoreCase = true) }
            }
        }

        // Apply alcoholic filter if not used as primary filter
        if (filters.alcoholic != null &&
            (filters.query.isNotBlank() || filters.category != null || filters.ingredient != null)) {
            val alcoholicString = if (filters.alcoholic) "Alcoholic" else "Non_Alcoholic"
            result = result.filter { it.alcoholic == alcoholicString }
        }

        // Apply glass filter if not used as primary filter
        if (filters.glass != null &&
            (filters.query.isNotBlank() || filters.category != null ||
             filters.ingredient != null || filters.alcoholic != null)) {
            result = result.filter { it.glass == filters.glass }
        }

        // Apply price range filter
        if (filters.priceRange != null) {
            result = result.filter {
                it.price.toFloat() in filters.priceRange
            }
        }

        // Apply multiple ingredients filter
        if (filters.ingredients.isNotEmpty()) {
            result = result.filter { cocktail ->
                filters.ingredients.all { ingredient ->
                    cocktail.ingredients.any { it.name.contains(ingredient, ignoreCase = true) }
                }
            }
        }

        // Apply excluded ingredients filter
        if (filters.excludeIngredients.isNotEmpty()) {
            result = result.filter { cocktail ->
                filters.excludeIngredients.none { ingredient ->
                    cocktail.ingredients.any { it.name.contains(ingredient, ignoreCase = true) }
                }
            }
        }

        // Apply taste profile filter (this is more complex and would require ingredient analysis)
        // For now, we'll use a simplified approach based on common ingredients
        if (filters.tasteProfile != null) {
            result = result.filter { cocktail ->
                when (filters.tasteProfile) {
                    com.cocktailcraft.domain.model.TasteProfile.SWEET ->
                        cocktail.ingredients.any {
                            it.name.contains("sugar", ignoreCase = true) ||
                            it.name.contains("syrup", ignoreCase = true) ||
                            it.name.contains("liqueur", ignoreCase = true)
                        }
                    com.cocktailcraft.domain.model.TasteProfile.SOUR ->
                        cocktail.ingredients.any {
                            it.name.contains("lemon", ignoreCase = true) ||
                            it.name.contains("lime", ignoreCase = true) ||
                            it.name.contains("citrus", ignoreCase = true)
                        }
                    com.cocktailcraft.domain.model.TasteProfile.BITTER ->
                        cocktail.ingredients.any {
                            it.name.contains("bitters", ignoreCase = true) ||
                            it.name.contains("campari", ignoreCase = true)
                        }
                    com.cocktailcraft.domain.model.TasteProfile.SPICY ->
                        cocktail.ingredients.any {
                            it.name.contains("pepper", ignoreCase = true) ||
                            it.name.contains("ginger", ignoreCase = true) ||
                            it.name.contains("cinnamon", ignoreCase = true)
                        }
                    com.cocktailcraft.domain.model.TasteProfile.FRUITY ->
                        cocktail.ingredients.any {
                            it.name.contains("fruit", ignoreCase = true) ||
                            it.name.contains("berry", ignoreCase = true) ||
                            it.name.contains("apple", ignoreCase = true) ||
                            it.name.contains("orange", ignoreCase = true) ||
                            it.name.contains("pineapple", ignoreCase = true)
                        }
                    com.cocktailcraft.domain.model.TasteProfile.HERBAL ->
                        cocktail.ingredients.any {
                            it.name.contains("herb", ignoreCase = true) ||
                            it.name.contains("mint", ignoreCase = true) ||
                            it.name.contains("basil", ignoreCase = true) ||
                            it.name.contains("rosemary", ignoreCase = true)
                        }
                    com.cocktailcraft.domain.model.TasteProfile.CREAMY ->
                        cocktail.ingredients.any {
                            it.name.contains("cream", ignoreCase = true) ||
                            it.name.contains("milk", ignoreCase = true) ||
                            it.name.contains("coconut", ignoreCase = true)
                        }
                    else -> true
                }
            }
        }

        // Apply complexity filter (based on number of ingredients and preparation steps)
        if (filters.complexity != null) {
            result = result.filter { cocktail ->
                val ingredientCount = cocktail.ingredients.size
                val instructionLength = cocktail.instructions?.length ?: 0

                when (filters.complexity) {
                    com.cocktailcraft.domain.model.Complexity.EASY ->
                        ingredientCount <= 3 && instructionLength < 100
                    com.cocktailcraft.domain.model.Complexity.MEDIUM ->
                        ingredientCount in 4..6 && instructionLength in 100..200
                    com.cocktailcraft.domain.model.Complexity.COMPLEX ->
                        ingredientCount > 6 || instructionLength > 200
                    else -> true
                }
            }
        }

        // Apply preparation time filter (estimated based on complexity)
        if (filters.preparationTime != null) {
            result = result.filter { cocktail ->
                val ingredientCount = cocktail.ingredients.size
                val instructionLength = cocktail.instructions?.length ?: 0
                val hasComplexTechniques = cocktail.instructions?.contains("muddle", ignoreCase = true) == true ||
                                          cocktail.instructions?.contains("shake", ignoreCase = true) == true ||
                                          cocktail.instructions?.contains("stir", ignoreCase = true) == true

                when (filters.preparationTime) {
                    com.cocktailcraft.domain.model.PreparationTime.QUICK ->
                        ingredientCount <= 3 && !hasComplexTechniques
                    com.cocktailcraft.domain.model.PreparationTime.MEDIUM ->
                        ingredientCount in 4..6 || hasComplexTechniques
                    com.cocktailcraft.domain.model.PreparationTime.LONG ->
                        ingredientCount > 6 || instructionLength > 300
                    else -> true
                }
            }
        }

        return result
    }

    // Helper method to check API connectivity
    private suspend fun pingApiInternal(): Boolean {
        return try {
            // Don't ping if we're in offline mode
            if (forceOfflineMode) {
                return false
            }
            
            // Skip ping if we're in backoff period
            if (rateLimitBackoffMs > 0) {
                val timeSinceLastCall = System.currentTimeMillis() - lastApiCallTime
                if (timeSinceLastCall < rateLimitBackoffMs) {
                    CocktailDebugLogger.log("   ⏳ In rate limit backoff period, skipping ping")
                    return false
                }
            }
            
            // Check if we've made recent API calls to avoid rate limiting
            val timeSinceLastCall = System.currentTimeMillis() - lastApiCallTime
            if (timeSinceLastCall < MIN_API_CALL_INTERVAL_MS) {
                CocktailDebugLogger.log("   ⏳ Skipping ping to avoid rate limit (last call ${timeSinceLastCall}ms ago)")
                return true // Assume API is reachable
            }
            
            lastApiCallTime = System.currentTimeMillis()
            val isConnected = api.pingApi()
            
            // Reset backoff on successful ping
            if (isConnected) {
                rateLimitBackoffMs = 0
            }
            
            isConnected
        } catch (e: Exception) {
            // If it's a rate limit error, consider API as reachable but throttled
            if (e.message?.contains("429") == true || e.message?.contains("Too Many Requests") == true) {
                CocktailDebugLogger.log("   ⚠️ API rate limited but reachable")
                // Apply exponential backoff
                rateLimitBackoffMs = minOf(
                    if (rateLimitBackoffMs == 0L) 2000L else rateLimitBackoffMs * 2,
                    MAX_BACKOFF_MS
                )
                return true
            }
            false
        }
    }
    
    companion object {
        // Global cache to persist across repository instances
        private var globalCocktailsCache: List<Cocktail> = emptyList()
        private var globalLastFetchTime: Long = 0
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000 // 5 minutes
        
        // Category-specific cache
        private val categoryCacheMap = mutableMapOf<String, List<Cocktail>>()
        private val categoryLastFetchMap = mutableMapOf<String, Long>()
        
        // Rate limit tracking
        private var lastApiCallTime: Long = 0
        private var rateLimitBackoffMs: Long = 0
        private const val MIN_API_CALL_INTERVAL_MS = 1000L // 1 second between calls
        private const val MAX_BACKOFF_MS = 30000L // Max 30 seconds backoff
    }
}