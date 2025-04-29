package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for retrieving filter options for cocktails.
 * This use case handles the business logic of fetching filter options,
 * including error handling and result transformation.
 */
class GetFilterOptionsUseCase(
    private val cocktailRepository: CocktailRepository
) {
    /**
     * Get all available categories.
     * @return Flow of Result containing either a list of categories or an error
     */
    suspend fun getCategories(): Flow<Result<List<String>>> {
        return cocktailRepository.getCategories()
            .map<List<String>, Result<List<String>>> { Result.Success(it) }
            .catch { e ->
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Get all available ingredients.
     * @return Flow of Result containing either a list of ingredients or an error
     */
    suspend fun getIngredients(): Flow<Result<List<String>>> {
        return cocktailRepository.getIngredients()
            .map<List<String>, Result<List<String>>> { Result.Success(it) }
            .catch { e ->
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Get all available alcoholic filters.
     * @return Flow of Result containing either a list of alcoholic filters or an error
     */
    suspend fun getAlcoholicFilters(): Flow<Result<List<String>>> {
        return cocktailRepository.getAlcoholicFilters()
            .map<List<String>, Result<List<String>>> { Result.Success(it) }
            .catch { e ->
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
}
