package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CartRepository
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
    
    // UI State - SKIE will convert these to Swift AsyncSequence
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
        loadCart()
    }
    
    private fun loadCart() {
        viewModelScope.launch {
            setLoading(true)
            try {
                repository.getCartItems()
                    .catch { /* Ignore errors */ }
                    .collect { items ->
                        _cartItems.value = items
                        updateTotals()
                        setLoading(false)
                    }
            } catch (e: Exception) {
                setLoading(false)
            }
        }
    }
    
    private fun updateTotals() {
        val items = _cartItems.value
        _itemCount.value = items.sumOf { it.quantity }
        _totalPrice.value = items.sumOf { it.cocktail.price * it.quantity }
    }
    
    /**
     * Add a cocktail to the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun addToCart(cocktail: Cocktail, quantity: Int = 1) {
        try {
            val cartItem = CocktailCartItem(cocktail, quantity)
            repository.addToCart(cartItem)
            loadCart()
        } catch (e: Exception) {
            handleException(e, "Failed to add item to cart", showAsEvent = true)
        }
    }
    
    /**
     * Remove an item from the cart by cocktail ID.
     * SKIE will convert this to Swift async function.
     */
    suspend fun removeFromCart(cocktailId: String) {
        try {
            repository.removeFromCart(cocktailId)
            loadCart()
        } catch (e: Exception) {
            handleException(e, "Failed to remove item from cart", showAsEvent = true)
        }
    }
    
    /**
     * Update the quantity of an item in the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateQuantity(cocktailId: String, quantity: Int) {
        try {
            repository.updateQuantity(cocktailId, quantity)
            loadCart()
        } catch (e: Exception) {
            handleException(e, "Failed to update quantity", showAsEvent = true)
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
        if (currentItem != null && currentItem.quantity > 1) {
            updateQuantity(cocktailId, currentItem.quantity - 1)
        } else if (currentItem != null) {
            removeFromCart(cocktailId)
        }
    }
    
    /**
     * Clear all items from the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun clearCart() {
        try {
            repository.clearCart()
            _cartItems.value = emptyList()
            updateTotals()
        } catch (e: Exception) {
            handleException(e, "Failed to clear cart", showAsEvent = true)
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
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
     * Calculate estimated delivery time.
     */
    fun getEstimatedDeliveryTime(): String {
        val itemCount = _itemCount.value
        return when {
            itemCount == 0 -> "No items"
            itemCount <= 3 -> "15-20 minutes"
            itemCount <= 6 -> "20-25 minutes"
            else -> "25-30 minutes"
        }
    }
    
    /**
     * Check if free delivery is applicable.
     */
    fun isFreeDelivery(): Boolean {
        return _totalPrice.value >= 50.0
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
        loadCart()
    }
}