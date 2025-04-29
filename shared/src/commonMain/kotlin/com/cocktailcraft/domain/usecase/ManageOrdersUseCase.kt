package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Use case for managing order operations.
 * This use case handles the business logic of order management,
 * including error handling and result transformation.
 */
class ManageOrdersUseCase(
    private val orderRepository: OrderRepository
) {
    /**
     * Get all orders.
     * @return Flow of Result containing either a list of orders or an error
     */
    suspend fun getOrderHistory(): Flow<Result<List<Order>>> = flow {
        try {
            orderRepository.getOrderHistory().collect { orders ->
                emit(Result.Success(orders))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to get order history"))
        }
    }
    
    /**
     * Get an order by ID.
     * @param orderId The ID of the order to retrieve
     * @return Flow of Result containing either the order or an error
     */
    suspend fun getOrderById(orderId: String): Flow<Result<Order?>> = flow {
        try {
            orderRepository.getOrderById(orderId).collect { order ->
                emit(Result.Success(order))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to get order details"))
        }
    }
    
    /**
     * Add an order.
     * @param order The order to add
     * @return Flow of Result containing either a success message or an error
     */
    suspend fun addOrder(order: Order): Flow<Result<String>> = flow {
        try {
            orderRepository.addOrder(order)
            emit(Result.Success("Order added successfully"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to add order"))
        }
    }
    
    /**
     * Place an order.
     * @param order The order to place
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun placeOrder(order: Order): Flow<Result<Boolean>> = flow {
        try {
            orderRepository.placeOrder(order).collect { success ->
                emit(Result.Success(success))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to place order"))
        }
    }
    
    /**
     * Update the status of an order.
     * @param orderId The ID of the order to update
     * @param status The new status
     * @return Flow of Result containing either a success message or an error
     */
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Flow<Result<String>> = flow {
        try {
            orderRepository.updateOrderStatus(orderId, status)
            emit(Result.Success("Order status updated successfully"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to update order status"))
        }
    }
    
    /**
     * Cancel an order.
     * @param orderId The ID of the order to cancel
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun cancelOrder(orderId: String): Flow<Result<Boolean>> = flow {
        try {
            orderRepository.cancelOrder(orderId).collect { success ->
                emit(Result.Success(success))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to cancel order"))
        }
    }
}
