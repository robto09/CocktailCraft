package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.model.TasteProfile
import com.cocktailcraft.domain.model.Complexity
import com.cocktailcraft.domain.model.PreparationTime
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for applying advanced filtering to cocktails.
 * This moves the filtering business logic from the repository to the domain layer.
 */
class AdvancedFilterUseCase {
    
    /**
     * Apply filters to a list of cocktails
     * @param cocktails The list of cocktails to filter
     * @param filters The search filters to apply
     * @return Filtered list of cocktails
     */
    fun applyFiltersToList(cocktails: List<Cocktail>, filters: SearchFilters): List<Cocktail> {
        var result = cocktails

        // Apply category filter if not used as primary filter
        if (filters.category != null && filters.query.isNotBlank()) {
            result = result.filter { it.category == filters.category }
        }

        // Apply ingredient filter if not used as primary filter
        if (filters.ingredient != null && (filters.query.isNotBlank() || filters.category != null)) {
            result = result.filter { cocktail ->
                cocktail.ingredients.any { it.name.contains(filters.ingredient, ignoreCase = true) }
            }
        }

        // Apply alcoholic filter if not used as primary filter
        if (filters.alcoholic != null &&
            (filters.query.isNotBlank() || filters.category != null || filters.ingredient != null)) {
            val alcoholicString = if (filters.alcoholic) "Alcoholic" else "Non_Alcoholic"
            result = result.filter { it.alcoholic == alcoholicString }
        }

        // Apply glass filter if not used as primary filter
        if (filters.glass != null &&
            (filters.query.isNotBlank() || filters.category != null ||
             filters.ingredient != null || filters.alcoholic != null)) {
            result = result.filter { it.glass == filters.glass }
        }

        // Apply price range filter
        if (filters.priceRange != null) {
            result = result.filter {
                it.price.toFloat() in filters.priceRange
            }
        }

        // Apply multiple ingredients filter
        if (filters.ingredients.isNotEmpty()) {
            result = result.filter { cocktail ->
                filters.ingredients.all { ingredient ->
                    cocktail.ingredients.any { it.name.contains(ingredient, ignoreCase = true) }
                }
            }
        }

        // Apply excluded ingredients filter
        if (filters.excludeIngredients.isNotEmpty()) {
            result = result.filter { cocktail ->
                filters.excludeIngredients.none { ingredient ->
                    cocktail.ingredients.any { it.name.contains(ingredient, ignoreCase = true) }
                }
            }
        }

        // Apply taste profile filter
        if (filters.tasteProfile != null) {
            result = result.filter { cocktail ->
                when (filters.tasteProfile) {
                    TasteProfile.SWEET ->
                        cocktail.ingredients.any {
                            it.name.contains("sugar", ignoreCase = true) ||
                            it.name.contains("syrup", ignoreCase = true) ||
                            it.name.contains("liqueur", ignoreCase = true)
                        }
                    TasteProfile.SOUR ->
                        cocktail.ingredients.any {
                            it.name.contains("lemon", ignoreCase = true) ||
                            it.name.contains("lime", ignoreCase = true) ||
                            it.name.contains("citrus", ignoreCase = true)
                        }
                    TasteProfile.BITTER ->
                        cocktail.ingredients.any {
                            it.name.contains("bitters", ignoreCase = true) ||
                            it.name.contains("campari", ignoreCase = true)
                        }
                    TasteProfile.SPICY ->
                        cocktail.ingredients.any {
                            it.name.contains("pepper", ignoreCase = true) ||
                            it.name.contains("ginger", ignoreCase = true) ||
                            it.name.contains("cinnamon", ignoreCase = true)
                        }
                    TasteProfile.FRUITY ->
                        cocktail.ingredients.any {
                            it.name.contains("fruit", ignoreCase = true) ||
                            it.name.contains("berry", ignoreCase = true) ||
                            it.name.contains("apple", ignoreCase = true) ||
                            it.name.contains("orange", ignoreCase = true) ||
                            it.name.contains("pineapple", ignoreCase = true)
                        }
                    TasteProfile.HERBAL ->
                        cocktail.ingredients.any {
                            it.name.contains("herb", ignoreCase = true) ||
                            it.name.contains("mint", ignoreCase = true) ||
                            it.name.contains("basil", ignoreCase = true) ||
                            it.name.contains("rosemary", ignoreCase = true)
                        }
                    TasteProfile.CREAMY ->
                        cocktail.ingredients.any {
                            it.name.contains("cream", ignoreCase = true) ||
                            it.name.contains("milk", ignoreCase = true) ||
                            it.name.contains("coconut", ignoreCase = true)
                        }
                    else -> true
                }
            }
        }

        // Apply complexity filter (based on number of ingredients and preparation steps)
        if (filters.complexity != null) {
            result = result.filter { cocktail ->
                val ingredientCount = cocktail.ingredients.size
                val instructionLength = cocktail.instructions?.length ?: 0

                when (filters.complexity) {
                    Complexity.EASY ->
                        ingredientCount <= 3 && instructionLength < 100
                    Complexity.MEDIUM ->
                        ingredientCount in 4..6 && instructionLength in 100..200
                    Complexity.COMPLEX ->
                        ingredientCount > 6 || instructionLength > 200
                    else -> true
                }
            }
        }

        // Apply preparation time filter (estimated based on complexity)
        if (filters.preparationTime != null) {
            result = result.filter { cocktail ->
                val ingredientCount = cocktail.ingredients.size
                val instructionLength = cocktail.instructions?.length ?: 0
                val hasComplexTechniques = cocktail.instructions?.contains("muddle", ignoreCase = true) == true ||
                                          cocktail.instructions?.contains("shake", ignoreCase = true) == true ||
                                          cocktail.instructions?.contains("stir", ignoreCase = true) == true

                when (filters.preparationTime) {
                    PreparationTime.QUICK ->
                        ingredientCount <= 3 && !hasComplexTechniques
                    PreparationTime.MEDIUM ->
                        ingredientCount in 4..6 || hasComplexTechniques
                    PreparationTime.LONG ->
                        ingredientCount > 6 || instructionLength > 300
                    else -> true
                }
            }
        }

        return result
    }
}