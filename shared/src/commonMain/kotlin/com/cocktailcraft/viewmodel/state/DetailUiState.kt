package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.NutritionFacts

/**
 * Consolidated UI state for the Cocktail Detail screen.
 */
data class DetailUiState(
    val cocktail: Cocktail? = null,
    val isFavorite: Boolean = false,
    val isInCart: Boolean = false,
    val cartQuantity: Int = 0,
    val relatedCocktails: List<Cocktail> = emptyList(),
    val ingredientsByType: Map<String, List<String>> = emptyMap(),
    // Estimated together with ingredientsByType whenever the cocktail changes
    val nutritionFacts: NutritionFacts? = null,
    val isLoading: Boolean = false
) {
    /** Plain-text share payload for the loaded cocktail ("" until loaded). */
    val shareableText: String
        get() {
            val cocktail = cocktail ?: return ""
            val ingredients = cocktail.ingredients.joinToString("\n") {
                "• ${it.measure} ${it.name}".trim()
            }
            return """
            🍹 ${cocktail.name}

            Ingredients:
            $ingredients

            Instructions:
            ${cocktail.instructions}

            Glass: ${cocktail.glass}
            Category: ${cocktail.category}
            """.trimIndent()
        }
}
