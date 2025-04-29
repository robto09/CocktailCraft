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

/**
 * ViewModel for the cocktail detail screen.
 * Uses use cases instead of directly accessing repositories.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state for cocktail details, favorites, and recommendations
 * - Handles user interactions like toggling favorites
 * - Provides a clean API for the UI layer
 * - Implements the ICocktailDetailViewModel interface for cross-platform compatibility
 */
class CocktailDetailViewModel(
    private val getCocktailDetailsUseCase: GetCocktailDetailsUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase
) : BaseViewModel(), ICocktailDetailViewModel {

    // Cocktail state
    private val _cocktail = MutableStateFlow<Cocktail?>(null)
    override val cocktail: StateFlow<Cocktail?> = _cocktail.asStateFlow()

    // Favorite state
    private val _isFavorite = MutableStateFlow(false)
    override val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Recommendations state
    private val _recommendations = MutableStateFlow<List<Cocktail>>(emptyList())
    override val recommendations: StateFlow<List<Cocktail>> = _recommendations.asStateFlow()

    /**
     * Load cocktail details by ID.
     */
    override fun loadCocktailDetails(id: String) {
        executeWithErrorHandling(
            operation = {
                getCocktailDetailsUseCase(id)
            },
            onSuccess = { resultFlow ->
                handleResultFlow(
                    flow = resultFlow,
                    onSuccess = { cocktail ->
                        _cocktail.value = cocktail
                        checkFavoriteStatus(id)
                        loadRecommendations(cocktail)
                    },
                    defaultErrorMessage = "Failed to load cocktail details. Please try again.",
                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCocktailDetails(id) }
                )
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
            handleResultFlow(
                flow = manageFavoritesUseCase.isFavorite(id),
                onSuccess = { isFavorite ->
                    _isFavorite.value = isFavorite
                },
                onError = { _ ->
                    // Don't show error for favorite check
                    _isFavorite.value = false
                },
                defaultErrorMessage = "Failed to check favorite status",
                showAsEvent = false,
                showLoading = false
            )
        }
    }

    /**
     * Toggle favorite status of the current cocktail.
     */
    override fun toggleFavorite() {
        val currentCocktail = _cocktail.value ?: return

        viewModelScope.launch {
            handleResultFlow(
                flow = manageFavoritesUseCase.toggleFavorite(currentCocktail),
                onSuccess = { isFavorite ->
                    _isFavorite.value = isFavorite
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to update favorite status",
                showAsEvent = true
            )
        }
    }

    /**
     * Load recommendations based on the current cocktail.
     */
    private fun loadRecommendations(cocktail: Cocktail) {
        viewModelScope.launch {
            try {
                var recommendationsFound = false
                
                // Try to get recommendations by category first
                val category = cocktail.category
                if (!recommendationsFound && category != null && category.isNotBlank()) {
                    handleResultFlow(
                        flow = getRecommendationsUseCase.byCategory(category, 3),
                        onSuccess = { recommendations ->
                            if (recommendations.isNotEmpty()) {
                                _recommendations.value = recommendations.filter { it.id != cocktail.id }
                                recommendationsFound = true
                            }
                        },
                        onError = { /* Continue to next approach if this fails */ },
                        defaultErrorMessage = "",
                        showAsEvent = false,
                        showLoading = false
                    )
                }

                // If category recommendations failed or were empty, try by ingredient
                if (!recommendationsFound && cocktail.ingredients.isNotEmpty()) {
                    val mainIngredient = cocktail.ingredients.first().name
                    handleResultFlow(
                        flow = getRecommendationsUseCase.byIngredient(mainIngredient, 3),
                        onSuccess = { recommendations ->
                            if (recommendations.isNotEmpty()) {
                                _recommendations.value = recommendations.filter { it.id != cocktail.id }
                                recommendationsFound = true
                            }
                        },
                        onError = { /* Continue to next approach if this fails */ },
                        defaultErrorMessage = "",
                        showAsEvent = false,
                        showLoading = false
                    )
                }

                // If all else fails, try similar cocktails
                if (!recommendationsFound) {
                    handleResultFlow(
                        flow = getRecommendationsUseCase.similarTo(cocktail, 3),
                        onSuccess = { recommendations ->
                            _recommendations.value = recommendations
                        },
                        onError = { _ ->
                            // Don't show error for recommendations
                            _recommendations.value = emptyList()
                        },
                        defaultErrorMessage = "",
                        showAsEvent = false,
                        showLoading = false
                    )
                }
            } catch (e: Exception) {
                // Fallback in case of any errors
                _recommendations.value = emptyList()
            }
        }
    }

    /**
     * Get a random cocktail.
     */
    override fun loadRandomCocktail() {
        executeWithErrorHandling(
            operation = {
                getCocktailDetailsUseCase.random()
            },
            onSuccess = { resultFlow ->
                handleResultFlow(
                    flow = resultFlow,
                    onSuccess = { cocktail ->
                        _cocktail.value = cocktail
                        checkFavoriteStatus(cocktail.id)
                        loadRecommendations(cocktail)
                    },
                    defaultErrorMessage = "Failed to load random cocktail. Please try again.",
                    recoveryAction = ErrorUtils.RecoveryAction("Try Again") { loadRandomCocktail() }
                )
            },
            defaultErrorMessage = "Failed to load random cocktail. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Try Again") { loadRandomCocktail() }
        )
    }

    /**
     * For backward compatibility with code that uses the old API.
     */
    fun loadCocktail(id: String) {
        loadCocktailDetails(id)
    }

    /**
     * For backward compatibility with code that uses the old API.
     */
    fun setCurrentCocktail(cocktail: Cocktail) {
        _cocktail.value = cocktail
        checkFavoriteStatus(cocktail.id)
        loadRecommendations(cocktail)
    }
}
