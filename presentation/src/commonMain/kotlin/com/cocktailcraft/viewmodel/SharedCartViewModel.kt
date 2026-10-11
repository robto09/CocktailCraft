package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_add_item_to_cart_failed
import com.cocktailcraft.resources.error_cart_title
import com.cocktailcraft.resources.error_clear_cart_failed
import com.cocktailcraft.resources.error_refresh_cart_failed
import com.cocktailcraft.resources.error_remove_item_from_cart_failed
import com.cocktailcraft.resources.error_update_quantity_failed
import com.cocktailcraft.viewmodel.state.CartUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Cart functionality.
 * Uses consolidated [CartUiState] for atomic state updates; [uiState] is the
 * only public state surface (delivery pricing is derived on the state).
 */
class SharedCartViewModel internal constructor(
    private val manageCartUseCase: ManageCartUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        // The repository exposes a hot StateFlow seeded from persistence —
        // collecting it is the single way cart contents enter this ViewModel.
        viewModelScope.launch {
            manageCartUseCase.observeCartItems().collect { applyItems(it) }
        }
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
            manageCartUseCase.addToCart(cartItem).getOrThrow()
        } catch (e: Exception) {
            handleException(e, MR.strings.error_add_item_to_cart_failed)
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
                setError(MR.strings.error_cart_title, MR.strings.error_remove_item_from_cart_failed)
            }
        } catch (e: Exception) {
            applyItems(previous)
            handleException(e, MR.strings.error_remove_item_from_cart_failed)
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
                setError(MR.strings.error_cart_title, MR.strings.error_update_quantity_failed)
            }
        } catch (e: Exception) {
            applyItems(previous)
            handleException(e, MR.strings.error_update_quantity_failed)
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
            handleException(e, MR.strings.error_clear_cart_failed)
        }
    }

    /**
     * Explicitly resync state with the repository (normally the collected
     * flow keeps it current on its own).
     */
    fun refresh() {
        viewModelScope.launch {
            applyItems(manageCartUseCase.getCartItems().getOrReport(_uiState.value.cartItems, MR.strings.error_refresh_cart_failed))
        }
    }
}