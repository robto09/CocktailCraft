package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.util.ErrorHandler

/**
 * Consolidated UI state for the Cart screen.
 */
data class CartUiState(
    val cartItems: List<CocktailCartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val itemCount: Int = 0,
    val isLoading: Boolean = false,
    val error: ErrorHandler.UserFriendlyError? = null
)

