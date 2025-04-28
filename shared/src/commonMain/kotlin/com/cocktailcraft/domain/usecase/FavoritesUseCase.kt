package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for managing favorite cocktails.
 * This moves the favorites management business logic from the repository to the domain layer.
 */
class FavoritesUseCase(
    private val cocktailRepository: CocktailRepository,
    private val offlineModeUseCase: OfflineModeUseCase
) {
    /**
     * Get all favorite cocktails
     * @return Flow of Result containing either a list of favorite cocktails or an error
     */
    suspend fun getFavoriteCocktails(): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.getFavoriteCocktails()
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Failed to get favorite cocktails"))
            }
    }

    /**
     * Add a cocktail to favorites
     * @param cocktail The cocktail to add to favorites
     * @return Flow of Result indicating success or failure
     */
    suspend fun addToFavorites(cocktail: Cocktail): Flow<Result<Unit>> = kotlinx.coroutines.flow.flow {
        try {
            emit(Result.Loading)
            cocktailRepository.addToFavorites(cocktail)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to add cocktail to favorites"))
        }
    }

    /**
     * Remove a cocktail from favorites
     * @param cocktail The cocktail to remove from favorites
     * @return Flow of Result indicating success or failure
     */
    suspend fun removeFromFavorites(cocktail: Cocktail): Flow<Result<Unit>> = kotlinx.coroutines.flow.flow {
        try {
            emit(Result.Loading)
            cocktailRepository.removeFromFavorites(cocktail)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to remove cocktail from favorites"))
        }
    }

    /**
     * Check if a cocktail is in favorites
     * @param id The ID of the cocktail to check
     * @return Flow of Result containing either a boolean indicating if the cocktail is a favorite or an error
     */
    suspend fun isCocktailFavorite(id: String): Flow<Result<Boolean>> {
        return cocktailRepository.isCocktailFavorite(id)
            .map { isFavorite -> Result.Success(isFavorite) as Result<Boolean> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Failed to check if cocktail is favorite"))
            }
    }

    /**
     * Toggle favorite status for a cocktail
     * @param cocktail The cocktail to toggle favorite status for
     * @return Flow of Result containing either the new favorite status or an error
     */
    suspend fun toggleFavorite(cocktail: Cocktail): Flow<Result<Boolean>> = kotlinx.coroutines.flow.flow {
        try {
            emit(Result.Loading)
            
            val isFavorite = cocktailRepository.isCocktailFavorite(cocktail.id).map { it }.catch { false }.first()
            
            if (isFavorite) {
                cocktailRepository.removeFromFavorites(cocktail)
                emit(Result.Success(false))
            } else {
                cocktailRepository.addToFavorites(cocktail)
                emit(Result.Success(true))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to toggle favorite status"))
        }
    }
}