package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving cocktails sorted by newest first.
 * This use case handles the business logic of fetching cocktails,
 * including error handling and result transformation.
 */
class GetCocktailsUseCase(
    private val cocktailRepository: CocktailRepository
) {
    /**
     * Get cocktails sorted by newest first.
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend operator fun invoke(): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.getCocktailsSortedByNewest()
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Get cocktails filtered by category.
     * @param category The category to filter by
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun byCategory(category: String): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.filterByCategory(category)
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Get cocktails filtered by ingredient.
     * @param ingredient The ingredient to filter by
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun byIngredient(ingredient: String): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.filterByIngredient(ingredient)
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Get cocktails sorted by price.
     * @param ascending Whether to sort in ascending order
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun sortedByPrice(ascending: Boolean): Flow<Result<List<Cocktail>>> {
        return if (ascending) {
            cocktailRepository.getCocktailsSortedByPriceLowToHigh()
        } else {
            cocktailRepository.getCocktailsSortedByPriceHighToLow()
        }.map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
        .catch { e -> 
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    /**
     * Get cocktails sorted by popularity.
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun sortedByPopularity(): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.getCocktailsSortedByPopularity()
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
}
