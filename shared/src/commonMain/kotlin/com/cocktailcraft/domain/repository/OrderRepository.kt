package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<Order>>
    suspend fun getOrders(): Result<List<Order>>
    suspend fun addOrder(order: Order): Result<Unit>
    suspend fun getOrderById(id: String): Result<Order?>
    suspend fun updateOrderStatus(id: String, status: String): Result<Unit>
    suspend fun deleteOrder(id: String): Result<Unit>

    // Methods needed by the implementation
    suspend fun placeOrder(order: Order): Result<Boolean>
    suspend fun getOrderHistory(): Result<List<Order>>
    suspend fun cancelOrder(orderId: String): Result<Boolean>
}