package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import kotlinx.coroutines.flow.Flow

interface CocktailRepository {
    suspend fun searchCocktailsByName(name: String): Flow<List<Cocktail>>
    suspend fun searchCocktailsByFirstLetter(letter: Char): Flow<List<Cocktail>>
    suspend fun getCocktailById(id: String): Flow<Cocktail?>
    suspend fun getRandomCocktail(): Flow<Cocktail?>
    suspend fun filterByIngredient(ingredient: String): Flow<List<Cocktail>>
    suspend fun filterByAlcoholic(alcoholic: Boolean): Flow<List<Cocktail>>
    suspend fun filterByCategory(category: String): Flow<List<Cocktail>>
    suspend fun filterByGlass(glass: String): Flow<List<Cocktail>>
    suspend fun getCategories(): Flow<List<String>>
    suspend fun getGlasses(): Flow<List<String>>
    suspend fun getIngredients(): Flow<List<String>>
    suspend fun getAlcoholicFilters(): Flow<List<String>>
    suspend fun getFavoriteCocktails(): Flow<List<Cocktail>>
    suspend fun addToFavorites(cocktail: Cocktail)
    suspend fun removeFromFavorites(cocktail: Cocktail)
    suspend fun isCocktailFavorite(id: String): Flow<Boolean>
    suspend fun getCocktailsSortedByNewest(): Flow<List<Cocktail>>
    suspend fun getCocktailsSortedByPriceLowToHigh(): Flow<List<Cocktail>>
    suspend fun getCocktailsSortedByPriceHighToLow(): Flow<List<Cocktail>>
    suspend fun getCocktailsSortedByPopularity(): Flow<List<Cocktail>>
    suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Cocktail>>
    
    // New method for getting image URL
    fun getCocktailImageUrl(cocktail: Cocktail): String
    
    // New method to check API connectivity
    suspend fun checkApiConnectivity(): Flow<Boolean>
} 