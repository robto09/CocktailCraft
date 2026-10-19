package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_cancel_order_failed
import com.cocktailcraft.resources.error_cancellation_failed_message
import com.cocktailcraft.resources.error_cancellation_failed_title
import com.cocktailcraft.resources.error_load_order_details_failed
import com.cocktailcraft.resources.error_reorder_failed
import com.cocktailcraft.resources.order_cancelled_message
import com.cocktailcraft.resources.order_reorder_added
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingResult
import com.cocktailcraft.viewmodel.state.OrderDetailUiState
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The order detail screen: one order, Cancel and Reorder.
 *
 * Screen-scoped (a new instance per screen, cleared with it), so its state
 * never shows an order an earlier visit left behind, and its [events] —
 * "Order cancelled", "Added 3 items to your cart", a failed reorder —
 * belong to this screen.
 */
class SharedOrderDetailViewModel internal constructor(
    private val orderRepository: OrderRepository,
    // Reorder puts an order's items back in the cart
    private val manageCartUseCase: ManageCartUseCase
) : SharedViewModel() {

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    /**
     * Show order [orderId]: looked up once (a failure is reported on
     * [error]), then kept current from the observed orders, so a cancel
     * shows as soon as it lands. Also the Retry after a failed lookup.
     */
    fun loadOrder(orderId: String) {
        clearError()
        _uiState.update { it.copy(order = it.order?.takeIf { order -> order.id == orderId }, isLoading = true) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val order = runCatchingResult("Failed to load order") { orderRepository.getOrderById(orderId) }
                .getOrReport(null, MR.strings.error_load_order_details_failed)
            _uiState.update { it.copy(order = order, isLoading = false) }

            orderRepository.observeOrders().collect { orders ->
                val fresh = orders.find { it.id == orderId } ?: return@collect
                _uiState.update { it.copy(order = fresh) }
            }
        }
    }

    /**
     * Cancel the shown order. Success is announced on [events] ("Order
     * cancelled") and the order picks up its new status; a failure is
     * reported on [error]. A tap while a cancel is in flight is ignored, so
     * a double tap can't cancel twice.
     */
    fun cancelOrder() {
        val state = _uiState.value
        val order = state.order
        if (order == null || state.isCancelling) return
        clearError()
        _uiState.update { it.copy(isCancelling = true) }
        viewModelScope.launch {
            val result = runCatchingResult("Failed to cancel order") { orderRepository.cancelOrder(order.id) }
            when {
                result is Result.Error -> result.getOrReport(Unit, MR.strings.error_cancel_order_failed)
                // The repository refused: the order is past cancelling
                result.getOrNull() != true -> setError(
                    MR.strings.error_cancellation_failed_title,
                    MR.strings.error_cancellation_failed_message,
                    ErrorHandler.ErrorCategory.SERVER
                )
                else -> sendEvent(UiEvent.Success(MR.strings.order_cancelled_message.desc()))
            }
            _uiState.update { it.copy(isCancelling = false) }
        }
    }

    /**
     * Reorder: puts the shown order's items back in the cart with their
     * quantities (see ManageCartUseCase.reorder). Success is announced on
     * [events] ("Added 3 items to your cart"), and so is a failure (a toast,
     * like add-to-cart). An order that can't be reordered
     * ([OrderDetailUiState.canReorder] false) adds nothing. A tap while a
     * reorder is in flight is ignored, so a double tap can't add the items
     * twice.
     */
    fun reorder() {
        val state = _uiState.value
        val order = state.order
        if (order == null || state.isReordering) return
        clearError()
        _uiState.update { it.copy(isReordering = true) }
        viewModelScope.launch {
            val result = runCatchingResult("Failed to reorder") { manageCartUseCase.reorder(order) }
            sendEvent(
                if (result is Result.Success) {
                    UiEvent.Success(MR.plurals.order_reorder_added.format(result.data, result.data))
                } else {
                    UiEvent.Failure(MR.strings.error_reorder_failed.desc())
                }
            )
            _uiState.update { it.copy(isReordering = false) }
        }
    }
}
