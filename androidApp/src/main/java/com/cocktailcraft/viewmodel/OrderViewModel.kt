package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
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
 */
class OrderViewModel : BaseViewModel() {

    // Use cases
    private val placeOrderUseCase: PlaceOrderUseCase by inject()
    private val manageOrdersUseCase: ManageOrdersUseCase by inject()

    // Orders state
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    init {
        loadOrders()
    }

    /**
     * Load all orders.
     */
    fun loadOrders() {
        executeWithErrorHandling(
            operation = {
                manageOrdersUseCase.getOrderHistory()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _orders.value = result.data
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Load Orders",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA,
                                    recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadOrders() }
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to load orders. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadOrders() }
        )
    }

    /**
     * Add an order.
     */
    fun addOrder(order: Order) {
        executeWithErrorHandling(
            operation = {
                manageOrdersUseCase.addOrder(order)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                loadOrders() // Refresh the orders list
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Add Order",
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
            defaultErrorMessage = "Failed to add order. Please try again."
        )
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
            status = "Processing"
        )

        executeWithErrorHandling(
            operation = {
                manageOrdersUseCase.placeOrder(order)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data) {
                                    loadOrders() // Refresh orders list on success
                                } else {
                                    setError(
                                        title = "Failed to Place Order",
                                        message = "The order could not be processed. Please try again.",
                                        category = ErrorUtils.ErrorCategory.DATA
                                    )
                                }
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Place Order",
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
            defaultErrorMessage = "Failed to place order. Please try again."
        )
    }

    /**
     * Place an order using the PlaceOrderUseCase.
     */
    fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        executeWithErrorHandling(
            operation = {
                placeOrderUseCase(cartItems, totalPrice)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                loadOrders() // Refresh the orders list
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Place Order",
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
            defaultErrorMessage = "Failed to place order. Please try again."
        )
    }

    /**
     * Update the status of an order.
     */
    fun updateOrderStatus(orderId: String, status: String) {
        executeWithErrorHandling(
            operation = {
                manageOrdersUseCase.updateOrderStatus(orderId, status)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                loadOrders() // Refresh the orders list
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Update Order Status",
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
            defaultErrorMessage = "Failed to update order status. Please try again."
        )
    }

    /**
     * Cancel an order.
     */
    fun cancelOrder(orderId: String) {
        executeWithErrorHandling(
            operation = {
                manageOrdersUseCase.cancelOrder(orderId)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data) {
                                    loadOrders() // Refresh orders list on success
                                } else {
                                    setError(
                                        title = "Failed to Cancel Order",
                                        message = "It may be too late to cancel this order.",
                                        category = ErrorUtils.ErrorCategory.DATA
                                    )
                                }
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Cancel Order",
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
            defaultErrorMessage = "Failed to cancel order. Please try again."
        )
    }

    /**
     * Get an order by ID.
     * Returns a Flow of Order? for backward compatibility.
     */
    fun getOrderById(orderId: String): Flow<Order?> {
        return flow {
            setLoading(true)

            // First check if the order is in the current list
            orders.value.find { it.id == orderId }?.let {
                emit(it)
                setLoading(false)
                return@flow
            }

            // If not found in current list, try to load from repository
            executeWithErrorHandling(
                operation = {
                    manageOrdersUseCase.getOrderById(orderId)
                },
                onSuccess = { resultFlow ->
                    viewModelScope.launch {
                        resultFlow.collect { result ->
                            when (result) {
                                is Result.Success -> {
                                    emit(result.data)
                                }
                                is Result.Error -> {
                                    setError(
                                        title = "Failed to Load Order Details",
                                        message = result.message,
                                        category = ErrorUtils.ErrorCategory.DATA
                                    )
                                    emit(null)
                                }
                                is Result.Loading -> {
                                    // Already handled by executeWithErrorHandling
                                }
                            }
                            setLoading(false)
                        }
                    }
                },
                defaultErrorMessage = "Failed to load order details. Please try again.",
                showLoading = false
            )
        }
    }
}