package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<Order>>
    suspend fun getOrders(): Result<List<Order>>
    suspend fun placeOrder(order: Order): Result<Unit>

    /**
     * Checkout: stores [order] and empties the cart atomically — either both
     * happen or neither does, so a failed placement never loses the cart and
     * a placed order never leaves it behind.
     */
    suspend fun placeOrderAndClearCart(order: Order): Result<Unit>

    suspend fun getOrderById(id: String): Result<Order?>
    suspend fun updateOrderStatus(id: String, status: OrderStatus): Result<Unit>
    suspend fun deleteOrder(id: String): Result<Unit>

    /** Cancels a [OrderStatus.isCancellable] order; false when it isn't (or doesn't exist). */
    suspend fun cancelOrder(orderId: String): Result<Boolean>
}
