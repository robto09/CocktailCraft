package com.coffee.store.domain.repository

import com.coffee.store.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun getOrders(): Flow<List<Order>>
    suspend fun addOrder(order: Order)
    suspend fun getOrderById(id: String): Flow<Order?>
    suspend fun updateOrderStatus(id: String, status: String)
    suspend fun deleteOrder(id: String)
    
    // Methods needed by the implementation
    suspend fun placeOrder(order: Order): Flow<Boolean>
    suspend fun getOrderHistory(): Flow<List<Order>>
    suspend fun cancelOrder(orderId: String): Flow<Boolean>
}