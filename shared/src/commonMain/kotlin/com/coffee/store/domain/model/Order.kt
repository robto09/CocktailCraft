package com.coffee.store.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    val date: String,
    val items: List<OrderItem>,
    val total: Double,
    val status: String
)

@Serializable
data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double
)

enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
} 