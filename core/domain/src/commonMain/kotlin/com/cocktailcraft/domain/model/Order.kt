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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Order(
    val id: String,
    val date: String,
    val items: List<OrderItem>,
    val total: Double,
    // Stored (database column, legacy Settings JSON) as the status's
    // storageValue, e.g. "Processing" — the strings written before the enum
    @Serializable(with = OrderStatusSerializer::class)
    val status: OrderStatus
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
}
@Serializable
data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double
) {
    // Hidden so the generated serializer does not drag the
    // kotlinx-serialization type tree into the Obj-C header.
    @HiddenFromObjC
    companion object
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
