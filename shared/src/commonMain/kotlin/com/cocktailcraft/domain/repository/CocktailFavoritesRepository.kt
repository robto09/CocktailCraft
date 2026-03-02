package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result

/**
 * Repository interface for cocktail favorites operations.
 * Part of the Interface Segregation split of CocktailRepository.
 */
interface CocktailFavoritesRepository {
    suspend fun getFavoriteCocktails(): Result<List<Cocktail>>
    suspend fun addToFavorites(cocktail: Cocktail): Result<Unit>
    suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit>
    suspend fun isCocktailFavorite(id: String): Result<Boolean>
}

