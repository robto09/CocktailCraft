package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.ErrorCode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for cocktail-related operations.
 * This interface defines all data operations related to cocktails.
 */
interface CocktailRepository {
    // Basic search operations
    suspend fun searchCocktailsByName(name: String): Flow<List<Cocktail>>
    suspend fun searchCocktailsByFirstLetter(letter: Char): Flow<List<Cocktail>>
    suspend fun getCocktailById(id: String): Flow<Cocktail?>
    suspend fun getRandomCocktail(): Flow<Cocktail?>

    // Filtering operations
    suspend fun filterByIngredient(ingredient: String): Flow<List<Cocktail>>
    suspend fun filterByAlcoholic(alcoholic: Boolean): Flow<List<Cocktail>>
    suspend fun filterByCategory(category: String): Flow<List<Cocktail>>
    suspend fun filterByGlass(glass: String): Flow<List<Cocktail>>

    // Metadata operations
    suspend fun getCategories(): Flow<List<String>>
    suspend fun getGlasses(): Flow<List<String>>
    suspend fun getIngredients(): Flow<List<String>>
    suspend fun getAlcoholicFilters(): Flow<List<String>>

    // Favorites operations
    suspend fun getFavoriteCocktails(): Flow<List<Cocktail>>
    suspend fun addToFavorites(cocktail: Cocktail)
    suspend fun removeFromFavorites(cocktail: Cocktail)
    suspend fun isCocktailFavorite(id: String): Flow<Boolean>

    // Sorting operations
    suspend fun getCocktailsSortedByNewest(): Flow<List<Cocktail>>
    suspend fun getCocktailsSortedByPriceLowToHigh(): Flow<List<Cocktail>>
    suspend fun getCocktailsSortedByPriceHighToLow(): Flow<List<Cocktail>>
    suspend fun getCocktailsSortedByPopularity(): Flow<List<Cocktail>>
    suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Cocktail>>

    // Advanced search and filtering
    suspend fun advancedSearch(filters: com.cocktailcraft.domain.model.SearchFilters): Flow<List<Cocktail>>

    // Image URL operations
    fun getCocktailImageUrl(cocktail: Cocktail): String

    // Network operations
    suspend fun checkApiConnectivity(): Flow<Boolean>

    // Offline mode operations
    suspend fun getRecentlyViewedCocktails(): Flow<List<Cocktail>>
    fun setOfflineMode(enabled: Boolean)
    fun isOfflineModeEnabled(): Boolean

    // Recommendation operations
    suspend fun getCocktailsByCategory(category: String): List<Cocktail>
    suspend fun getCocktailsByIngredient(ingredient: String): List<Cocktail>
    suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): List<Cocktail>

    // Refresh operations
    suspend fun refreshCocktailDetails(id: String): Flow<Cocktail?>
}