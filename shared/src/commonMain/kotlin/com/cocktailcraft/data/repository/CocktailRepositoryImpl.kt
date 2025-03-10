package com.cocktailcraft.data.repository

import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.repository.CocktailRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import com.cocktailcraft.domain.config.AppConfig

class CocktailRepositoryImpl(
    private val api: CocktailApi,
    private val settings: Settings,
    private val appConfig: AppConfig
) : CocktailRepository {

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
            println("REPOSITORY: Getting cocktail by ID: $id")
            // Make a direct API call to get full cocktail details
            val dto = api.getCocktailById(id)
            
            if (dto != null) {
                val cocktail = mapDtoToCocktail(dto)
                println("REPOSITORY: Successfully mapped cocktail ${dto.name}, has instructions: ${!dto.instructions.isNullOrBlank()}")
                emit(cocktail)
            } else {
                println("REPOSITORY: API returned null for cocktail ID $id")
                emit(null)
            }
        } catch (e: Exception) {
            println("REPOSITORY ERROR: Failed to get cocktail by ID $id: ${e.message}")
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
        
        for (id in favoriteIds) {
            getCocktailById(id).collect { cocktail ->
                if (cocktail != null) {
                    favoriteCocktails.add(cocktail)
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
            println("DEBUG: Attempting to load cocktails by newest...")
            // First check if API is reachable
            if (!pingApiInternal()) {
                throw Exception("API is not reachable. Please check your internet connection.")
            }
            
            val cocktails = api.filterByCategory("Cocktail").map { dto ->
                mapDtoToCocktail(dto)
            }
            println("DEBUG: Successfully loaded ${cocktails.size} cocktails")
            emit(cocktails.sortedByDescending { it.dateAdded })
        } catch (e: Exception) {
            // Log the detailed error with stack trace
            println("ERROR loading cocktails: ${e.message}")
            println("ERROR stack trace: ${e.stackTraceToString()}")
            // Re-throw with more context
            throw Exception("Failed to load cocktails: ${e.message}", e)
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
        // Debug logging for instructions field
        println("DEBUG: Mapping instructions for ${dto.name}, instructions=${dto.instructions ?: "null"}")
        
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

    // Helper method to check API connectivity
    private suspend fun pingApiInternal(): Boolean {
        println("DEBUG: Checking API connectivity...")
        return try {
            val isConnected = api.pingApi()
            println("DEBUG: API connectivity check result: $isConnected")
            isConnected
        } catch (e: Exception) {
            println("ERROR checking API connectivity: ${e.message}")
            false
        }
    }
} 