package com.cocktailcraft.domain.recommendation

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

/**
 * Engine for generating cocktail recommendations based on various strategies.
 * This client-side implementation works within the constraints of the free TheCocktailDB API.
 */
class CocktailRecommendationEngine(
    private val cocktailRepository: CocktailRepository,
    private val favoritesRepository: FavoritesRepository
) {
    /**
     * Get recommendations for a given cocktail.
     * Uses a hybrid approach combining multiple recommendation strategies.
     *
     * @param cocktail The current cocktail to generate recommendations for
     * @param limit Maximum number of recommendations to return
     * @return List of recommended cocktails
     */
    suspend fun getRecommendations(cocktail: Cocktail, limit: Int = 3): List<Cocktail> {
        val recommendations = mutableListOf<Cocktail>()
        val excludeIds = mutableSetOf(cocktail.id)

        // Strategy 1: Category-based recommendations
        cocktail.category?.let { category ->
            val categorySimilar = getCocktailsByCategory(category, excludeIds)
            recommendations.addAll(categorySimilar)
            excludeIds.addAll(categorySimilar.map { it.id })
        }

        // Strategy 2: Ingredient-based recommendations (if we need more)
        if (recommendations.size < limit && cocktail.ingredients.isNotEmpty()) {
            val mainIngredient = cocktail.ingredients.firstOrNull()?.name
            if (!mainIngredient.isNullOrBlank()) {
                val ingredientSimilar = getCocktailsByIngredient(mainIngredient, excludeIds)
                recommendations.addAll(ingredientSimilar)
                excludeIds.addAll(ingredientSimilar.map { it.id })
            }
        }

        // Strategy 3: User preferences (favorites)
        if (recommendations.size < limit) {
            val favorites = favoritesRepository.getFavorites().first()
            val favoriteCategories = favorites
                .mapNotNull { it.category }
                .groupBy { it }
                .maxByOrNull { it.value.size }
                ?.key

            if (favoriteCategories != null && favoriteCategories != cocktail.category) {
                val favoriteCategorySimilar = getCocktailsByCategory(favoriteCategories, excludeIds)
                recommendations.addAll(favoriteCategorySimilar)
                excludeIds.addAll(favoriteCategorySimilar.map { it.id })
            }
        }

        // Strategy 4: Alcoholic/Non-alcoholic matching
        cocktail.alcoholic?.let { alcoholicFilter ->
            if (recommendations.size < limit) {
                val alcoholicSimilar = getCocktailsByAlcoholicFilter(alcoholicFilter, excludeIds)
                recommendations.addAll(alcoholicSimilar)
            }
        }

        return recommendations.take(limit)
    }

    /**
     * Get cocktails from the same category.
     */
    private suspend fun getCocktailsByCategory(
        category: String,
        excludeIds: Set<String> = emptySet(),
        limit: Int = 2
    ): List<Cocktail> {
        return cocktailRepository.getCocktailsByCategory(category)
            .filter { it.id !in excludeIds }
            .shuffled() // Add some randomness
            .take(limit)
    }

    /**
     * Get cocktails with the specified ingredient.
     */
    private suspend fun getCocktailsByIngredient(
        ingredient: String,
        excludeIds: Set<String> = emptySet(),
        limit: Int = 2
    ): List<Cocktail> {
        return cocktailRepository.getCocktailsByIngredient(ingredient)
            .filter { it.id !in excludeIds }
            .shuffled() // Add some randomness
            .take(limit)
    }

    /**
     * Get cocktails matching the alcoholic/non-alcoholic filter.
     */
    private suspend fun getCocktailsByAlcoholicFilter(
        alcoholicFilter: String,
        excludeIds: Set<String> = emptySet(),
        limit: Int = 2
    ): List<Cocktail> {
        return cocktailRepository.getCocktailsByAlcoholicFilter(alcoholicFilter)
            .filter { it.id !in excludeIds }
            .shuffled() // Add some randomness
            .take(limit)
    }
}
