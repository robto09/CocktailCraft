package com.cocktailcraft.domain.model

import kotlin.native.HiddenFromObjC
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val date: String,
    val items: List<OrderItem>,
    val total: Double,
    val status: String
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
enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
} 