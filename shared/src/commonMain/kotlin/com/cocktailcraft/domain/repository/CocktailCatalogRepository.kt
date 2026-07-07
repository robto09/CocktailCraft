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
    suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>>
}

