package com.cocktailcraft.domain.model

import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.order_status_cancelled
import com.cocktailcraft.resources.order_status_delivered
import com.cocktailcraft.resources.order_status_pending
import com.cocktailcraft.resources.order_status_processing
import com.cocktailcraft.resources.order_status_shipped
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlin.native.HiddenFromObjC

// The JSON shape only matters for the legacy Settings import (orders live in
// database columns since); OrderSerializer also reads its pre-cents totals.
@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = OrderSerializer::class)
data class Order(
    val id: String,
    val date: String,
    val items: List<OrderItem>,
    /** Items plus the delivery fee charged at checkout. */
    @SerialName("totalCents")
    val total: Money,
    // Stored (database column, legacy Settings JSON) as the status's
    // storageValue, e.g. "Processing" — the strings written before the enum
    @Serializable(with = OrderStatusSerializer::class)
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

    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}

@OptIn(ExperimentalSerializationApi::class)
@KeepGeneratedSerializer
@Serializable(with = OrderItemSerializer::class)
data class OrderItem(
    val name: String,
    val quantity: Int,
    /** Unit price at checkout. */
    @SerialName("priceCents")
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

    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}

/** Order JSON, also reading the pre-cents shape (`"total": 33.99` dollars). */
@OptIn(ExperimentalSerializationApi::class)
internal object OrderSerializer : JsonTransformingSerializer<Order>(Order.generatedSerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withLegacyDollarsAsCents(dollarsKey = "total", centsKey = "totalCents")
}

/** Order item JSON, also reading the pre-cents shape (`"price": 12.5` dollars). */
@OptIn(ExperimentalSerializationApi::class)
internal object OrderItemSerializer : JsonTransformingSerializer<OrderItem>(OrderItem.generatedSerializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withLegacyDollarsAsCents(dollarsKey = "price", centsKey = "priceCents")
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

/** (De)serializes [OrderStatus] as its [OrderStatus.storageValue]. */
internal object OrderStatusSerializer : KSerializer<OrderStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.cocktailcraft.domain.model.OrderStatus", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: OrderStatus) = encoder.encodeString(value.storageValue)

    override fun deserialize(decoder: Decoder): OrderStatus = OrderStatus.fromStorageValue(decoder.decodeString())
}
