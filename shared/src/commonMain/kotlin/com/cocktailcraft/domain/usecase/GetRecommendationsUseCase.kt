package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for retrieving cocktail recommendations.
 * This use case handles the business logic of generating recommendations,
 * including error handling and result transformation.
 */
class GetRecommendationsUseCase(
    private val cocktailRepository: CocktailRepository
) {
    /**
     * Get cocktail recommendations based on category.
     * @param category The category to base recommendations on
     * @param limit The maximum number of recommendations to return
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun byCategory(category: String, limit: Int = 3): Flow<Result<List<Cocktail>>> = flow {
        try {
            val recommendations = cocktailRepository.getCocktailsByCategory(category)
                .take(limit)
            
            emit(Result.Success(recommendations))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    /**
     * Get cocktail recommendations based on ingredient.
     * @param ingredient The ingredient to base recommendations on
     * @param limit The maximum number of recommendations to return
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun byIngredient(ingredient: String, limit: Int = 3): Flow<Result<List<Cocktail>>> = flow {
        try {
            val recommendations = cocktailRepository.getCocktailsByIngredient(ingredient)
                .take(limit)
            
            emit(Result.Success(recommendations))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    /**
     * Get cocktail recommendations based on alcoholic filter.
     * @param alcoholicFilter The alcoholic filter to base recommendations on
     * @param limit The maximum number of recommendations to return
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun byAlcoholicFilter(alcoholicFilter: String, limit: Int = 3): Flow<Result<List<Cocktail>>> = flow {
        try {
            val recommendations = cocktailRepository.getCocktailsByAlcoholicFilter(alcoholicFilter)
                .take(limit)
            
            emit(Result.Success(recommendations))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
    
    /**
     * Get similar cocktails based on a given cocktail.
     * @param cocktail The cocktail to find similar ones for
     * @param limit The maximum number of recommendations to return
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun similarTo(cocktail: Cocktail, limit: Int = 3): Flow<Result<List<Cocktail>>> = flow {
        try {
            // First try by category
            var recommendations = cocktailRepository.getCocktailsByCategory(cocktail.category ?: "")
                .filter { it.id != cocktail.id } // Exclude the original cocktail
                .take(limit)
            
            // If we don't have enough, try by ingredient
            if (recommendations.size < limit && cocktail.ingredients.isNotEmpty()) {
                val mainIngredient = cocktail.ingredients.first().name
                val byIngredient = cocktailRepository.getCocktailsByIngredient(mainIngredient)
                    .filter { it.id != cocktail.id } // Exclude the original cocktail
                
                // Combine and deduplicate
                recommendations = (recommendations + byIngredient)
                    .distinctBy { it.id }
                    .take(limit)
            }
            
            // If we still don't have enough, try by alcoholic filter
            if (recommendations.size < limit) {
                val byAlcoholic = cocktailRepository.getCocktailsByAlcoholicFilter(cocktail.alcoholic)
                    .filter { it.id != cocktail.id } // Exclude the original cocktail
                
                // Combine and deduplicate
                recommendations = (recommendations + byAlcoholic)
                    .distinctBy { it.id }
                    .take(limit)
            }
            
            emit(Result.Success(recommendations))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
