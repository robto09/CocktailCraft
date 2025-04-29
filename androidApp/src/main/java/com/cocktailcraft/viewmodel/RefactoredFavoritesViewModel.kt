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
import org.koin.core.component.inject

/**
 * Refactored ViewModel for the favorites screen.
 * Uses use cases instead of directly accessing repositories.
 */
class RefactoredFavoritesViewModel : BaseViewModel() {

    // Use cases
    private val manageFavoritesUseCase: ManageFavoritesUseCase by inject()

    // Favorites state
    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())
    val favorites: StateFlow<List<Cocktail>> = _favorites.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * Load all favorite cocktails.
     */
    fun loadFavorites() {
        executeWithErrorHandling(
            operation = {
                manageFavoritesUseCase.getFavorites()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success<List<Cocktail>> -> {
                                _favorites.value = result.data
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Load Favorites",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA,
                                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadFavorites() }
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to load favorites. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadFavorites() }
        )
    }

    /**
     * Add a cocktail to favorites.
     */
    fun addToFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            manageFavoritesUseCase.addToFavorites(cocktail).collect { result ->
                when (result) {
                    is Result.Success -> {
                        loadFavorites() // Refresh favorites after adding
                    }
                    is Result.Error -> {
                        setError(
                            title = "Failed to Add Favorite",
                            message = result.message,
                            category = ErrorUtils.ErrorCategory.DATA
                        )
                    }
                    is Result.Loading -> {
                        // No action needed
                    }
                }
            }
        }
    }

    /**
     * Remove a cocktail from favorites.
     */
    fun removeFromFavorites(cocktail: Cocktail) {
        viewModelScope.launch {
            manageFavoritesUseCase.removeFromFavorites(cocktail).collect { result ->
                when (result) {
                    is Result.Success -> {
                        loadFavorites() // Refresh favorites after removing
                    }
                    is Result.Error -> {
                        setError(
                            title = "Failed to Remove Favorite",
                            message = result.message,
                            category = ErrorUtils.ErrorCategory.DATA
                        )
                    }
                    is Result.Loading -> {
                        // No action needed
                    }
                }
            }
        }
    }

    /**
     * Toggle favorite status of a cocktail.
     */
    fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            manageFavoritesUseCase.toggleFavorite(cocktail).collect { result ->
                when (result) {
                    is Result.Success -> {
                        loadFavorites() // Refresh favorites after toggling
                    }
                    is Result.Error -> {
                        setError(
                            title = "Failed to Toggle Favorite",
                            message = result.message,
                            category = ErrorUtils.ErrorCategory.DATA
                        )
                    }
                    is Result.Loading -> {
                        // No action needed
                    }
                }
            }
        }
    }

    /**
     * Check if a cocktail is a favorite.
     */
    fun isFavorite(id: String): Boolean {
        return favorites.value.any { it.id == id }
    }
}
