package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Cocktail

/**
 * Consolidated UI state for the Cocktail Detail screen.
 */
data class DetailUiState(
    val cocktail: Cocktail? = null,
    val isFavorite: Boolean = false,
    val relatedCocktails: List<Cocktail> = emptyList(),
    val isLoading: Boolean = false,
    // An add-to-cart from this screen is in flight (the button shows busy)
    val isAddingToCart: Boolean = false
) {
    /**
     * Plain-text share payload for the loaded cocktail ("" until loaded).
     * Built line by line: a trimIndent() template can't trim once the
     * multi-line ingredient list is interpolated (those lines have no
     * indent), which left every template line indented in shared text.
     */
    val shareableText: String
        get() {
            val cocktail = cocktail ?: return ""
            return buildString {
                appendLine("🍹 ${cocktail.name}")
                appendLine()
                appendLine("Ingredients:")
                cocktail.ingredients.forEach { appendLine("• ${it.displayText}") }
                cocktail.instructions?.let {
                    appendLine()
                    appendLine("Instructions:")
                    appendLine(it)
                }
                appendLine()
                cocktail.glass?.let { appendLine("Glass: $it") }
                cocktail.category?.let { appendLine("Category: $it") }
            }.trimEnd()
        }
}
