package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.util.Result

/**
 * Repository interface for cocktail search operations.
 * Part of the Interface Segregation split of CocktailRepository.
 */
interface CocktailSearchRepository {
    suspend fun searchCocktailsByName(name: String): Result<List<Cocktail>>
    suspend fun advancedSearch(filters: SearchFilters): Result<List<Cocktail>>
    suspend fun filterByIngredient(ingredient: String): Result<List<Cocktail>>
    suspend fun filterByAlcoholic(alcoholic: Boolean): Result<List<Cocktail>>
    suspend fun filterByCategory(category: String): Result<List<Cocktail>>
}

