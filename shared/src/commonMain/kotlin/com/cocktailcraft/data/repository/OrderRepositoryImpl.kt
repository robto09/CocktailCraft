package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.util.Result
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cocktailcraft.util.UUID
import com.cocktailcraft.util.runCatchingResult

internal class OrderRepositoryImpl(
    private val settings: Settings,
    private val json: Json,
    private val appConfig: AppConfig,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OrderRepository {

    // In-memory cache of orders
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    
    private val loadMutex = Mutex()
    private var loaded = false

    // Persisted state loads lazily on first use — a Koin `single` must not
    // do disk I/O on whichever thread first resolves it.
    private suspend fun ensureLoaded() {
        if (loaded) return
        loadMutex.withLock {
            if (!loaded) {
                withContext(ioDispatcher) { loadOrdersFromStorage() }
                loaded = true
            }
        }
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

    override fun observeOrders(): Flow<List<Order>> =
        _orders.asStateFlow().onStart { ensureLoaded() }

    override suspend fun getOrders(): Result<List<Order>> {
        ensureLoaded()
        return Result.Success(_orders.value)
    }

    override suspend fun placeOrder(order: Order): Result<Unit> = withContext(ioDispatcher) {
        ensureLoaded()
        runCatchingResult("Failed to place order") {
            val orders = _orders.value.toMutableList()
            val orderWithId = if (order.id.isBlank()) {
                order.copy(id = UUID.randomUUID())
            } else {
                order
            }
            orders.add(orderWithId)
            _orders.value = orders
            saveOrdersToStorage()
            Result.Success(Unit)
        }
    }

    override suspend fun getOrderById(id: String): Result<Order?> {
        ensureLoaded()
        return runCatchingResult("Failed to get order by ID") {
            Result.Success(_orders.value.find { it.id == id })
        }
    }

    override suspend fun updateOrderStatus(id: String, status: String): Result<Unit> = withContext(ioDispatcher) {
        ensureLoaded()
        runCatchingResult("Failed to update order status") {
            val updatedOrders = _orders.value.map { order ->
                if (order.id == id) order.copy(status = status) else order
            }
            _orders.value = updatedOrders
            saveOrdersToStorage()
            Result.Success(Unit)
        }
    }

    override suspend fun deleteOrder(id: String): Result<Unit> = withContext(ioDispatcher) {
        ensureLoaded()
        runCatchingResult("Failed to delete order") {
            val updatedOrders = _orders.value.filter { it.id != id }
            _orders.value = updatedOrders
            saveOrdersToStorage()
            Result.Success(Unit)
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> = withContext(ioDispatcher) {
        ensureLoaded()
        runCatchingResult("Failed to cancel order") {
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
        }
    }
}