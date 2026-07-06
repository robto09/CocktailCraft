package com.cocktailcraft.domain.config

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
    fun estimatedDeliveryTime(itemCount: Int): String = when {
        itemCount == 0 -> "No items"
        itemCount <= 3 -> "15-20 minutes"
        itemCount <= 6 -> "20-25 minutes"
        else -> "25-30 minutes"
    }

    /** Estimated delivery time for an order in the given status. */
    fun estimatedDeliveryTimeForStatus(status: String?): String = when (status?.lowercase()) {
        "pending" -> "Processing will begin shortly"
        "processing" -> "15-25 minutes"
        "shipped" -> "5-10 minutes"
        "delivered" -> "Delivered"
        "cancelled" -> "Cancelled"
        else -> "Unknown"
    }
}
