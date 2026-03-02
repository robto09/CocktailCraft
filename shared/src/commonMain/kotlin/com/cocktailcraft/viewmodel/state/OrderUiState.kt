package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.util.ErrorHandler

/**
 * Consolidated UI state for the Order screen.
 */
data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val currentOrder: Order? = null,
    val orderCount: Int = 0,
    val isPlacingOrder: Boolean = false,
    val totalSpent: Double = 0.0,
    val isLoading: Boolean = false,
    val error: ErrorHandler.UserFriendlyError? = null
)

