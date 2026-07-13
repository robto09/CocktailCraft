package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for cocktail favorites operations.
 * Part of the Interface Segregation split of CocktailRepository.
 */
interface CocktailFavoritesRepository {
    /**
     * Hot stream of resolved favorite cocktails, seeded on first collection
     * and re-published after every mutation (SH-4) — mirrors
     * [CartRepository.observeCartItems].
     */
    fun observeFavorites(): Flow<List<Cocktail>>

    suspend fun getFavoriteCocktails(): Result<List<Cocktail>>
    suspend fun addToFavorites(cocktail: Cocktail): Result<Unit>
    suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit>
    suspend fun isCocktailFavorite(id: String): Result<Boolean>
}

