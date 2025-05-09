package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.cocktailcraft.domain.config.AppConfig
import kotlinx.serialization.json.Json

class CocktailRepositoryImpl(
    private val api: CocktailApi,
    private val settings: Settings,
    private val appConfig: AppConfig,
    private val json: Json,
    private val networkMonitor: NetworkMonitor
) : CocktailRepository {

    // Initialize the cocktail cache
    private val cocktailCache = CocktailCache(settings, json, appConfig)

    // Flag to force offline mode (user preference)
    private var forceOfflineMode: Boolean
        get() = settings.getBoolean(appConfig.offlineModeEnabledKey, false)
        set(value) = settings.putBoolean(appConfig.offlineModeEnabledKey, value)

    // Check if we're currently offline
    private suspend fun isOffline(): Boolean {
        return forceOfflineMode || !networkMonitor.isOnline.first()
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
            // First check if we're offline
            if (isOffline()) {
                // Try to get from cache
                val cachedCocktail = cocktailCache.getCachedCocktail(id)
                if (cachedCocktail != null) {
                    emit(cachedCocktail)
                } else {
                    emit(null)
                }
                return@flow
            }

            // If online, make a direct API call
            val dto = api.getCocktailById(id)

            if (dto != null) {
                val cocktail = mapDtoToCocktail(dto)

                // Cache the cocktail for offline access
                cocktailCache.cacheCocktail(cocktail)

                emit(cocktail)
            } else {
                // If API call returns null, try to get from cache as fallback
                val cachedCocktail = cocktailCache.getCachedCocktail(id)
                emit(cachedCocktail)
            }
        } catch (e: Exception) {
            // If there's an error with the API call, try to get from cache
            val cachedCocktail = cocktailCache.getCachedCocktail(id)
            if (cachedCocktail != null) {
                emit(cachedCocktail)
            } else {
                emit(null)
            }
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
        try {
            val cocktails = api.filterByCategory(category).map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails)
        } catch (e: Exception) {
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
        try {
            // Check if we're offline
            if (isOffline()) {
                // Use cached cocktails when offline
                val cachedCocktails = cocktailCache.getAllCachedCocktails()
                emit(cachedCocktails.sortedByDescending { it.dateAdded })
                return@flow
            }

            // If online, check if API is reachable
            if (!pingApiInternal()) {
                // Fall back to cache if API is not reachable
                val cachedCocktails = cocktailCache.getAllCachedCocktails()
                if (cachedCocktails.isNotEmpty()) {
                    emit(cachedCocktails.sortedByDescending { it.dateAdded })
                    return@flow
                }
                throw Exception("API is not reachable. Please check your internet connection.")
            }

            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                val cocktail = mapDtoToCocktail(dto)
                // Cache cocktails for offline access
                cocktailCache.cacheCocktail(cocktail)
                cocktail
            }
            emit(cocktails.sortedByDescending { it.dateAdded })
        } catch (e: Exception) {
            // Try to use cache as fallback
            val cachedCocktails = cocktailCache.getAllCachedCocktails()
            if (cachedCocktails.isNotEmpty()) {
                emit(cachedCocktails.sortedByDescending { it.dateAdded })
            } else {
                // Re-throw with more context if no cached data
                throw Exception("Failed to load cocktails: ${e.message}", e)
            }
        }
    }

    override suspend fun getCocktailsSortedByPriceLowToHigh(): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.sortedBy { it.price })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsSortedByPriceHighToLow(): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.sortedByDescending { it.price })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsSortedByPopularity(): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.sortedByDescending { it.popularity })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Cocktail>> = flow {
        try {
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            emit(cocktails.filter { it.price in minPrice..maxPrice })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    private fun mapDtoToCocktail(dto: CocktailDto): Cocktail {
        return Cocktail(
            id = dto.id,
            name = dto.name,
            instructions = dto.instructions ?: "",
            imageUrl = dto.imageUrl,
            price = generateRandomPrice(),
            ingredients = dto.getIngredients(),
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
            val isConnected = api.pingApi()
            isConnected
        } catch (e: Exception) {
            false
        }
    }
}