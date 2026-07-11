package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderStatistics
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.ManageOrdersUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.OrderUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Order functionality.
 * Uses consolidated [OrderUiState] for atomic state updates.
 */
class SharedOrderViewModel internal constructor(
    private val manageOrdersUseCase: ManageOrdersUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    val hasOrders: Boolean
        get() = _uiState.value.orders.isNotEmpty()
    val isEmpty: Boolean
        get() = _uiState.value.orders.isEmpty()

    init {
        // The repository exposes a hot StateFlow seeded from persistence —
        // collecting it is the single way orders enter this ViewModel.
        viewModelScope.launch {
            manageOrdersUseCase.observeOrders().collect { orderList ->
                _uiState.update { it.copy(
                    orders = orderList.sortedByDescending { o -> o.date },
                    orderCount = orderList.size,
                    totalSpent = orderList.sumOf { o -> o.total },
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Load orders from manageOrdersUseCase.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadOrders() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val orderList = manageOrdersUseCase.getOrders().getOrThrow()
            _uiState.update { it.copy(
                orders = orderList.sortedByDescending { o -> o.date },
                orderCount = orderList.size,
                totalSpent = orderList.sumOf { o -> o.total },
                isLoading = false
            ) }
        } catch (e: Exception) {
            handleException(e, "Failed to load orders")
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Place a new order.
     * SKIE will convert this to Swift async function.
     */
    suspend fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double): Boolean {
        if (cartItems.isEmpty()) {
            setError(
                "Empty Cart",
                "Cannot place order with empty cart",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        _uiState.update { it.copy(isPlacingOrder = true, isLoading = true) }

        try {
            val result = placeOrderUseCase(cartItems, totalPrice)
            return when (result) {
                is Result.Success -> {
                    // The observed orders flow picks up the new order automatically
                    _uiState.update { it.copy(currentOrder = result.data, isPlacingOrder = false, isLoading = false) }
                    true
                }
                is Result.Error -> {
                    setError(
                        "Order Failed",
                        result.message,
                        ErrorHandler.ErrorCategory.SERVER
                    )
                    _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
                    false
                }
                is Result.Loading -> {
                    false
                }
            }
        } catch (e: Exception) {
            handleException(e, "Failed to place order")
            _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
            return false
        }
    }

    /**
     * Get order by ID.
     * SKIE will convert this to Swift async function returning optional.
     */
    suspend fun getOrderById(orderId: String): Order? {
        return try {
            manageOrdersUseCase.getOrderById(orderId).getOrNull()
        } catch (e: Exception) {
            handleException(e, "Failed to load order details")
            null
        }
    }
    
    /**
     * Update order status.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Boolean {
        return try {
            val result = manageOrdersUseCase.updateOrderStatus(orderId, status)
            if (result.isSuccess()) {
                loadOrders() // Refresh orders list
                true
            } else {
                handleException(Exception("Failed to update order status"), "Failed to update order status")
                false
            }
        } catch (e: Exception) {
            handleException(e, "Failed to update order status")
            false
        }
    }
    
    /**
     * Cancel an order.
     * SKIE will convert this to Swift async function.
     */
    suspend fun cancelOrder(orderId: String): Boolean {
        return try {
            val success = manageOrdersUseCase.cancelOrder(orderId).getOrDefault(false)
            if (success) {
                loadOrders()
            } else {
                setError(
                    "Cancellation Failed",
                    "Unable to cancel order at this time",
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
            success
        } catch (e: Exception) {
            handleException(e, "Failed to cancel order")
            false
        }
    }
    
// MARK: - Synchronous Helper Methods
    
    /**
     * Get orders by status.
     */
    fun getOrdersByStatus(status: String): List<Order> =
        _uiState.value.orders.filter { it.status.equals(status, ignoreCase = true) }

    fun getTotalSpent(): Double = _uiState.value.totalSpent

    fun getOrderHistory(limit: Int = 10): List<Order> =
        _uiState.value.orders.take(limit)

    fun getRecentOrders(): List<Order> =
        _uiState.value.orders.take(5)

    fun getOrdersByDateRange(startDate: String, endDate: String): List<Order> =
        _uiState.value.orders.filter { it.date >= startDate && it.date <= endDate }

    fun getAverageOrderValue(): Double {
        val orders = _uiState.value.orders
        return if (orders.isNotEmpty()) orders.sumOf { it.total } / orders.size else 0.0
    }

    /** Orders placed in the current calendar month (order dates are ISO yyyy-MM-dd strings). */
    fun getOrdersThisMonth(): List<Order> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startOfMonth = LocalDate(today.year, today.month, 1)
        return getOrdersByDateRange(startOfMonth.toString(), today.toString())
    }

    fun getOrderStatistics(): OrderStatistics {
        val orders = _uiState.value.orders
        return OrderStatistics(
            totalOrders = orders.size,
            totalSpent = _uiState.value.totalSpent,
            averageOrderValue = getAverageOrderValue(),
            statusBreakdown = orders.groupBy { it.status }.mapValues { it.value.size }
        )
    }

    fun canCancelOrder(orderId: String): Boolean {
        val order = _uiState.value.orders.find { it.id == orderId }
        return order?.status?.lowercase() in listOf("pending", "processing")
    }
    
    /**
     * Get order status display text.
     */
    fun getOrderStatusDisplay(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "Order Pending"
            "processing" -> "Being Prepared"
            "shipped" -> "On the Way"
            "delivered" -> "Delivered"
            "cancelled" -> "Cancelled"
            else -> status
        }
    }
    
    /**
     * Get estimated delivery time for an order.
     */
    fun getEstimatedDeliveryTime(orderId: String): String {
        val order = _uiState.value.orders.find { it.id == orderId }
        return DeliveryPolicy.estimatedDeliveryTimeForStatus(order?.status)
    }
    
    /**
     * Refresh orders data.
     */
    fun refresh() {
        viewModelScope.launch {
            loadOrders()
        }
    }
}