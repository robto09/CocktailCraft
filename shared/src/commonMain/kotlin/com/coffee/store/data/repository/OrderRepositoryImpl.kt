package com.coffee.store.data.repository

import com.coffee.store.domain.config.AppConfig
import com.coffee.store.domain.model.Order
import com.coffee.store.domain.repository.OrderRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class OrderRepositoryImpl(
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

    override suspend fun getOrders(): Flow<List<Order>> = _orders.asStateFlow()

    override suspend fun addOrder(order: Order) {
        val updatedOrders = _orders.value.toMutableList().apply {
            add(order)
        }
        _orders.value = updatedOrders
        saveOrdersToStorage()
    }

    override suspend fun getOrderById(id: String): Flow<Order?> = flow {
        emit(_orders.value.find { it.id == id })
    }

    override suspend fun updateOrderStatus(id: String, status: String) {
        val updatedOrders = _orders.value.map { order ->
            if (order.id == id) order.copy(status = status) else order
        }
        _orders.value = updatedOrders
        saveOrdersToStorage()
    }

    override suspend fun deleteOrder(id: String) {
        val updatedOrders = _orders.value.filter { it.id != id }
        _orders.value = updatedOrders
        saveOrdersToStorage()
    }

    override suspend fun placeOrder(order: Order): Flow<Boolean> = flow {
        try {
            val orders = _orders.value.toMutableList()
            // Generate a new ID if not provided
            val orderWithId = if (order.id.isBlank()) {
                order.copy(id = UUID.randomUUID().toString())
            } else {
                order
            }
            orders.add(orderWithId)
            _orders.value = orders
            saveOrdersToStorage()
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun getOrderHistory(): Flow<List<Order>> = flow {
        emit(_orders.value)
    }

    override suspend fun cancelOrder(orderId: String): Flow<Boolean> = flow {
        try {
            val orders = _orders.value.toMutableList()
            val orderIndex = orders.indexOfFirst { it.id == orderId }
            
            if (orderIndex != -1) {
                val order = orders[orderIndex]
                // Only allow cancellation if the order is still pending or processing
                if (order.status == "Processing" || order.status == "Pending") {
                    orders[orderIndex] = order.copy(status = "Cancelled")
                    _orders.value = orders
                    saveOrdersToStorage()
                    emit(true)
                } else {
                    emit(false)
                }
            } else {
                emit(false)
            }
        } catch (e: Exception) {
            emit(false)
        }
    }
} 