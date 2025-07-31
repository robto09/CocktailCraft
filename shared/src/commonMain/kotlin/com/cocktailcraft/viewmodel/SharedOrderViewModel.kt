package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Order functionality.
 * Designed for full SKIE interoperability with iOS.
 * 
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedOrderViewModel : SharedViewModel() {
    
    private val repository: OrderRepository by inject()
    private val placeOrderUseCase: PlaceOrderUseCase by inject()
    
    // Order state - SKIE will convert these to Swift AsyncSequence
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()
    
    private val _orderCount = MutableStateFlow(0)
    val orderCount: StateFlow<Int> = _orderCount.asStateFlow()
    
    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()
    
    private val _totalSpent = MutableStateFlow(0.0)
    val totalSpent: StateFlow<Double> = _totalSpent.asStateFlow()
    
    // Computed properties
    val hasOrders: Boolean
        get() = _orders.value.isNotEmpty()
    
    val isEmpty: Boolean
        get() = _orders.value.isEmpty()
    
    init {
        viewModelScope.launch {
            loadOrders()
        }
    }
    
    /**
     * Load orders from repository.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadOrders() {
        setLoading(true)
        try {
            repository.getOrders()
                .catch { e ->
                    handleException(e, "Failed to load orders")
                }
                .collect { orderList ->
                    _orders.value = orderList.sortedByDescending { it.date }
                    _orderCount.value = orderList.size
                    _totalSpent.value = orderList.sumOf { it.total }
                    setLoading(false)
                }
        } catch (e: Exception) {
            handleException(e, "Failed to load orders")
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
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }

        _isPlacingOrder.value = true
        setLoading(true)

        try {
            var orderSuccess = false
            placeOrderUseCase(cartItems, totalPrice)
                .catch { e ->
                    handleException(e, "Failed to place order")
                    _isPlacingOrder.value = false
                    setLoading(false)
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _currentOrder.value = result.data
                            loadOrders() // Refresh orders list
                            _isPlacingOrder.value = false
                            setLoading(false)
                            orderSuccess = true
                        }
                        is Result.Error -> {
                            setError(
                                "Order Failed",
                                result.message,
                                ErrorHandler.ErrorCategory.SERVER,
                                showAsEvent = true
                            )
                            _isPlacingOrder.value = false
                            setLoading(false)
                            orderSuccess = false
                        }
                        is Result.Loading -> {
                            // Loading state is already handled by _isPlacingOrder and setLoading
                        }
                    }
                }
            return orderSuccess
        } catch (e: Exception) {
            handleException(e, "Failed to place order", showAsEvent = true)
            _isPlacingOrder.value = false
            setLoading(false)
            return false
        }
    }

    /**
     * Place order with callback for iOS integration.
     * This method provides a callback-based interface for better iOS interop.
     */
    fun placeOrderWithCallback(cartItems: List<CocktailCartItem>, totalPrice: Double, callback: (Boolean) -> Unit) {
        println("SharedOrderViewModel.placeOrderWithCallback called with ${cartItems.size} items, total: $totalPrice")

        if (cartItems.isEmpty()) {
            setError(
                "Empty Cart",
                "Cannot place order with empty cart",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            callback(false)
            return
        }

        _isPlacingOrder.value = true
        setLoading(true)

        viewModelScope.launch {
            try {
                placeOrderUseCase(cartItems, totalPrice)
                    .catch { e ->
                        handleException(e, "Failed to place order")
                        _isPlacingOrder.value = false
                        setLoading(false)
                        callback(false)
                    }
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _currentOrder.value = result.data
                                loadOrders() // Refresh orders list
                                _isPlacingOrder.value = false
                                setLoading(false)
                                println("SharedOrderViewModel: Order placed successfully, calling callback with true")
                                callback(true)
                            }
                            is Result.Error -> {
                                setError(
                                    "Order Failed",
                                    result.message,
                                    ErrorHandler.ErrorCategory.SERVER,
                                    showAsEvent = true
                                )
                                _isPlacingOrder.value = false
                                setLoading(false)
                                println("SharedOrderViewModel: Order failed, calling callback with false")
                                callback(false)
                            }
                            is Result.Loading -> {
                                // Loading state is already handled by _isPlacingOrder and setLoading
                            }
                        }
                    }
            } catch (e: Exception) {
                handleException(e, "Failed to place order", showAsEvent = true)
                _isPlacingOrder.value = false
                setLoading(false)
                println("SharedOrderViewModel: Exception occurred, calling callback with false")
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
            repository.getOrderById(orderId).first()
        } catch (e: Exception) {
            handleException(e, "Failed to load order details", showAsEvent = true)
            null
        }
    }
    
    /**
     * Update order status.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Boolean {
        return try {
            repository.updateOrderStatus(orderId, status)
            loadOrders() // Refresh orders list
            true
        } catch (e: Exception) {
            handleException(e, "Failed to update order status", showAsEvent = true)
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
            repository.cancelOrder(orderId)
                .catch { e ->
                    handleException(e, "Failed to cancel order")
                    setLoading(false)
                }
                .collect { success ->
                    if (success) {
                        loadOrders() // Refresh orders list
                    } else {
                        setError(
                            "Cancellation Failed",
                            "Unable to cancel order at this time",
                            ErrorHandler.ErrorCategory.SERVER,
                            showAsEvent = true
                        )
                    }
                    setLoading(false)
                }
            true
        } catch (e: Exception) {
            handleException(e, "Failed to cancel order", showAsEvent = true)
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
                    ErrorHandler.ErrorCategory.CLIENT,
                    showAsEvent = true
                )
                false
            } else {
                setError(
                    "Order Not Found",
                    "Cannot reorder - original order not found",
                    ErrorHandler.ErrorCategory.DATA,
                    showAsEvent = true
                )
                false
            }
        } catch (e: Exception) {
            handleException(e, "Failed to reorder items", showAsEvent = true)
            false
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
    /**
     * Get orders by status.
     */
    fun getOrdersByStatus(status: String): List<Order> {
        return _orders.value.filter { it.status.equals(status, ignoreCase = true) }
    }
    
    /**
     * Get total amount spent.
     */
    fun getTotalSpent(): Double {
        return _totalSpent.value
    }
    
    /**
     * Get order history with limit.
     */
    fun getOrderHistory(limit: Int = 10): List<Order> {
        return _orders.value.take(limit)
    }
    
    /**
     * Get recent orders (last 5).
     */
    fun getRecentOrders(): List<Order> {
        return _orders.value.take(5)
    }
    
    /**
     * Get orders by date range.
     */
    fun getOrdersByDateRange(startDate: String, endDate: String): List<Order> {
        return _orders.value.filter { order ->
            order.date >= startDate && order.date <= endDate
        }
    }
    
    /**
     * Get order statistics.
     */
    fun getOrderStatistics(): Map<String, Any> {
        val orders = _orders.value
        val statusCounts = orders.groupBy { it.status }.mapValues { it.value.size }
        val averageOrderValue = if (orders.isNotEmpty()) orders.sumOf { it.total } / orders.size else 0.0
        
        return mapOf(
            "totalOrders" to orders.size,
            "totalSpent" to _totalSpent.value,
            "averageOrderValue" to averageOrderValue,
            "statusBreakdown" to statusCounts
        )
    }
    
    /**
     * Check if order can be cancelled.
     */
    fun canCancelOrder(orderId: String): Boolean {
        val order = _orders.value.find { it.id == orderId }
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
        val order = _orders.value.find { it.id == orderId }
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