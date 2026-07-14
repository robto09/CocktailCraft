package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Cocktail

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
    val isLoading: Boolean = false
)

