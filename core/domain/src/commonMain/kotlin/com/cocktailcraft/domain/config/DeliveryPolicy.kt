package com.cocktailcraft.domain.config

import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_unknown
import com.cocktailcraft.resources.delivery_estimate_minutes
import com.cocktailcraft.resources.delivery_estimate_no_items
import com.cocktailcraft.resources.delivery_estimate_processing_soon
import com.cocktailcraft.resources.order_status_cancelled
import com.cocktailcraft.resources.order_status_delivered
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format

/**
 * Delivery pricing and timing rules. Single source of truth — the cart and
 * order ViewModels delegate here instead of hardcoding fees/thresholds.
 */
object DeliveryPolicy {
    const val FREE_DELIVERY_THRESHOLD = 50.0
    const val STANDARD_FEE = 5.99

    fun isFreeDelivery(subtotal: Double): Boolean = subtotal >= FREE_DELIVERY_THRESHOLD

    fun deliveryFee(subtotal: Double): Double = if (isFreeDelivery(subtotal)) 0.0 else STANDARD_FEE

    fun finalTotal(subtotal: Double): Double = subtotal + deliveryFee(subtotal)

    /** Estimated delivery time for a cart of [itemCount] items. */
    fun estimatedDeliveryTime(itemCount: Int): StringDesc = when {
        itemCount == 0 -> MR.strings.delivery_estimate_no_items.desc()
        itemCount <= 3 -> minutes(15, 20)
        itemCount <= 6 -> minutes(20, 25)
        else -> minutes(25, 30)
    }

    /** Estimated delivery time for an order in the given status. */
    fun estimatedDeliveryTimeForStatus(status: OrderStatus?): StringDesc = when (status) {
        OrderStatus.PENDING -> MR.strings.delivery_estimate_processing_soon.desc()
        OrderStatus.PROCESSING -> minutes(15, 25)
        OrderStatus.SHIPPED -> minutes(5, 10)
        OrderStatus.DELIVERED -> MR.strings.order_status_delivered.desc()
        OrderStatus.CANCELLED -> MR.strings.order_status_cancelled.desc()
        null -> MR.strings.common_unknown.desc()
    }

    private fun minutes(from: Int, to: Int): StringDesc =
        MR.strings.delivery_estimate_minutes.format(from, to)
}
