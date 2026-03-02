package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result

/**
 * Repository interface for managing favorite cocktails.
 */
interface FavoritesRepository {
    /**
     * Get all favorite cocktails.
     */
    suspend fun getFavorites(): Result<List<Cocktail>>

    /**
     * Add a cocktail to favorites.
     */
    suspend fun addToFavorites(cocktail: Cocktail): Result<Unit>

    /**
     * Remove a cocktail from favorites.
     */
    suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit>

    /**
     * Check if a cocktail is in favorites.
     */
    suspend fun isFavorite(id: String): Result<Boolean>

    /**
     * Toggle favorite status of a cocktail.
     */
    suspend fun toggleFavorite(cocktail: Cocktail): Result<Unit>
}
