package com.cocktailcraft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val date: String,
    val items: List<OrderItem>,
    val total: Double,
    val status: OrderStatus = OrderStatus.PENDING
)

@Serializable
data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double
)

enum class OrderStatus {
    SAVED,      // Added to order list
    COMPLETED   // Made and enjoyed
}