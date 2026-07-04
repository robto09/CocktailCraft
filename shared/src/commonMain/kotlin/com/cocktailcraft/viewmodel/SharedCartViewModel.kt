package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
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
                reloadItems()
                _uiState.update { it.copy(isLoading = false) }
                setLoading(false)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                setLoading(false)
            }
        }
    }

    /** Reconcile state with the repository without toggling the loading flag. */
    private suspend fun reloadItems() {
        applyItems(manageCartUseCase.getCartItems().getOrDefault(emptyList()))
    }

    /** Atomically publish an item list with its derived totals. */
    private fun applyItems(items: List<CocktailCartItem>) {
        _uiState.update { it.copy(
            cartItems = items,
            itemCount = items.sumOf { item -> item.quantity },
            totalPrice = items.sumOf { item -> item.cocktail.price * item.quantity }
        ) }
    }
    
    /**
     * Add a cocktail to the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun addToCart(cocktail: Cocktail, quantity: Int = 1) {
        try {
            val cartItem = CocktailCartItem(cocktail, quantity)
            manageCartUseCase.addToCart(cartItem)
            reloadItems()
        } catch (e: Exception) {
            handleException(e, "Failed to add item to cart")
        }
    }
    
    /**
     * Remove an item from the cart by cocktail ID.
     * SKIE will convert this to Swift async function.
     */
    suspend fun removeFromCart(cocktailId: String) {
        val previous = _uiState.value.cartItems
        // Optimistic: reflect the removal immediately, revert if it fails
        applyItems(previous.filterNot { it.cocktail.id == cocktailId })
        try {
            val result = manageCartUseCase.removeFromCart(cocktailId)
            if (result is Result.Error) {
                applyItems(previous)
                setError("Cart Error", "Failed to remove item from cart")
            } else {
                reloadItems()
            }
        } catch (e: Exception) {
            applyItems(previous)
            handleException(e, "Failed to remove item from cart")
        }
    }
    
    /**
     * Update the quantity of an item in the cart.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateQuantity(cocktailId: String, quantity: Int) {
        val previous = _uiState.value.cartItems
        // Optimistic: reflect the new quantity immediately, revert if it fails
        applyItems(previous.map {
            if (it.cocktail.id == cocktailId) it.copy(quantity = quantity) else it
        })
        try {
            val result = manageCartUseCase.updateQuantity(cocktailId, quantity)
            if (result is Result.Error) {
                applyItems(previous)
                setError("Cart Error", "Failed to update quantity")
            } else {
                reloadItems()
            }
        } catch (e: Exception) {
            applyItems(previous)
            handleException(e, "Failed to update quantity")
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
            handleException(e, "Failed to clear cart")
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