package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.util.ErrorHandler

/**
 * Consolidated UI state for the Favorites screen.
 */
data class FavoritesUiState(
    val favorites: List<Cocktail> = emptyList(),
    val favoriteCount: Int = 0,
    val isLoading: Boolean = false,
    val error: ErrorHandler.UserFriendlyError? = null
)

