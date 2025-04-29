package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.usecase.ManageOrdersUseCase
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for the order screen.
 * Uses use cases instead of directly accessing repositories.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state for orders
 * - Handles user interactions like placing and canceling orders
 * - Provides a clean API for the UI layer
 * - Implements the IOrderViewModel interface for cross-platform compatibility
 */
class OrderViewModel(
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val manageOrdersUseCase: ManageOrdersUseCase
) : BaseViewModel(), IOrderViewModel {

    // Orders state
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    override val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    init {
        loadOrders()
    }

    /**
     * Load all orders.
     */
    override fun loadOrders() {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageOrdersUseCase.getOrderHistory(),
                onSuccess = { orderList ->
                    _orders.value = orderList
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load orders. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadOrders() }
            )
        }
    }

    /**
     * Add an order.
     */
    override fun addOrder(order: Order) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageOrdersUseCase.addOrder(order),
                onSuccess = { _ ->
                    loadOrders() // Refresh the orders list
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to add order. Please try again."
            )
        }
    }

    /**
     * Place an order directly (without using PlaceOrderUseCase).
     * This is kept for backward compatibility.
     */
    fun placeOrderDirectly(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        // Create order object
        val orderId = "ORD-${System.currentTimeMillis()}"
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Map cart items to order items
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                name = cartItem.cocktail.name,
                quantity = cartItem.quantity,
                price = cartItem.cocktail.price
            )
        }

        val order = Order(
            id = orderId,
            date = currentDate,
            items = orderItems,
            total = totalPrice,
            status = OrderStatus.SAVED
        )

        viewModelScope.launch {
            handleResultFlow(
                flow = manageOrdersUseCase.placeOrder(order),
                onSuccess = { success ->
                    if (success) {
                        loadOrders() // Refresh orders list on success
                    } else {
                        setError(
                            title = "Failed to Place Order",
                            message = "The order could not be processed. Please try again.",
                            category = ErrorUtils.ErrorCategory.DATA
                        )
                    }
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to place order. Please try again."
            )
        }
    }

    /**
     * Place an order using the PlaceOrderUseCase.
     */
    override fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        viewModelScope.launch {
            handleResultFlow(
                flow = placeOrderUseCase(cartItems, totalPrice),
                onSuccess = { _ ->
                    loadOrders() // Refresh the orders list
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to place order. Please try again."
            )
        }
    }

    /**
     * Update the status of an order.
     */
    override fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageOrdersUseCase.updateOrderStatus(orderId, status),
                onSuccess = { _ ->
                    loadOrders() // Refresh the orders list
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to update order status. Please try again."
            )
        }
    }

    /**
     * Cancel an order.
     */
    override fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageOrdersUseCase.cancelOrder(orderId),
                onSuccess = { success ->
                    if (success) {
                        loadOrders() // Refresh orders list on success
                    } else {
                        setError(
                            title = "Failed to Cancel Order",
                            message = "It may be too late to cancel this order.",
                            category = ErrorUtils.ErrorCategory.DATA
                        )
                    }
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to cancel order. Please try again."
            )
        }
    }

    /**
     * Get an order by ID.
     * Returns a Flow of Order? for backward compatibility.
     */
    override fun getOrderById(orderId: String): Flow<Order?> {
        return flow {
            setLoading(true)

            try {
                // First check if the order is in the current list
                orders.value.find { it.id == orderId }?.let {
                    emit(it)
                    return@flow
                }

                // If not found in current list, try to load from repository
                manageOrdersUseCase.getOrderById(orderId).collect { result ->
                    when (result) {
                        is Result.Success -> emit(result.data)
                        is Result.Error -> {
                            setError(
                                title = "Failed to Load Order Details",
                                message = result.message,
                                category = ErrorUtils.ErrorCategory.DATA
                            )
                            emit(null)
                        }
                        is Result.Loading -> {
                            // No action needed
                        }
                    }
                }
            } catch (e: Exception) {
                setError(
                    title = "Failed to Load Order Details",
                    message = e.message ?: "Unknown error occurred",
                    category = ErrorUtils.ErrorCategory.DATA
                )
                emit(null)
            } finally {
                setLoading(false)
            }
        }
    }
}