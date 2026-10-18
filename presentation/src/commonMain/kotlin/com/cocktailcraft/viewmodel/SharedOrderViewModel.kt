package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.sumOfMoney
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.ManageCartUseCase
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
import com.cocktailcraft.resources.error_reorder_failed
import com.cocktailcraft.resources.order_cancelled_message
import com.cocktailcraft.resources.order_reorder_added
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingNonCancellation
import com.cocktailcraft.viewmodel.state.OrderUiState
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
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
    private val placeOrderUseCase: PlaceOrderUseCase,
    // Reorder puts an order's items back in the cart
    private val manageCartUseCase: ManageCartUseCase
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
     * PlaceOrderUseCase computes the stored total from [cartItems] and
     * DeliveryPolicy; callers never pass one.
     * SKIE will convert this to Swift async function.
     */
    suspend fun placeOrder(cartItems: List<CocktailCartItem>): Boolean = when {
        _uiState.value.isPlacingOrder -> false
        cartItems.isEmpty() -> {
            clearError()
            setError(
                MR.strings.error_empty_cart_title,
                MR.strings.error_empty_cart_message,
                ErrorHandler.ErrorCategory.DATA
            )
            false
        }
        else -> {
            clearError()
            checkOut(cartItems)
        }
    }

    /** [placeOrder] for a non-empty cart, holding the in-flight guard while it runs. */
    private suspend fun checkOut(cartItems: List<CocktailCartItem>): Boolean {
        _uiState.update { it.copy(isPlacingOrder = true, isLoading = true) }
        return try {
            when (val result = placeOrderUseCase(cartItems)) {
                is Result.Success -> {
                    // The observed orders flow picks up the new order automatically
                    _uiState.update { it.copy(currentOrder = result.data) }
                    true
                }
                is Result.Error -> {
                    setError(
                        MR.strings.error_order_failed_title,
                        ErrorHandler.errorFromResult(result, MR.strings.error_place_order_failed.desc()).message,
                        ErrorHandler.ErrorCategory.SERVER
                    )
                    false
                }
            }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_place_order_failed)
            false
        } finally {
            // Always release the in-flight guard, or every later checkout is ignored
            _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
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
    
    /**
     * Reorder: puts order [orderId]'s items back in the cart with their
     * quantities (see ManageCartUseCase.reorder). Success is announced on
     * [events] ("Added 3 items to your cart"), and so is a failure (a toast
     * on the detail screen, like add-to-cart). An order that can't be
     * reordered ([OrderUiState.canReorderSelectedOrder] false) adds nothing.
     * A call while a reorder is in flight is ignored (returns false), so a
     * double tap can't add the items twice.
     * SKIE will convert this to Swift async function.
     */
    suspend fun reorder(orderId: String): Boolean {
        if (_uiState.value.isReordering) return false
        clearError()
        _uiState.update { it.copy(isReordering = true) }
        return try {
            val order = _uiState.value.selectedOrder?.takeIf { it.id == orderId }
                ?: orderRepository.getOrderById(orderId).getOrDefault(null)
            val result = runCatchingNonCancellation(onFailure = { Result.Error(it.message.orEmpty()) }) {
                order?.let { manageCartUseCase.reorder(it) } ?: Result.Error("No order $orderId")
            }
            if (result is Result.Success) {
                sendEvent(UiEvent.Success(MR.plurals.order_reorder_added.format(result.data, result.data)))
                true
            } else {
                sendEvent(UiEvent.Failure(MR.strings.error_reorder_failed.desc()))
                false
            }
        } finally {
            _uiState.update { it.copy(isReordering = false) }
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

/**
 * [orderList] as the list state, with the order on the detail screen
 * replaced by its fresh copy (a cancel shows there as soon as it lands).
 */
private fun OrderUiState.withOrders(orderList: List<Order>): OrderUiState = copy(
    orders = orderList.sortedByDescending { o -> o.date },
    orderCount = orderList.size,
    totalSpent = orderList.sumOfMoney { o -> o.total },
    selectedOrder = selectedOrder?.let { selected -> orderList.find { it.id == selected.id } ?: selected }
)
