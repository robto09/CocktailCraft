package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.viewmodel.state.CartUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Cart functionality.
 * Uses consolidated [CartUiState] for atomic state updates.
 */
class SharedCartViewModel : SharedViewModel() {

    private val manageCartUseCase: ManageCartUseCase by inject()

    // Consolidated UI State
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // Derived StateFlows for backward compatibility
    val cartItems: StateFlow<List<CocktailCartItem>> = _uiState
        .map { it.cartItems }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val totalPrice: StateFlow<Double> = _uiState
        .map { it.totalPrice }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)
    val itemCount: StateFlow<Int> = _uiState
        .map { it.itemCount }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    
    // Computed properties
    val isEmpty: Boolean
        get() = _uiState.value.cartItems.isEmpty()

    val hasItems: Boolean
        get() = _uiState.value.cartItems.isNotEmpty()

    init {
        loadCart()
    }

    private fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            setLoading(true)
            try {
                val items = manageCartUseCase.getCartItems().getOrDefault(emptyList())
                _uiState.update { it.copy(
                    cartItems = items,
                    itemCount = items.sumOf { item -> item.quantity },
                    totalPrice = items.sumOf { item -> item.cocktail.price * item.quantity },
                    isLoading = false
                ) }
                setLoading(false)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                setLoading(false)
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
            manageCartUseCase.addToCart(cartItem)
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
            manageCartUseCase.removeFromCart(cocktailId)
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
            manageCartUseCase.updateQuantity(cocktailId, quantity)
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
        val currentItem = _uiState.value.cartItems.find { it.cocktail.id == cocktailId }
        if (currentItem != null) {
            updateQuantity(cocktailId, currentItem.quantity + 1)
        }
    }

    suspend fun decrementQuantity(cocktailId: String) {
        val currentItem = _uiState.value.cartItems.find { it.cocktail.id == cocktailId }
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
            manageCartUseCase.clearCart()
            _uiState.update { it.copy(cartItems = emptyList(), itemCount = 0, totalPrice = 0.0) }
        } catch (e: Exception) {
            handleException(e, "Failed to clear cart", showAsEvent = true)
        }
    }

    // MARK: - Synchronous Helper Methods

    fun isInCart(cocktailId: String): Boolean {
        return _uiState.value.cartItems.any { it.cocktail.id == cocktailId }
    }

    fun getQuantity(cocktailId: String): Int {
        return _uiState.value.cartItems.find { it.cocktail.id == cocktailId }?.quantity ?: 0
    }

    fun getCartItem(cocktailId: String): CocktailCartItem? {
        return _uiState.value.cartItems.find { it.cocktail.id == cocktailId }
    }

    fun getEstimatedDeliveryTime(): String {
        val count = _uiState.value.itemCount
        return when {
            count == 0 -> "No items"
            count <= 3 -> "15-20 minutes"
            count <= 6 -> "20-25 minutes"
            else -> "25-30 minutes"
        }
    }

    fun isFreeDelivery(): Boolean {
        return _uiState.value.totalPrice >= 50.0
    }

    fun getDeliveryFee(): Double {
        return if (isFreeDelivery()) 0.0 else 5.99
    }

    fun getFinalTotal(): Double {
        return _uiState.value.totalPrice + getDeliveryFee()
    }
    
    /**
     * Refresh cart data.
     */
    fun refresh() {
        loadCart()
    }
}