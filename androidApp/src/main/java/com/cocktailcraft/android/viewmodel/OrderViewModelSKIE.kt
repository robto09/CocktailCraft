package com.cocktailcraft.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedOrderViewModel using SKIE.
 * This replaces the old OrderViewModel with a thin wrapper around the shared implementation.
 * 
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class OrderViewModelSKIE : ViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedOrderViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val orders: StateFlow<List<Order>> = sharedViewModel.orders
    val currentOrder: StateFlow<Order?> = sharedViewModel.currentOrder
    val orderCount: StateFlow<Int> = sharedViewModel.orderCount
    val isPlacingOrder: StateFlow<Boolean> = sharedViewModel.isPlacingOrder
    val totalSpent: StateFlow<Double> = sharedViewModel.totalSpent
    
    // Expose loading and error from shared base class
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    val errorEventState = sharedViewModel.errorEvent
    
    // Computed properties
    val hasOrders: Boolean
        get() = sharedViewModel.hasOrders
    
    val isEmpty: Boolean
        get() = sharedViewModel.isEmpty
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Load orders using SKIE async interop
     */
    fun loadOrders() {
        viewModelScope.launch {
            sharedViewModel.loadOrders()
        }
    }
    
    /**
     * Place a new order using SKIE async interop
     */
    fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        viewModelScope.launch {
            sharedViewModel.placeOrder(cartItems, totalPrice)
        }
    }
    
    /**
     * Get order by ID using SKIE async interop
     */
    fun getOrderById(orderId: String) {
        viewModelScope.launch {
            sharedViewModel.getOrderById(orderId)
        }
    }
    
    /**
     * Update order status using SKIE async interop
     */
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            sharedViewModel.updateOrderStatus(orderId, status)
        }
    }
    
    /**
     * Cancel order using SKIE async interop
     */
    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            sharedViewModel.cancelOrder(orderId)
        }
    }
    
    /**
     * Reorder items using SKIE async interop
     */
    fun reorderItems(orderId: String) {
        viewModelScope.launch {
            sharedViewModel.reorderItems(orderId)
        }
    }
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Get orders by status
     */
    fun getOrdersByStatus(status: String): List<Order> {
        return sharedViewModel.getOrdersByStatus(status)
    }
    
    /**
     * Get total amount spent
     */
    fun getTotalSpent(): Double {
        return sharedViewModel.getTotalSpent()
    }
    
    /**
     * Get order history with limit
     */
    fun getOrderHistory(limit: Int = 10): List<Order> {
        return sharedViewModel.getOrderHistory(limit)
    }
    
    /**
     * Get recent orders
     */
    fun getRecentOrders(): List<Order> {
        return sharedViewModel.getRecentOrders()
    }
    
    /**
     * Get orders by date range
     */
    fun getOrdersByDateRange(startDate: String, endDate: String): List<Order> {
        return sharedViewModel.getOrdersByDateRange(startDate, endDate)
    }
    
    /**
     * Get order statistics
     */
    fun getOrderStatistics(): Map<String, Any> {
        return sharedViewModel.getOrderStatistics()
    }
    
    /**
     * Check if order can be cancelled
     */
    fun canCancelOrder(orderId: String): Boolean {
        return sharedViewModel.canCancelOrder(orderId)
    }
    
    /**
     * Get order status display text
     */
    fun getOrderStatusDisplay(status: String): String {
        return sharedViewModel.getOrderStatusDisplay(status)
    }
    
    /**
     * Get estimated delivery time for an order
     */
    fun getEstimatedDeliveryTime(orderId: String): String {
        return sharedViewModel.getEstimatedDeliveryTime(orderId)
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Refresh orders data
     */
    fun refresh() {
        sharedViewModel.refresh()
    }
    
    /**
     * Clean up when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        sharedViewModel.onCleared()
    }
}