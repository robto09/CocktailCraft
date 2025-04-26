package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing favorite cocktails.
 */
interface FavoritesRepository {
    /**
     * Get all favorite cocktails.
     */
    suspend fun getFavorites(): Flow<List<Cocktail>>
    
    /**
     * Add a cocktail to favorites.
     */
    suspend fun addToFavorites(cocktail: Cocktail)
    
    /**
     * Remove a cocktail from favorites.
     */
    suspend fun removeFromFavorites(cocktail: Cocktail)
    
    /**
     * Check if a cocktail is in favorites.
     */
    suspend fun isFavorite(id: String): Flow<Boolean>
    
    /**
     * Toggle favorite status of a cocktail.
     */
    suspend fun toggleFavorite(cocktail: Cocktail)
}
