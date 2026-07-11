package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Cocktail

/**
 * Consolidated UI state for the Favorites screen.
 */
data class FavoritesUiState(
    val favorites: List<Cocktail> = emptyList(),
    val favoriteCount: Int = 0,
    val isLoading: Boolean = false
)

