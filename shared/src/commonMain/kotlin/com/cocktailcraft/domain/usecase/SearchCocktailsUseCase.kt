package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for searching cocktails by various criteria.
 * This use case handles the business logic of searching cocktails,
 * including error handling and result transformation.
 */
class SearchCocktailsUseCase(
    private val cocktailRepository: CocktailRepository
) {
    /**
     * Search cocktails by name.
     * @param query The search query
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend operator fun invoke(query: String): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.searchCocktailsByName(query)
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Search cocktails using advanced filters.
     * @param filters The search filters to apply
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun advanced(filters: SearchFilters): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.advancedSearch(filters)
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Search cocktails by first letter.
     * @param letter The first letter to search by
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun byFirstLetter(letter: Char): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.searchCocktailsByFirstLetter(letter)
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
}
