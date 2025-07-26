package com.cocktailcraft.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedCocktailDetailViewModel using SKIE.
 * This replaces the old CocktailDetailViewModel with a thin wrapper around the shared implementation.
 *
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class CocktailDetailViewModelSKIE : ViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedCocktailDetailViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val cocktail: StateFlow<Cocktail?> = sharedViewModel.cocktail
    val isFavorite: StateFlow<Boolean> = sharedViewModel.isFavorite
    val isInCart: StateFlow<Boolean> = sharedViewModel.isInCart
    val cartQuantity: StateFlow<Int> = sharedViewModel.cartQuantity
    val relatedCocktails: StateFlow<List<Cocktail>> = sharedViewModel.relatedCocktails
    val recommendations: StateFlow<List<Cocktail>> = sharedViewModel.relatedCocktails // Alias for compatibility
    val ingredientsByType: StateFlow<Map<String, List<String>>> = sharedViewModel.ingredientsByType
    val isLoadingRecommendations: StateFlow<Boolean> = sharedViewModel.isLoading // Alias for compatibility
    
    // Expose loading and error from shared base class
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    val errorEventState = sharedViewModel.errorEvent
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Load cocktail details by ID using SKIE async interop
     */
    fun loadCocktail(cocktailId: String) {
        viewModelScope.launch {
            sharedViewModel.loadCocktail(cocktailId)
        }
    }
    
    /**
     * Toggle favorite status using SKIE async interop
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            sharedViewModel.toggleFavorite()
        }
    }
    
    /**
     * Add to cart or update quantity using SKIE async interop
     */
    fun addToCart(quantity: Int = 1) {
        viewModelScope.launch {
            sharedViewModel.addToCart(quantity)
        }
    }
    
    /**
     * Update cart quantity using SKIE async interop
     */
    fun updateCartQuantity(quantity: Int) {
        viewModelScope.launch {
            sharedViewModel.updateCartQuantity(quantity)
        }
    }
    
    /**
     * Remove from cart using SKIE async interop
     */
    fun removeFromCart() {
        viewModelScope.launch {
            sharedViewModel.removeFromCart()
        }
    }
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Share cocktail recipe - returns a shareable text representation
     */
    fun getShareableText(): String {
        return sharedViewModel.getShareableText()
    }
    
    /**
     * Get nutrition facts (example calculation)
     */
    fun getNutritionFacts(): Map<String, String> {
        return sharedViewModel.getNutritionFacts()
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Refresh cocktail details
     */
    fun refresh() {
        sharedViewModel.refresh()
    }
    
    /**
     * Clean up when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        sharedViewModel.onCleared()
    }
}