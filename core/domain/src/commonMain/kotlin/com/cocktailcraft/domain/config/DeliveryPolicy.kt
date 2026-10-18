package com.cocktailcraft.domain.config

import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_unknown
import com.cocktailcraft.resources.delivery_estimate_minutes
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
    /** Subtotals of at least this much ($50.00) deliver for free. */
    val FREE_DELIVERY_THRESHOLD = Money(5000)

    /** The delivery fee below [FREE_DELIVERY_THRESHOLD] ($5.99). */
    val STANDARD_FEE = Money(599)

    fun isFreeDelivery(subtotal: Money): Boolean = subtotal >= FREE_DELIVERY_THRESHOLD

    fun deliveryFee(subtotal: Money): Money = if (isFreeDelivery(subtotal)) Money.ZERO else STANDARD_FEE

    fun finalTotal(subtotal: Money): Money = subtotal + deliveryFee(subtotal)

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
