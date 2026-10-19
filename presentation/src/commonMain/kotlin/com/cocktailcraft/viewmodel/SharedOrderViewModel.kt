package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.sumOfMoney
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_empty_cart_message
import com.cocktailcraft.resources.error_empty_cart_title
import com.cocktailcraft.resources.error_load_orders_failed
import com.cocktailcraft.resources.error_order_failed_title
import com.cocktailcraft.resources.error_place_order_failed
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingResult
import com.cocktailcraft.viewmodel.state.OrderUiState
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for the order list and checkout.
 * Uses consolidated [OrderUiState] for atomic state updates; [uiState] is the
 * only public state surface. One order's screen has its own, screen-scoped
 * SharedOrderDetailViewModel.
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
     * Reload the orders from the repository. Returns once they are in, so a
     * refresh spinner lasts exactly as long.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun loadOrders() {
        runInViewModelScope {
            clearError()
            _uiState.update { it.copy(isLoading = true) }
            val orderList = runCatchingResult("Failed to load orders") { orderRepository.getOrders() }
                .getOrReport(null, MR.strings.error_load_orders_failed)
            _uiState.update { state -> (orderList?.let(state::withOrders) ?: state).copy(isLoading = false) }
        }
    }

    /**
     * Check out: place an order for [cartItems] and empty the cart, atomically.
     * On success the cart is already empty (SharedCartViewModel picks that up
     * from its observed flow); on failure it is untouched. Callers must not
     * clear the cart themselves. A call while an order is already being
     * placed is ignored (returns false), so a double tap can't order twice.
     * PlaceOrderUseCase computes the stored total from [cartItems] and
     * DeliveryPolicy; callers never pass one. Returns whether the order was
     * placed, so the caller can move on to the order list.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun placeOrder(cartItems: List<CocktailCartItem>): Boolean = runInViewModelScope {
        when {
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
    }

    /** [placeOrder] for a non-empty cart, holding the in-flight guard while it runs. */
    private suspend fun checkOut(cartItems: List<CocktailCartItem>): Boolean {
        _uiState.update { it.copy(isPlacingOrder = true, isLoading = true) }
        val result = runCatchingResult("Failed to place order") { placeOrderUseCase(cartItems) }
        when (result) {
            // The observed orders flow picks up the new order automatically
            is Result.Success -> _uiState.update { it.copy(currentOrder = result.data) }
            is Result.Error -> setError(
                MR.strings.error_order_failed_title,
                ErrorHandler.errorFromResult(result, MR.strings.error_place_order_failed.desc()).message,
                ErrorHandler.ErrorCategory.SERVER
            )
        }
        // Always release the in-flight guard, or every later checkout is ignored
        _uiState.update { it.copy(isPlacingOrder = false, isLoading = false) }
        return result is Result.Success
    }

    /** [loadOrders] for callers that don't wait for it. */
    fun refresh() {
        viewModelScope.launch {
            loadOrders()
        }
    }
}

/** [orderList] as the list state, newest first. */
private fun OrderUiState.withOrders(orderList: List<Order>): OrderUiState = copy(
    orders = orderList.sortedByDescending { o -> o.date },
    orderCount = orderList.size,
    totalSpent = orderList.sumOfMoney { o -> o.total }
)
