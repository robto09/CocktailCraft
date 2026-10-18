package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.config.DeliveryPolicy
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import dev.icerock.moko.resources.desc.StringDesc

/**
 * UI state for the order detail screen: one [order], kept current from the
 * observed orders (e.g. after a cancel). [order] is null both while the
 * first lookup runs ([isLoading]) and once it found no such order.
 */
data class OrderDetailUiState(
    val order: Order? = null,
    // True until the first lookup is done, so the screen can tell
    // "loading" from "not found"
    val isLoading: Boolean = true,
    val isCancelling: Boolean = false,
    val isReordering: Boolean = false
) {
    /** Sum of the order's line items, before delivery. */
    val subtotal: Money
        get() = order?.subtotal ?: Money.ZERO

    /**
     * The delivery fee the order was charged: what its stored total
     * (items + DeliveryPolicy fee at checkout) adds to the items.
     */
    val deliveryFee: Money
        get() = order?.deliveryFee ?: Money.ZERO

    /** Delivery estimate for the order's status, from DeliveryPolicy. */
    val deliveryEstimate: StringDesc
        get() = DeliveryPolicy.estimatedDeliveryTimeForStatus(order?.status)

    /** Whether the screen offers "Cancel order" (shown busy while [isCancelling]). */
    val canCancel: Boolean
        get() = order?.status?.isCancellable == true

    /**
     * Whether Reorder is enabled (shown busy while [isReordering]). Orders
     * placed before items recorded their cocktail show it disabled, with
     * order_reorder_unavailable as the reason.
     */
    val canReorder: Boolean
        get() = order?.canReorder == true
}
