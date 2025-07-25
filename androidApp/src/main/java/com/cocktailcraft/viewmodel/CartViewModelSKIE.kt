package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.viewmodel.SharedCartViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedCartViewModel using SKIE.
 * This replaces the old CartViewModel with a thin wrapper around the shared implementation.
 * 
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class CartViewModelSKIE : BaseViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedCartViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val cartItems: StateFlow<List<CocktailCartItem>> = sharedViewModel.cartItems
    val totalPrice: StateFlow<Double> = sharedViewModel.totalPrice
    val itemCount: StateFlow<Int> = sharedViewModel.itemCount
    
    // Expose loading and error from shared base class
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    val errorEventState = sharedViewModel.errorEvent
    
    // Computed properties
    val isEmpty: Boolean
        get() = sharedViewModel.isEmpty
    
    val hasItems: Boolean
        get() = sharedViewModel.hasItems
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Add a cocktail to the cart using SKIE async interop
     */
    fun addToCart(cocktail: Cocktail, quantity: Int = 1) {
        viewModelScope.launch {
            sharedViewModel.addToCart(cocktail, quantity)
        }
    }
    
    /**
     * Remove an item from the cart using SKIE async interop
     */
    fun removeFromCart(cocktailId: String) {
        viewModelScope.launch {
            sharedViewModel.removeFromCart(cocktailId)
        }
    }
    
    /**
     * Update the quantity of an item in the cart using SKIE async interop
     */
    fun updateQuantity(cocktailId: String, quantity: Int) {
        viewModelScope.launch {
            sharedViewModel.updateQuantity(cocktailId, quantity)
        }
    }
    
    /**
     * Increment the quantity of an item using SKIE async interop
     */
    fun incrementQuantity(cocktailId: String) {
        viewModelScope.launch {
            sharedViewModel.incrementQuantity(cocktailId)
        }
    }
    
    /**
     * Decrement the quantity of an item using SKIE async interop
     */
    fun decrementQuantity(cocktailId: String) {
        viewModelScope.launch {
            sharedViewModel.decrementQuantity(cocktailId)
        }
    }
    
    /**
     * Clear all items from the cart using SKIE async interop
     */
    fun clearCart() {
        viewModelScope.launch {
            sharedViewModel.clearCart()
        }
    }
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Check if a cocktail is in the cart
     */
    fun isInCart(cocktailId: String): Boolean {
        return sharedViewModel.isInCart(cocktailId)
    }
    
    /**
     * Get the quantity of a cocktail in the cart
     */
    fun getQuantity(cocktailId: String): Int {
        return sharedViewModel.getQuantity(cocktailId)
    }
    
    /**
     * Get cart item by cocktail ID
     */
    fun getCartItem(cocktailId: String): CocktailCartItem? {
        return sharedViewModel.getCartItem(cocktailId)
    }
    
    /**
     * Calculate estimated delivery time
     */
    fun getEstimatedDeliveryTime(): String {
        return sharedViewModel.getEstimatedDeliveryTime()
    }
    
    /**
     * Check if free delivery is applicable
     */
    fun isFreeDelivery(): Boolean {
        return sharedViewModel.isFreeDelivery()
    }
    
    /**
     * Calculate delivery fee
     */
    fun getDeliveryFee(): Double {
        return sharedViewModel.getDeliveryFee()
    }
    
    /**
     * Get the final total including delivery
     */
    fun getFinalTotal(): Double {
        return sharedViewModel.getFinalTotal()
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Refresh cart data
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