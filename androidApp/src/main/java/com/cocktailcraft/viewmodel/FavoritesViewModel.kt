package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the favorites screen.
 * Uses use cases instead of directly accessing repositories.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state for favorite cocktails
 * - Handles user interactions like adding/removing favorites
 * - Provides a clean API for the UI layer
 * - Implements the IFavoritesViewModel interface for cross-platform compatibility
 */
class FavoritesViewModel(
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : BaseViewModel(), IFavoritesViewModel {

    // Favorites state
    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    override val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * Load all favorite cocktails.
     */
    override fun loadFavorites() {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageFavoritesUseCase.getFavorites(),
                onSuccess = { favorites ->
                    _favorites.value = favorites
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load favorites. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadFavorites() }
            )
        }
    }

    /**
     * Add a cocktail to favorites.
     */
    override fun addToFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageFavoritesUseCase.addToFavorites(cocktail),
                onSuccess = { _ ->
                    loadFavorites() // Refresh favorites after adding
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to add favorite. Please try again."
            )
        }
    }

    /**
     * Remove a cocktail from favorites.
     */
    override fun removeFromFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageFavoritesUseCase.removeFromFavorites(cocktail),
                onSuccess = { _ ->
                    loadFavorites() // Refresh favorites after removing
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to remove favorite. Please try again."
            )
        }
    }

    /**
     * Toggle favorite status of a cocktail.
     */
    override fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageFavoritesUseCase.toggleFavorite(cocktail),
                onSuccess = { _ ->
                    loadFavorites() // Refresh favorites after toggling
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to toggle favorite. Please try again."
            )
        }
    }

    /**
     * Check if a cocktail is a favorite.
     */
    override fun isFavorite(id: String): Boolean {
        return favorites.value.any { it.id == id }
    }

    /**
     * For backward compatibility with code that uses the old API.
     */
    fun isFavorite(id: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = favorites.value.any { it.id == id }
            callback(result)
        }
    }
}
