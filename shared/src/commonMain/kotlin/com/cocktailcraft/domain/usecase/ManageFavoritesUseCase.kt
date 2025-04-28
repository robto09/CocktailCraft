package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Use case for managing favorite cocktails.
 * This use case handles the business logic of managing favorites,
 * including error handling and result transformation.
 */
class ManageFavoritesUseCase(
    private val cocktailRepository: CocktailRepository
) {
    /**
     * Get all favorite cocktails.
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun getFavorites(): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.getFavoriteCocktails()
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Check if a cocktail is a favorite.
     * @param id The cocktail ID
     * @return Flow of Result containing either a boolean or an error
     */
    suspend fun isFavorite(id: String): Flow<Result<Boolean>> {
        return cocktailRepository.isCocktailFavorite(id)
            .map { isFavorite -> Result.Success(isFavorite) as Result<Boolean> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Add a cocktail to favorites.
     * @param cocktail The cocktail to add
     * @return Flow of Result containing either a success message or an error
     */
    suspend fun addToFavorites(cocktail: Cocktail): Flow<Result<String>> = flow {
        try {
            cocktailRepository.addToFavorites(cocktail)
            emit(Result.Success("Added to favorites"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    /**
     * Remove a cocktail from favorites.
     * @param cocktail The cocktail to remove
     * @return Flow of Result containing either a success message or an error
     */
    suspend fun removeFromFavorites(cocktail: Cocktail): Flow<Result<String>> = flow {
        try {
            cocktailRepository.removeFromFavorites(cocktail)
            emit(Result.Success("Removed from favorites"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    /**
     * Toggle favorite status of a cocktail.
     * @param cocktail The cocktail to toggle
     * @return Flow of Result containing either the new favorite status or an error
     */
    suspend fun toggleFavorite(cocktail: Cocktail): Flow<Result<Boolean>> = flow {
        try {
            val isAlreadyFavorite = cocktailRepository.isCocktailFavorite(cocktail.id).first()
            
            if (isAlreadyFavorite) {
                cocktailRepository.removeFromFavorites(cocktail)
                emit(Result.Success(false)) // Now not a favorite
            } else {
                cocktailRepository.addToFavorites(cocktail)
                emit(Result.Success(true)) // Now a favorite
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
