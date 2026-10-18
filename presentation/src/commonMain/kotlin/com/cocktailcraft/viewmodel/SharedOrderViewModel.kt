package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_cancel_order_failed
import com.cocktailcraft.resources.error_cancellation_failed_message
import com.cocktailcraft.resources.error_cancellation_failed_title
import com.cocktailcraft.resources.error_empty_cart_message
import com.cocktailcraft.resources.error_empty_cart_title
import com.cocktailcraft.resources.error_load_order_details_failed
import com.cocktailcraft.resources.error_load_orders_failed
import com.cocktailcraft.resources.error_order_failed_title
import com.cocktailcraft.resources.error_place_order_failed
import com.cocktailcraft.resources.error_update_order_status_failed
import com.cocktailcraft.resources.order_cancelled_message
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.OrderUiState
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
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
                _uiState.update { it.withOrders(orderList).copy(isLoading = false) }
            }
        }
    }

    /**
     * [orderList] as the list state, with the order on the detail screen
     * replaced by its fresh copy (a cancel shows there as soon as it lands).
     */
    private fun OrderUiState.withOrders(orderList: List<Order>): OrderUiState = copy(
        orders = orderList.sortedByDescending { o -> o.date },
        orderCount = orderList.size,
        totalSpent = orderList.sumOf { o -> o.total },
        selectedOrder = selectedOrder?.let { selected -> orderList.find { it.id == selected.id } ?: selected }
    )
    
    /**
     * Load orders from the repository.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadOrders() {
        clearError()
        _uiState.update { it.copy(isLoading = true) }
        try {
            val orderList = orderRepository.getOrders().getOrThrow()
            _uiState.update { it.withOrders(orderList).copy(isLoading = false) }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_load_orders_failed)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Check out: place an order for [cartItems] and empty the cart, atomically.
     * On success the cart is already empty (SharedCartViewModel picks that up
     * from its observed flow); on failure it is untouched. Callers must not
     * clear the cart themselves. A call while an order is already being
     * placed is ignored (returns false), so a double tap can't order twice.
     * [totalPrice] is ignored: PlaceOrderUseCase computes the stored total
     * from [cartItems] and DeliveryPolicy. It stays only so the platform call
     * sites keep compiling.
     * SKIE will convert this to Swift async function.
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double): Boolean {
        if (_uiState.value.isPlacingOrder) return false
        clearError()

        if (cartItems.isEmpty()) {
            setError(
                MR.strings.error_empty_cart_title,
                MR.strings.error_empty_cart_message,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        _uiState.update { it.copy(isPlacingOrder = true, isLoading = true) }

        try {
            val result = placeOrderUseCase(cartItems)
            return when (result) {
                is Result.Success -> {
                    // The observed orders flow picks up the new order automatically
                    _uiState.update { it.copy(currentOrder = result.data, isPlacingOrder = false, isLoading = false) }
                    true
                }
                is Result.Error -> {
                    setError(
                        MR.strings.error_order_failed_title,
                        ErrorHandler.rawMessage(result.message)
                            ?: MR.strings.error_place_order_failed.desc(),
                        ErrorHandler.ErrorCategory.SERVER
                    )
                    _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
                    false
                }
                is Result.Loading -> {
                    // Release the in-flight guard, or every later checkout is ignored
                    _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
                    false
                }
            }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_place_order_failed)
            _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
            return false
        }
    }

    /**
     * Get order by ID.
     * SKIE will convert this to Swift async function returning optional.
     */
    suspend fun getOrderById(orderId: String): Order? {
        clearError()
        return try {
            orderRepository.getOrderById(orderId)
                .getOrReport(null, MR.strings.error_load_order_details_failed)
        } catch (e: Exception) {
            handleException(e, MR.strings.error_load_order_details_failed)
            null
        }
    }
    
    /**
     * Load the order the detail screen shows into [OrderUiState.selectedOrder]
     * (null when there is no such order, or the lookup failed — which is
     * reported on [error]). While it loads, a different previously selected
     * order is cleared so the screen never shows the wrong one.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadOrderDetail(orderId: String) {
        _uiState.update { state ->
            state.copy(
                selectedOrder = state.selectedOrder?.takeIf { it.id == orderId },
                isLoadingOrderDetail = true
            )
        }
        val order = getOrderById(orderId)
        _uiState.update { it.copy(selectedOrder = order, isLoadingOrderDetail = false) }
    }

    /**
     * Update order status.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Boolean {
        clearError()
        return try {
            val result = orderRepository.updateOrderStatus(orderId, status)
            if (result.isSuccess()) {
                loadOrders() // Refresh orders list
                true
            } else {
                handleException(Exception("Failed to update order status"), MR.strings.error_update_order_status_failed)
                false
            }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_order_status_failed)
            false
        }
    }
    
    /**
     * Cancel an order. Success is announced on [events] ("Order cancelled")
     * and the list and detail state pick up the new status; a failure is
     * reported on [error]. A call while a cancel is in flight is ignored
     * (returns false), so a double tap can't cancel twice.
     * SKIE will convert this to Swift async function.
     */
    suspend fun cancelOrder(orderId: String): Boolean {
        if (_uiState.value.isCancellingOrder) return false
        clearError()
        _uiState.update { it.copy(isCancellingOrder = true) }
        return try {
            val success = orderRepository.cancelOrder(orderId).getOrDefault(false)
            if (success) {
                loadOrders()
                sendEvent(UiEvent.Success(MR.strings.order_cancelled_message.desc()))
            } else {
                setError(
                    MR.strings.error_cancellation_failed_title,
                    MR.strings.error_cancellation_failed_message,
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
            success
        } catch (e: Exception) {
            handleException(e, MR.strings.error_cancel_order_failed)
            false
        } finally {
            _uiState.update { it.copy(isCancellingOrder = false) }
        }
    }
    
// MARK: - Synchronous Helper Methods
    
    /**
     * Get order status display text.
     */
    fun getOrderStatusDisplay(status: OrderStatus): StringDesc = status.displayName
    
    /**
     * Refresh orders data.
     */
    fun refresh() {
        viewModelScope.launch {
            loadOrders()
        }
    }
}