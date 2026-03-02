package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result

/**
 * Repository interface for cocktail detail operations.
 * Part of the Interface Segregation split of CocktailRepository.
 */
interface CocktailDetailRepository {
    suspend fun getCocktailById(id: String): Result<Cocktail?>
    suspend fun refreshCocktailById(id: String): Result<Cocktail?>
    suspend fun getRandomCocktail(): Result<Cocktail?>
    fun getCocktailImageUrl(cocktail: Cocktail): String
}

