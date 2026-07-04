package com.cocktailcraft.viewmodel

import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.usecase.ManageOrdersUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.OrderUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Order functionality.
 * Uses consolidated [OrderUiState] for atomic state updates.
 */
class SharedOrderViewModel : SharedViewModel() {

    private val manageOrdersUseCase: ManageOrdersUseCase by inject()
    private val placeOrderUseCase: PlaceOrderUseCase by inject()

    // Consolidated UI State
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    val hasOrders: Boolean
        get() = _uiState.value.orders.isNotEmpty()
    val isEmpty: Boolean
        get() = _uiState.value.orders.isEmpty()

    init {
        viewModelScope.launch { loadOrders() }
    }
    
    /**
     * Load orders from manageOrdersUseCase.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadOrders() {
        _uiState.update { it.copy(isLoading = true) }
        setLoading(true)
        try {
            val orderList = manageOrdersUseCase.getOrders().getOrDefault(emptyList())
            _uiState.update { it.copy(
                orders = orderList.sortedByDescending { o -> o.date },
                orderCount = orderList.size,
                totalSpent = orderList.sumOf { o -> o.total },
                isLoading = false
            ) }
            setLoading(false)
        } catch (e: Exception) {
            handleException(e, "Failed to load orders")
            _uiState.update { it.copy(isLoading = false) }
            setLoading(false)
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
        setLoading(true)

        try {
            val result = placeOrderUseCase(cartItems, totalPrice)
            return when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(currentOrder = result.data, isPlacingOrder = false, isLoading = false) }
                    setLoading(false)
                    loadOrders()
                    true
                }
                is Result.Error -> {
                    setError(
                        "Order Failed",
                        result.message,
                        ErrorHandler.ErrorCategory.SERVER
                    )
                    _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
                    setLoading(false)
                    false
                }
                is Result.Loading -> {
                    false
                }
            }
        } catch (e: Exception) {
            handleException(e, "Failed to place order")
            _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
            setLoading(false)
            return false
        }
    }

    /**
     * Place order with callback for iOS integration.
     * This method provides a callback-based interface for better iOS interop.
     */
    fun placeOrderWithCallback(cartItems: List<CocktailCartItem>, totalPrice: Double, callback: (Boolean) -> Unit) {
        Logger.d { "SharedOrderViewModel.placeOrderWithCallback called with ${cartItems.size} items, total: $totalPrice" }

        if (cartItems.isEmpty()) {
            setError(
                "Empty Cart",
                "Cannot place order with empty cart",
                ErrorHandler.ErrorCategory.DATA
            )
            callback(false)
            return
        }

        _uiState.update { it.copy(isPlacingOrder = true, isLoading = true) }
        setLoading(true)

        viewModelScope.launch {
            try {
                Logger.d { "SharedOrderViewModel: Starting order placement" }
                val result = placeOrderUseCase(cartItems, totalPrice)
                Logger.d { "SharedOrderViewModel: Received result: $result" }
                when (result) {
                    is Result.Success -> {
                        Logger.d { "SharedOrderViewModel: Processing success result" }
                        _uiState.update { it.copy(currentOrder = result.data, isPlacingOrder = false, isLoading = false) }
                        setLoading(false)
                        Logger.d { "SharedOrderViewModel: Order placed successfully, calling callback with true" }
                        callback(true)
                        loadOrders()
                    }
                    is Result.Error -> {
                        Logger.w { "SharedOrderViewModel: Processing error result: ${result.message}" }
                        setError(
                            "Order Failed",
                            result.message,
                            ErrorHandler.ErrorCategory.SERVER
                        )
                        _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
                        setLoading(false)
                        Logger.d { "SharedOrderViewModel: Order failed, calling callback with false" }
                        callback(false)
                    }
                    is Result.Loading -> {
                        Logger.d { "SharedOrderViewModel: Processing loading result" }
                    }
                }
                Logger.d { "SharedOrderViewModel: Order placement completed" }
            } catch (e: Exception) {
                Logger.e(e) { "SharedOrderViewModel: Exception in viewModelScope: ${e.message}" }
                handleException(e, "Failed to place order")
                _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
                setLoading(false)
                Logger.d { "SharedOrderViewModel: Exception occurred, calling callback with false" }
                callback(false)
            }
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
            setLoading(true)
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
            setLoading(false)
            success
        } catch (e: Exception) {
            handleException(e, "Failed to cancel order")
            setLoading(false)
            false
        }
    }
    
    /**
     * Reorder items from a previous order.
     * SKIE will convert this to Swift async function.
     */
    suspend fun reorderItems(orderId: String): Boolean {
        return try {
            val order = getOrderById(orderId)
            if (order != null) {
                // Convert order items back to cart items for reordering
                // This is a simplified implementation - in a real app you'd need to
                // convert OrderItems back to CocktailCartItems properly
                val totalPrice = order.total
                
                // For now, we'll create a new order with the same total
                // In a real implementation, you'd reconstruct the cart items
                val emptyCartItems = emptyList<CocktailCartItem>()
                
                setError(
                    "Reorder Feature",
                    "Reorder functionality needs cart integration",
                    ErrorHandler.ErrorCategory.CLIENT
                )
                false
            } else {
                setError(
                    "Order Not Found",
                    "Cannot reorder - original order not found",
                    ErrorHandler.ErrorCategory.DATA
                )
                false
            }
        } catch (e: Exception) {
            handleException(e, "Failed to reorder items")
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

    fun getOrderStatistics(): Map<String, Any> {
        val orders = _uiState.value.orders
        val statusCounts = orders.groupBy { it.status }.mapValues { it.value.size }
        val averageOrderValue = if (orders.isNotEmpty()) orders.sumOf { it.total } / orders.size else 0.0
        return mapOf(
            "totalOrders" to orders.size,
            "totalSpent" to _uiState.value.totalSpent,
            "averageOrderValue" to averageOrderValue,
            "statusBreakdown" to statusCounts
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
        return when (order?.status?.lowercase()) {
            "pending" -> "Processing will begin shortly"
            "processing" -> "15-25 minutes"
            "shipped" -> "5-10 minutes"
            "delivered" -> "Delivered"
            "cancelled" -> "Cancelled"
            else -> "Unknown"
        }
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