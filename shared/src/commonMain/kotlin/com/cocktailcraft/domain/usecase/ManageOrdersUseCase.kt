package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result

class ManageOrdersUseCase(
    private val orderRepository: OrderRepository
) {
    suspend fun getOrders(): Result<List<Order>> {
        return orderRepository.getOrders()
    }

    suspend fun getOrderById(orderId: String): Result<Order?> {
        return orderRepository.getOrderById(orderId)
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return orderRepository.updateOrderStatus(orderId, status)
    }

    suspend fun cancelOrder(orderId: String): Result<Boolean> {
        return orderRepository.cancelOrder(orderId)
    }
}

