package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the cart screen.
 * Uses use cases instead of directly accessing repositories.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state for cart items and total price
 * - Handles user interactions like adding/removing items and updating quantities
 * - Provides a clean API for the UI layer
 * - Implements the ICartViewModel interface for cross-platform compatibility
 */
class CartViewModel(
    private val manageCartUseCase: ManageCartUseCase
) : BaseViewModel(), ICartViewModel {

    // Cart state
    private val _cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())
    override val cartItems: StateFlow<List<CocktailCartItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    override val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    init {
        loadCartItems()
    }

    /**
     * Load all cart items and calculate total price.
     */
    override fun loadCartItems() {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageCartUseCase.getCartItems(),
                onSuccess = { items ->
                    _cartItems.value = items
                    loadCartTotal()
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load cart items. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCartItems() }
            )
        }
    }

    /**
     * Load the total price of all items in the cart.
     */
    private fun loadCartTotal() {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageCartUseCase.getCartTotal(),
                onSuccess = { total ->
                    _totalPrice.value = total
                },
                defaultErrorMessage = "Failed to calculate cart total.",
                showLoading = false,
                showAsEvent = false // Don't show error for total calculation
            )
        }
    }

    /**
     * Add a cocktail to the cart.
     */
    override fun addToCart(cocktail: Cocktail, quantity: Int) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageCartUseCase.addToCart(cocktail, quantity),
                onSuccess = { _ ->
                    loadCartItems()
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to add item to cart. Please try again."
            )
        }
    }

    /**
     * Remove a cocktail from the cart.
     */
    override fun removeFromCart(cocktailId: String) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageCartUseCase.removeFromCart(cocktailId),
                onSuccess = { _ ->
                    loadCartItems()
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to remove item from cart. Please try again."
            )
        }
    }

    /**
     * Update the quantity of a cocktail in the cart.
     */
    override fun updateQuantity(cocktailId: String, quantity: Int) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageCartUseCase.updateQuantity(cocktailId, quantity),
                onSuccess = { _ ->
                    loadCartItems()
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to update quantity. Please try again."
            )
        }
    }

    /**
     * Clear all items from the cart.
     */
    override fun clearCart() {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageCartUseCase.clearCart(),
                onSuccess = { _ ->
                    loadCartItems()
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to clear cart. Please try again."
            )
        }
    }
}