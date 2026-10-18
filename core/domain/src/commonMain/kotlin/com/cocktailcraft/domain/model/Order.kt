package com.cocktailcraft.domain.model

import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.order_status_cancelled
import com.cocktailcraft.resources.order_status_delivered
import com.cocktailcraft.resources.order_status_pending
import com.cocktailcraft.resources.order_status_processing
import com.cocktailcraft.resources.order_status_shipped
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.native.HiddenFromObjC

data class Order(
    val id: String,
    val date: String,
    val items: List<OrderItem>,
    /** Items plus the delivery fee charged at checkout. */
    val total: Money,
    // Stored (database column, legacy Settings JSON) as the status's
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
 * Lifecycle of an order. [storageValue] is what the database and the legacy
 * Settings JSON hold — the exact strings written back when status was a raw
 * String — so existing rows keep reading back as the same status.
 */
enum class OrderStatus(val storageValue: String) {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    /** Only orders not yet out for delivery can still be cancelled. */
    val isCancellable: Boolean
        get() = this == PENDING || this == PROCESSING

    /** Localized label for the order list. */
    val displayName: StringDesc
        get() = when (this) {
            PENDING -> MR.strings.order_status_pending.desc()
            PROCESSING -> MR.strings.order_status_processing.desc()
            SHIPPED -> MR.strings.order_status_shipped.desc()
            DELIVERED -> MR.strings.order_status_delivered.desc()
            CANCELLED -> MR.strings.order_status_cancelled.desc()
        }

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
