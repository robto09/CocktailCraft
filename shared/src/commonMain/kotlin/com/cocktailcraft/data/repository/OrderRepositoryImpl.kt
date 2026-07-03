package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cocktailcraft.util.UUID

internal class OrderRepositoryImpl(
    private val settings: Settings,
    private val json: Json,
    private val appConfig: AppConfig
) : OrderRepository {

    // In-memory cache of orders
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    
    init {
        // Load orders from persistent storage when repository is created
        loadOrdersFromStorage()
    }
    
    private fun loadOrdersFromStorage() {
        val ordersJson = settings.getStringOrNull(appConfig.ordersStorageKey) ?: "[]"
        try {
            _orders.value = json.decodeFromString(ordersJson)
        } catch (e: Exception) {
            // If there's an error parsing, start with empty list
            _orders.value = emptyList()
        }
    }
    
    private fun saveOrdersToStorage() {
        val ordersJson = json.encodeToString(_orders.value)
        settings.putString(appConfig.ordersStorageKey, ordersJson)
    }

    override fun observeOrders(): Flow<List<Order>> = _orders.asStateFlow()

    override suspend fun getOrders(): Result<List<Order>> = Result.Success(_orders.value)

    override suspend fun addOrder(order: Order): Result<Unit> {
        return try {
            val updatedOrders = _orders.value.toMutableList().apply {
                add(order)
            }
            _orders.value = updatedOrders
            saveOrdersToStorage()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add order")
        }
    }

    override suspend fun getOrderById(id: String): Result<Order?> {
        return try {
            Result.Success(_orders.value.find { it.id == id })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get order by ID")
        }
    }

    override suspend fun updateOrderStatus(id: String, status: String): Result<Unit> {
        return try {
            val updatedOrders = _orders.value.map { order ->
                if (order.id == id) order.copy(status = status) else order
            }
            _orders.value = updatedOrders
            saveOrdersToStorage()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update order status")
        }
    }

    override suspend fun deleteOrder(id: String): Result<Unit> {
        return try {
            val updatedOrders = _orders.value.filter { it.id != id }
            _orders.value = updatedOrders
            saveOrdersToStorage()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete order")
        }
    }

    override suspend fun placeOrder(order: Order): Result<Boolean> {
        return try {
            val orders = _orders.value.toMutableList()
            val orderWithId = if (order.id.isBlank()) {
                order.copy(id = UUID.randomUUID())
            } else {
                order
            }
            orders.add(orderWithId)
            _orders.value = orders
            saveOrdersToStorage()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to place order")
        }
    }

    override suspend fun getOrderHistory(): Result<List<Order>> {
        return Result.Success(_orders.value)
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        return try {
            val orders = _orders.value.toMutableList()
            val orderIndex = orders.indexOfFirst { it.id == orderId }

            if (orderIndex != -1) {
                val order = orders[orderIndex]
                if (order.status == "Processing" || order.status == "Pending") {
                    orders[orderIndex] = order.copy(status = "Cancelled")
                    _orders.value = orders
                    saveOrdersToStorage()
                    Result.Success(true)
                } else {
                    Result.Success(false)
                }
            } else {
                Result.Success(false)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to cancel order")
        }
    }
}