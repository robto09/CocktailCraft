package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.NutritionFacts
import com.cocktailcraft.domain.usecase.AnalyzeCocktailUseCase
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.viewmodel.state.DetailUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Cocktail Detail functionality.
 * Uses consolidated [DetailUiState] for atomic state updates.
 */
class SharedCocktailDetailViewModel internal constructor(
    private val getCocktailDetailUseCase: GetCocktailDetailUseCase,
    private val manageFavoritesUseCase: ManageFavoritesUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val analyzeCocktailUseCase: AnalyzeCocktailUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Load cocktail details by ID.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadCocktail(cocktailId: String) {
        _uiState.update { it.copy(isLoading = true) }
        clearError()

        try {
            val loadedCocktail = getCocktailDetailUseCase(cocktailId).getOrThrow()
            _uiState.update { it.copy(cocktail = loadedCocktail) }
            if (loadedCocktail != null) {
                viewModelScope.launch {
                    try {
                        val fav = getCocktailDetailUseCase.isFavorite(cocktailId)
                        _uiState.update { it.copy(isFavorite = fav) }
                    } catch (e: Exception) { /* Silent fail */ }
                }
                updateCartStatus(cocktailId)
                loadRelatedCocktails(loadedCocktail)
                processIngredients(loadedCocktail)
            }
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            handleException(e, "Failed to load cocktail details")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Toggle favorite status. The flip is optimistic — the heart must react
     * on tap, not after the persist/hydration round trip — and is reconciled
     * against the toggle result (reverting, with a surfaced error, on failure).
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleFavorite() {
        val currentCocktail = _uiState.value.cocktail ?: return
        val wasFavorite = _uiState.value.isFavorite
        _uiState.update { it.copy(isFavorite = !wasFavorite) }

        val reconciled = manageFavoritesUseCase.toggle(currentCocktail)
            .getOrReport(default = wasFavorite, fallbackMessage = "Failed to update favorites")
        _uiState.update { it.copy(isFavorite = reconciled) }
    }

    /**
     * Add to cart or update quantity.
     * SKIE will convert this to Swift async function.
     */
    suspend fun addToCart(quantity: Int = 1) {
        val currentCocktail = _uiState.value.cocktail ?: return
        try {
            manageCartUseCase.addToCart(currentCocktail, quantity)
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to add to cart")
        }
    }

    suspend fun updateCartQuantity(quantity: Int) {
        val currentCocktail = _uiState.value.cocktail ?: return
        try {
            if (quantity > 0) {
                manageCartUseCase.updateQuantity(currentCocktail.id, quantity)
            } else {
                manageCartUseCase.removeFromCart(currentCocktail.id)
            }
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to update cart quantity")
        }
    }

    suspend fun removeFromCart() {
        val currentCocktail = _uiState.value.cocktail ?: return
        try {
            manageCartUseCase.removeFromCart(currentCocktail.id)
            updateCartStatus(currentCocktail.id)
        } catch (e: Exception) {
            handleException(e, "Failed to remove from cart")
        }
    }
    
    // MARK: - Private Helper Methods
    
    private suspend fun updateCartStatus(cocktailId: String) {
        try {
            val cartItems = manageCartUseCase.getCartItems().getOrDefault(emptyList())
            val cartItem = cartItems.find { it.cocktail.id == cocktailId }
            _uiState.update { it.copy(isInCart = cartItem != null, cartQuantity = cartItem?.quantity ?: 0) }
        } catch (e: Exception) {
            // Silent fail for cart status
        }
    }

    private suspend fun loadRelatedCocktails(cocktail: Cocktail) {
        try {
            val related = getCocktailDetailUseCase.getRelatedCocktails(cocktail)
            _uiState.update { it.copy(relatedCocktails = related) }
        } catch (e: Exception) {
            // Silent fail for related cocktails
        }
    }

    private fun processIngredients(cocktail: Cocktail) {
        _uiState.update { it.copy(ingredientsByType = analyzeCocktailUseCase.ingredientsByType(cocktail)) }
    }
    
    // MARK: - Synchronous Helper Methods
    
    /**
     * Get shareable text representation of the cocktail.
     */
    fun getShareableText(): String {
        val cocktail = _uiState.value.cocktail ?: return ""
        
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
    
    /**
     * Get estimated nutrition facts (typed; heuristic estimation in domain).
     */
    fun getNutritionFacts(): NutritionFacts? {
        val cocktail = _uiState.value.cocktail ?: return null
        return analyzeCocktailUseCase.nutritionFacts(cocktail)
    }
    
    /**
     * Refresh cocktail details.
     */
    fun refresh() {
        val currentCocktail = _uiState.value.cocktail
        if (currentCocktail != null) {
            viewModelScope.launch {
                loadCocktail(currentCocktail.id)
            }
        }
    }

    /**
     * Force-refresh the current cocktail from the network, bypassing the
     * repository cache, and update the UI state with the fresh copy.
     * SKIE will convert this to a Swift async function.
     */
    suspend fun refreshCocktail() {
        val cocktailId = _uiState.value.cocktail?.id ?: return
        _uiState.update { it.copy(isLoading = true) }

        try {
            val refreshed = getCocktailDetailUseCase.refresh(cocktailId).getOrThrow()
            if (refreshed != null) {
                _uiState.update { it.copy(cocktail = refreshed) }
                processIngredients(refreshed)
            }
            _uiState.update { it.copy(isLoading = false) }
        } catch (e: Exception) {
            handleException(e, "Failed to refresh cocktail details")
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}