package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.OrderUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Order functionality.
 * Uses consolidated [OrderUiState] for atomic state updates; [uiState] is the
 * only public state surface.
 */
class SharedOrderViewModel internal constructor(
    // Direct repository dependency (AR-8): ManageOrdersUseCase was a pure
    // 1:1 pass-through with this VM as its only consumer.
    private val orderRepository: OrderRepository,
    private val placeOrderUseCase: PlaceOrderUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        // The repository exposes a hot StateFlow seeded from persistence —
        // collecting it is the single way orders enter this ViewModel.
        viewModelScope.launch {
            orderRepository.observeOrders().collect { orderList ->
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
     * Load orders from the repository.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadOrders() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val orderList = orderRepository.getOrders().getOrThrow()
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
     * Check out: place an order for [cartItems] and empty the cart, atomically.
     * On success the cart is already empty (SharedCartViewModel picks that up
     * from its observed flow); on failure it is untouched. Callers must not
     * clear the cart themselves.
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
            orderRepository.getOrderById(orderId).getOrNull()
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
            val result = orderRepository.updateOrderStatus(orderId, status)
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
            val success = orderRepository.cancelOrder(orderId).getOrDefault(false)
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
     * Refresh orders data.
     */
    fun refresh() {
        viewModelScope.launch {
            loadOrders()
        }
    }
}