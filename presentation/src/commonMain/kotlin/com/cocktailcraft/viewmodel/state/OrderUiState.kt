package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order

/**
 * Consolidated UI state for the order list and checkout. One order's screen
 * has its own state ([OrderDetailUiState]).
 */
data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val currentOrder: Order? = null,
    val orderCount: Int = 0,
    val isPlacingOrder: Boolean = false,
    val totalSpent: Money = Money.ZERO,
    val isLoading: Boolean = false
)
