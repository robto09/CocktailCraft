package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.Order
import dev.icerock.moko.resources.desc.StringDesc

/**
 * Consolidated UI state for the Order screens: the order list and the order
 * detail screen ([selectedOrder], loaded with loadOrderDetail and kept
 * current from the observed orders, e.g. after a cancel).
 */
data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val currentOrder: Order? = null,
    val orderCount: Int = 0,
    val isPlacingOrder: Boolean = false,
    val totalSpent: Double = 0.0,
    val isLoading: Boolean = false,
    val selectedOrder: Order? = null,
    val isLoadingOrderDetail: Boolean = false,
    val isCancellingOrder: Boolean = false
) {
    /** Sum of the selected order's line items, before delivery. */
    val selectedOrderSubtotal: Double
        get() = selectedOrder?.items?.sumOf { it.price * it.quantity } ?: 0.0

    /**
     * The delivery fee the selected order was charged: what its stored
     * total (items + DeliveryPolicy fee at checkout) adds to the items.
     */
    val selectedOrderDeliveryFee: Double
        get() = selectedOrder?.let { (it.total - selectedOrderSubtotal).coerceAtLeast(0.0) } ?: 0.0

    /** Delivery estimate for the selected order's status, from DeliveryPolicy. */
    val selectedOrderDeliveryEstimate: StringDesc
        get() = DeliveryPolicy.estimatedDeliveryTimeForStatus(selectedOrder?.status)

    /**
     * Whether the detail screen offers "Cancel order" (shown busy while
     * [isCancellingOrder]).
     */
    val canCancelSelectedOrder: Boolean
        get() = selectedOrder?.status?.isCancellable == true
}
