package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result

/**
 * Repository interface for cocktail catalog/browsing operations.
 * Part of the Interface Segregation split of CocktailRepository.
 */
interface CocktailCatalogRepository {
    suspend fun getCategories(): Result<List<String>>
    suspend fun getGlasses(): Result<List<String>>
    suspend fun getIngredients(): Result<List<String>>
    suspend fun getAlcoholicFilters(): Result<List<String>>
    suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>>
    suspend fun getCocktailsSortedByPriceLowToHigh(): Result<List<Cocktail>>
    suspend fun getCocktailsSortedByPriceHighToLow(): Result<List<Cocktail>>
    suspend fun getCocktailsSortedByPopularity(): Result<List<Cocktail>>
    suspend fun getCocktailsByPriceRange(minPrice: Double, maxPrice: Double): Result<List<Cocktail>>
    suspend fun getCocktailsByCategory(category: String): Result<List<Cocktail>>
    suspend fun getCocktailsByIngredient(ingredient: String): Result<List<Cocktail>>
    suspend fun getCocktailsByAlcoholicFilter(alcoholicFilter: String): Result<List<Cocktail>>
}

