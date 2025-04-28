package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving cocktail details.
 * This use case handles the business logic of fetching cocktail details,
 * including error handling and result transformation.
 */
class GetCocktailDetailsUseCase(
    private val cocktailRepository: CocktailRepository
) {
    /**
     * Get cocktail details by ID.
     * @param id The cocktail ID
     * @return Flow of Result containing either a cocktail or an error
     */
    suspend operator fun invoke(id: String): Flow<Result<Cocktail>> {
        return cocktailRepository.getCocktailById(id)
            .map { cocktail -> 
                if (cocktail != null) {
                    Result.Success(cocktail) 
                } else {
                    Result.Error("Cocktail not found")
                }
            }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Get a random cocktail.
     * @return Flow of Result containing either a cocktail or an error
     */
    suspend fun random(): Flow<Result<Cocktail>> {
        return cocktailRepository.getRandomCocktail()
            .map { cocktail -> 
                if (cocktail != null) {
                    Result.Success(cocktail) 
                } else {
                    Result.Error("Failed to get random cocktail")
                }
            }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Get recently viewed cocktails.
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun recentlyViewed(): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.getRecentlyViewedCocktails()
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
}
