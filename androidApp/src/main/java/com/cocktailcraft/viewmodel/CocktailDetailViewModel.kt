package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.recommendation.CocktailRecommendationEngine
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CocktailDetailViewModel(
    private val cocktailRepository: CocktailRepository,
    private val recommendationEngine: CocktailRecommendationEngine
) : BaseViewModel() {

    private val _cocktail = MutableStateFlow<Cocktail?>(null)
    val cocktail: StateFlow<Cocktail?> = _cocktail.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _recommendations = MutableStateFlow<List<Cocktail>>(emptyList())
    val recommendations: StateFlow<List<Cocktail>> = _recommendations.asStateFlow()

    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations.asStateFlow()

    fun loadCocktail(id: String) {
        setLoading(true)
        clearError()

        viewModelScope.launch {
            try {
                cocktailRepository.getCocktailById(id).collect { loadedCocktail ->
                    _cocktail.value = loadedCocktail

                    // Check if this cocktail is a favorite
                    checkFavoriteStatus(id)

                    // Load recommendations for this cocktail
                    loadRecommendations(loadedCocktail)

                    setLoading(false)
                }
            } catch (e: Exception) {
                handleException(e, "Failed to load cocktail details")
            }
        }
    }

    private fun checkFavoriteStatus(id: String) {
        viewModelScope.launch {
            try {
                cocktailRepository.isCocktailFavorite(id).collect { favorite ->
                    _isFavorite.value = favorite
                }
            } catch (e: Exception) {
                // Just log the error but don't show to user as this is not critical
                println("Failed to check favorite status: ${e.message}")
            }
        }
    }

    fun toggleFavorite() {
        val currentCocktail = _cocktail.value ?: return

        viewModelScope.launch {
            try {
                if (_isFavorite.value) {
                    cocktailRepository.removeFromFavorites(currentCocktail)
                    _isFavorite.value = false
                } else {
                    cocktailRepository.addToFavorites(currentCocktail)
                    _isFavorite.value = true
                }
            } catch (e: Exception) {
                setError(
                    title = "Favorite Error",
                    message = "Failed to update favorite status: ${e.message}",
                    category = ErrorUtils.ErrorCategory.DATA
                )
            }
        }
    }

    private fun loadRecommendations(cocktail: Cocktail?) {
        if (cocktail == null) return

        _isLoadingRecommendations.value = true

        viewModelScope.launch {
            try {
                val recommendedCocktails = recommendationEngine.getRecommendations(cocktail)
                _recommendations.value = recommendedCocktails
            } catch (e: Exception) {
                // Just log the error but don't show to user as recommendations are not critical
                println("Failed to load recommendations: ${e.message}")
                _recommendations.value = emptyList()
            } finally {
                _isLoadingRecommendations.value = false
            }
        }
    }
}
