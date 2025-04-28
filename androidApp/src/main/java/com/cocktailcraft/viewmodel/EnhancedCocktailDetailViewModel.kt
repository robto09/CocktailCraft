package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.GetCocktailDetailsUseCase
import com.cocktailcraft.domain.usecase.GetRecommendationsUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Enhanced ViewModel for the cocktail detail screen.
 * Uses use cases instead of directly accessing repositories.
 */
class EnhancedCocktailDetailViewModel : BaseViewModel() {

    // Use cases
    private val getCocktailDetailsUseCase: GetCocktailDetailsUseCase by inject()
    private val getRecommendationsUseCase: GetRecommendationsUseCase by inject()
    private val manageFavoritesUseCase: ManageFavoritesUseCase by inject()

    // Cocktail state
    private val _cocktail = MutableStateFlow<Cocktail?>(null)
    val cocktail: StateFlow<Cocktail?> = _cocktail.asStateFlow()

    // Favorite state
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Recommendations state
    private val _recommendations = MutableStateFlow<List<Cocktail>>(emptyList())
    val recommendations: StateFlow<List<Cocktail>> = _recommendations.asStateFlow()

    /**
     * Load cocktail details by ID.
     */
    fun loadCocktailDetails(id: String) {
        executeWithErrorHandling(
            operation = {
                getCocktailDetailsUseCase(id)
            },
            onSuccess = { result ->
                when (result) {
                    is Result.Success -> {
                        _cocktail.value = result.data
                        checkFavoriteStatus(id)
                        loadRecommendations(result.data)
                    }
                    is Result.Error -> {
                        setError(
                            title = "Failed to Load Details",
                            message = result.message,
                            category = ErrorUtils.ErrorCategory.DATA,
                            recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCocktailDetails(id) }
                        )
                    }
                    is Result.Loading -> {
                        // Already handled by executeWithErrorHandling
                    }
                }
            },
            defaultErrorMessage = "Failed to load cocktail details. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCocktailDetails(id) }
        )
    }

    /**
     * Check if the cocktail is a favorite.
     */
    private fun checkFavoriteStatus(id: String) {
        viewModelScope.launch {
            manageFavoritesUseCase.isFavorite(id).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _isFavorite.value = result.data
                    }
                    is Result.Error -> {
                        // Don't show error for favorite check
                        _isFavorite.value = false
                    }
                    is Result.Loading -> {
                        // No action needed
                    }
                }
            }
        }
    }

    /**
     * Toggle favorite status of the current cocktail.
     */
    fun toggleFavorite() {
        val currentCocktail = _cocktail.value ?: return

        viewModelScope.launch {
            manageFavoritesUseCase.toggleFavorite(currentCocktail).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _isFavorite.value = result.data
                    }
                    is Result.Error -> {
                        setError(
                            title = "Favorite Error",
                            message = "Failed to update favorite status: ${result.message}",
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
     * Load recommendations based on the current cocktail.
     */
    private fun loadRecommendations(cocktail: Cocktail) {
        viewModelScope.launch {
            // Try to get recommendations by category first
            if (!cocktail.category.isNullOrBlank()) {
                getRecommendationsUseCase.byCategory(cocktail.category, 3).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            if (result.data.isNotEmpty()) {
                                _recommendations.value = result.data.filter { it.id != cocktail.id }
                                return@collect
                            }
                        }
                        else -> { /* Continue to next approach if this fails */ }
                    }
                }
            }

            // If category recommendations failed or were empty, try by ingredient
            if (cocktail.ingredients.isNotEmpty()) {
                val mainIngredient = cocktail.ingredients.first().name
                getRecommendationsUseCase.byIngredient(mainIngredient, 3).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            if (result.data.isNotEmpty()) {
                                _recommendations.value = result.data.filter { it.id != cocktail.id }
                                return@collect
                            }
                        }
                        else -> { /* Continue to next approach if this fails */ }
                    }
                }
            }

            // If all else fails, try similar cocktails
            getRecommendationsUseCase.similarTo(cocktail, 3).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _recommendations.value = result.data
                    }
                    is Result.Error -> {
                        // Don't show error for recommendations
                        _recommendations.value = emptyList()
                    }
                    is Result.Loading -> {
                        // No action needed
                    }
                }
            }
        }
    }

    /**
     * Get a random cocktail.
     */
    fun loadRandomCocktail() {
        executeWithErrorHandling(
            operation = {
                getCocktailDetailsUseCase.random()
            },
            onSuccess = { result ->
                when (result) {
                    is Result.Success -> {
                        _cocktail.value = result.data
                        checkFavoriteStatus(result.data.id)
                        loadRecommendations(result.data)
                    }
                    is Result.Error -> {
                        setError(
                            title = "Failed to Load Random Cocktail",
                            message = result.message,
                            category = ErrorUtils.ErrorCategory.DATA,
                            recoveryAction = ErrorUtils.RecoveryAction("Try Again") { loadRandomCocktail() }
                        )
                    }
                    is Result.Loading -> {
                        // Already handled by executeWithErrorHandling
                    }
                }
            },
            defaultErrorMessage = "Failed to load random cocktail. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Try Again") { loadRandomCocktail() }
        )
    }
}