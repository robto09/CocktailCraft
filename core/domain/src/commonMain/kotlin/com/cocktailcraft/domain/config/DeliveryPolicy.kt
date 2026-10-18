package com.cocktailcraft.domain.config

import com.cocktailcraft.domain.model.Money

/**
 * Delivery pricing rules. Single source of truth — the cart and order
 * ViewModels delegate here instead of hardcoding fees/thresholds. (How long
 * delivery takes is copy, not policy: see OrderStatus.deliveryEstimate in
 * :presentation.)
 */
object DeliveryPolicy {
    /** Subtotals of at least this much ($50.00) deliver for free. */
    val FREE_DELIVERY_THRESHOLD = Money(5000)

    /** The delivery fee below [FREE_DELIVERY_THRESHOLD] ($5.99). */
    val STANDARD_FEE = Money(599)

    fun isFreeDelivery(subtotal: Money): Boolean = subtotal >= FREE_DELIVERY_THRESHOLD

    fun deliveryFee(subtotal: Money): Money = if (isFreeDelivery(subtotal)) Money.ZERO else STANDARD_FEE

    fun finalTotal(subtotal: Money): Money = subtotal + deliveryFee(subtotal)
}
