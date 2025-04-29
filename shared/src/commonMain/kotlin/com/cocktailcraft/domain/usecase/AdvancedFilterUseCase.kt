package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
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

        // Apply category filter
        if (filters.category != null) {
            result = result.filter { it.category == filters.category }
        }

        // Apply ingredient filter
        if (filters.ingredient != null) {
            result = result.filter { cocktail ->
                cocktail.ingredients.any { it.name.contains(filters.ingredient, ignoreCase = true) }
            }
        }

        // Apply alcoholic filter
        if (filters.alcoholic != null) {
            val alcoholicString = if (filters.alcoholic) "Alcoholic" else "Non_Alcoholic"
            result = result.filter { it.alcoholic == alcoholicString }
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

        return result
    }
}