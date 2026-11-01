package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC

data class Order(
    val id: String,
    val date: String,
    val items: List<OrderItem>,
    /** Items plus the delivery fee charged at checkout. */
    val total: Money,
    // Stored (database column) as the status's
    // storageValue, e.g. "Processing" — the strings written before the enum
    val status: OrderStatus
) {
    /** Sum of the line items, before delivery. */
    val subtotal: Money
        get() = items.sumOfMoney { it.lineTotal }

    /**
     * The delivery fee this order was charged: what its stored [total]
     * (items + the DeliveryPolicy fee at checkout) adds to the items.
     */
    val deliveryFee: Money
        get() = (total - subtotal).coerceAtLeast(Money.ZERO)

    /**
     * Whether the order can be placed again: every item knows its cocktail.
     * Items of orders placed before database version 5 don't.
     */
    val canReorder: Boolean
        get() = items.isNotEmpty() && items.all { it.cocktailId != null }
}

data class OrderItem(
    val name: String,
    val quantity: Int,
    /** Unit price at checkout. */
    val price: Money,
    /**
     * The ordered cocktail, so the order can be placed again. Null for items
     * of orders placed before it was recorded (database version 5).
     */
    val cocktailId: String? = null
) {
    /** [price] times [quantity]. */
    val lineTotal: Money
        get() = price * quantity
}

/**
 * Lifecycle of an order. [storageValue] is what the database holds —
 * the exact strings written back when status was a raw
 * String — so existing rows keep reading back as the same status.
 */
enum class OrderStatus(val storageValue: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    /**
     * Only orders not yet out for delivery can still be cancelled. (The
     * status's label lives in :presentation, OrderStatus.displayName.)
     */
    val isCancellable: Boolean
        get() = this == PENDING || this == PROCESSING

    @HiddenFromObjC
    companion object {
        /**
         * Parses a stored status, case-insensitively. Every value this app
         * ever wrote is one of the [storageValue]s; anything else (only
         * possible from a hand-edited store) reads as [PROCESSING], the
         * status every order is placed in, rather than failing the whole
         * order list.
         */
        fun fromStorageValue(value: String): OrderStatus =
            entries.firstOrNull { it.storageValue.equals(value.trim(), ignoreCase = true) }
                ?: PROCESSING
    }
}
