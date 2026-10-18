package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.added_to_cart
import com.cocktailcraft.resources.error_add_item_to_cart_failed
import com.cocktailcraft.resources.error_cart_title
import com.cocktailcraft.resources.error_clear_cart_failed
import com.cocktailcraft.resources.error_refresh_cart_failed
import com.cocktailcraft.resources.error_remove_item_from_cart_failed
import com.cocktailcraft.resources.error_update_quantity_failed
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.CartUiState
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.CancellationException
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
     * Add a cocktail to the cart. Returns whether it was added.
     *
     * Add-to-cart runs from Home, Favorites and Detail, not from the cart, so
     * its outcome is announced on [events] (a "<name> added to cart" success
     * or the failure message) for the screen that triggered it — never on
     * the cart's error channel, where it would surface later on the Cart tab.
     * SKIE will convert this to Swift async function.
     */
    suspend fun addToCart(cocktail: Cocktail, quantity: Int = 1): Boolean {
        val result = try {
            manageCartUseCase.addToCart(CocktailCartItem(cocktail, quantity))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.message.orEmpty())
        }
        return when (result) {
            is Result.Success -> {
                sendEvent(UiEvent.Success(MR.strings.added_to_cart.format(cocktail.name)))
                true
            }
            is Result.Error -> {
                sendEvent(UiEvent.Failure(
                    ErrorHandler.rawMessage(result.message)
                        ?: MR.strings.error_add_item_to_cart_failed.desc()
                ))
                false
            }
            is Result.Loading -> false
        }
    }

    /**
     * Remove an item from the cart by cocktail ID.
     * SKIE will convert this to Swift async function.
     */
    suspend fun removeFromCart(cocktailId: String) {
        clearError()
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
        clearError()
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
     * Clear all items from the cart. A failed clear leaves the cart as it
     * was and reports the error.
     * SKIE will convert this to Swift async function.
     */
    suspend fun clearCart() {
        clearError()
        try {
            val result = manageCartUseCase.clearCart()
            if (result is Result.Success) {
                _uiState.update { it.copy(cartItems = emptyList(), itemCount = 0, totalPrice = 0.0) }
            } else {
                result.getOrReport(Unit, MR.strings.error_clear_cart_failed)
            }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_clear_cart_failed)
        }
    }

    /**
     * Explicitly resync state with the repository (normally the collected
     * flow keeps it current on its own).
     */
    fun refresh() {
        clearError()
        viewModelScope.launch {
            applyItems(manageCartUseCase.getCartItems().getOrReport(_uiState.value.cartItems, MR.strings.error_refresh_cart_failed))
        }
    }
}
