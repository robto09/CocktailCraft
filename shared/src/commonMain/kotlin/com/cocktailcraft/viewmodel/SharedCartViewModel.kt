package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Cart functionality.
 * Designed for full SKIE interoperability with iOS.
 * 
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedCartViewModel : SharedViewModel() {
    
    private val repository: CartRepository by inject()
    
    // Cart state - SKIE will convert these to Swift AsyncSequence
    private val _cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())
    val cartItems: StateFlow<List<CocktailCartItem>> = _cartItems.asStateFlow()
    
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()
    
    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()
    
    // Computed properties
    val isEmpty: Boolean
        get() = _cartItems.value.isEmpty()
    
    val hasItems: Boolean
        get() = _cartItems.value.isNotEmpty()
    
    init {
        loadCartItems()
    }
    
    /**
     * Load cart items and total.
     * SKIE will convert this to Swift async function.
     */
    private fun loadCartItems() {
        viewModelScope.launch {
            try {
                // Observe cart items
                repository.getCartItems()
                    .catch { e ->
                        handleException(
                            e,
                            "Failed to load cart items",
                            showAsEvent = true
                        )
                    }
                    .collect { items ->
                        _cartItems.value = items
                        _itemCount.value = items.sumOf { it.quantity }
                    }
            } catch (e: Exception) {
                handleException(e, "Failed to load cart items")
            }
        }
        
        viewModelScope.launch {
            try {
                // Observe cart total
                repository.getCartTotal()
                    .catch { e ->
                        // Silent fail for total calculation
                    }
                    .collect { total ->
                        _totalPrice.value = total
                    }
            } catch (e: Exception) {
                // Don't show error for total calculation
            }
        }
    }
    
    /**
     * Add a cocktail to the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun addToCart(cocktail: Cocktail, quantity: Int = 1) {
        try {
            val cartItem = CocktailCartItem(cocktail, quantity)
            repository.addToCart(cartItem)
            
            // Show success feedback
            setError(
                title = "Added to Cart",
                message = "${cocktail.name} has been added to your cart",
                category = ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to add ${cocktail.name} to cart",
                showAsEvent = true,
                recoveryAction = ErrorHandler.RecoveryAction("Retry") {
                    viewModelScope.launch {
                        addToCart(cocktail, quantity)
                    }
                }
            )
        }
    }
    
    /**
     * Remove an item from the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun removeFromCart(cocktailId: String) {
        try {
            repository.removeFromCart(cocktailId)
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to remove item from cart",
                showAsEvent = true,
                recoveryAction = ErrorHandler.RecoveryAction("Retry") {
                    viewModelScope.launch {
                        removeFromCart(cocktailId)
                    }
                }
            )
        }
    }
    
    /**
     * Update the quantity of an item in the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateQuantity(cocktailId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(cocktailId)
            return
        }
        
        try {
            repository.updateQuantity(cocktailId, quantity)
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to update quantity",
                showAsEvent = true,
                recoveryAction = ErrorHandler.RecoveryAction("Retry") {
                    viewModelScope.launch {
                        updateQuantity(cocktailId, quantity)
                    }
                }
            )
        }
    }
    
    /**
     * Increment the quantity of an item.
     * SKIE will convert this to Swift async function.
     */
    suspend fun incrementQuantity(cocktailId: String) {
        val currentItem = _cartItems.value.find { it.cocktail.id == cocktailId }
        if (currentItem != null) {
            updateQuantity(cocktailId, currentItem.quantity + 1)
        }
    }
    
    /**
     * Decrement the quantity of an item.
     * SKIE will convert this to Swift async function.
     */
    suspend fun decrementQuantity(cocktailId: String) {
        val currentItem = _cartItems.value.find { it.cocktail.id == cocktailId }
        if (currentItem != null) {
            updateQuantity(cocktailId, currentItem.quantity - 1)
        }
    }
    
    /**
     * Clear all items from the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun clearCart() {
        try {
            repository.clearCart()
            
            // Show success feedback
            setError(
                title = "Cart Cleared",
                message = "All items have been removed from your cart",
                category = ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
        } catch (e: Exception) {
            handleException(
                e,
                "Failed to clear cart",
                showAsEvent = true,
                recoveryAction = ErrorHandler.RecoveryAction("Retry") {
                    viewModelScope.launch {
                        clearCart()
                    }
                }
            )
        }
    }
    
    /**
     * Check if a cocktail is in the cart.
     */
    fun isInCart(cocktailId: String): Boolean {
        return _cartItems.value.any { it.cocktail.id == cocktailId }
    }
    
    /**
     * Get the quantity of a cocktail in the cart.
     */
    fun getQuantity(cocktailId: String): Int {
        return _cartItems.value.find { it.cocktail.id == cocktailId }?.quantity ?: 0
    }
    
    /**
     * Get cart item by cocktail ID.
     */
    fun getCartItem(cocktailId: String): CocktailCartItem? {
        return _cartItems.value.find { it.cocktail.id == cocktailId }
    }
    
    /**
     * Calculate estimated delivery time (example business logic).
     */
    fun getEstimatedDeliveryTime(): String {
        val itemCount = _itemCount.value
        return when {
            itemCount <= 3 -> "15-20 minutes"
            itemCount <= 6 -> "20-30 minutes"
            else -> "30-45 minutes"
        }
    }
    
    /**
     * Check if free delivery is applicable.
     */
    fun isFreeDelivery(): Boolean {
        return _totalPrice.value >= 50.0 // Free delivery for orders over $50
    }
    
    /**
     * Calculate delivery fee.
     */
    fun getDeliveryFee(): Double {
        return if (isFreeDelivery()) 0.0 else 5.99
    }
    
    /**
     * Get the final total including delivery.
     */
    fun getFinalTotal(): Double {
        return _totalPrice.value + getDeliveryFee()
    }
    
    /**
     * Refresh cart data.
     */
    fun refresh() {
        loadCartItems()
    }
}