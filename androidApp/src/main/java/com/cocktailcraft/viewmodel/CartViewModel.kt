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
    val cartItems: StateFlow<List<CocktailCartItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    init {
        loadCartItems()
    }

    /**
     * Load all cart items and calculate total price.
     */
    fun loadCartItems() {
        executeWithErrorHandling(
            operation = {
                manageCartUseCase.getCartItems()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _cartItems.value = result.data
                                loadCartTotal()
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Load Cart",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA,
                                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCartItems() }
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to load cart items. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadCartItems() }
        )
    }

    /**
     * Load the total price of all items in the cart.
     */
    private fun loadCartTotal() {
        executeWithErrorHandling(
            operation = {
                manageCartUseCase.getCartTotal()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _totalPrice.value = result.data
                            }
                            is Result.Error -> {
                                // Don't show error for total calculation
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to calculate cart total.",
            showLoading = false
        )
    }

    /**
     * Add a cocktail to the cart.
     */
    fun addToCart(cocktail: Cocktail, quantity: Int = 1) {
        executeWithErrorHandling(
            operation = {
                manageCartUseCase.addToCart(cocktail, quantity)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                loadCartItems()
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Add to Cart",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to add item to cart. Please try again."
        )
    }

    /**
     * Remove a cocktail from the cart.
     */
    fun removeFromCart(cocktailId: String) {
        executeWithErrorHandling(
            operation = {
                manageCartUseCase.removeFromCart(cocktailId)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                loadCartItems()
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Remove from Cart",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to remove item from cart. Please try again."
        )
    }

    /**
     * Update the quantity of a cocktail in the cart.
     */
    fun updateQuantity(cocktailId: String, quantity: Int) {
        executeWithErrorHandling(
            operation = {
                manageCartUseCase.updateQuantity(cocktailId, quantity)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                loadCartItems()
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Update Quantity",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to update quantity. Please try again."
        )
    }

    /**
     * Clear all items from the cart.
     */
    fun clearCart() {
        executeWithErrorHandling(
            operation = {
                manageCartUseCase.clearCart()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                loadCartItems()
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Clear Cart",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to clear cart. Please try again."
        )
    }
}