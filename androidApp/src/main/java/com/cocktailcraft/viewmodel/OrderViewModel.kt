package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderViewModel(
    private val orderRepository: OrderRepository? = null,
    private val placeOrderUseCase: PlaceOrderUseCase? = null
) : ViewModel(), KoinComponent {
    
    // Use injected repositories if not provided in constructor (for production)
    private val injectedOrderRepository: OrderRepository by inject()
    private val injectedPlaceOrderUseCase: PlaceOrderUseCase by inject()
    
    // Use the provided repositories or the injected ones
    private val repository: OrderRepository
        get() = orderRepository ?: injectedOrderRepository
    
    private val useCase: PlaceOrderUseCase
        get() = placeOrderUseCase ?: injectedPlaceOrderUseCase
    
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadOrders()
    }
    
    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                repository.getOrderHistory().collect { orderList ->
                    _orders.value = orderList
                }
            } catch (e: Exception) {
                _error.value = "Failed to load orders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addOrder(order: Order) {
        viewModelScope.launch {
            try {
                repository.addOrder(order)
                loadOrders() // Refresh the orders list
            } catch (e: Exception) {
                _error.value = "Failed to add order: ${e.message}"
            }
        }
    }
    
    fun placeOrderDirectly(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Create order object
                val orderId = "ORD-${System.currentTimeMillis()}"
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                // Map cart items to order items
                val orderItems = cartItems.map { cartItem ->
                    OrderItem(
                        name = cartItem.cocktail.name,
                        quantity = cartItem.quantity,
                        price = cartItem.cocktail.price
                    )
                }
                
                val order = Order(
                    id = orderId,
                    date = currentDate,
                    items = orderItems,
                    total = totalPrice,
                    status = "Processing"
                )
                
                repository.placeOrder(order).collect { success ->
                    if (success) {
                        loadOrders() // Refresh orders list on success
                    } else {
                        _error.value = "Failed to place order. Please try again."
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            useCase(cartItems, totalPrice).collect { result ->
                when (result) {
                    is Result.Success -> {
                        loadOrders() // Refresh the orders list
                    }
                    is Result.Error -> {
                        _error.value = "Failed to place order: ${result.message}"
                    }
                    is Result.Loading -> {
                        // Already handled by setting isLoading to true
                    }
                }
                _isLoading.value = false
            }
        }
    }
    
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, status)
                loadOrders() // Refresh the orders list
            } catch (e: Exception) {
                _error.value = "Failed to update order status: ${e.message}"
            }
        }
    }
    
    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            try {
                repository.cancelOrder(orderId).collect { success ->
                    if (success) {
                        loadOrders() // Refresh orders list on success
                    } else {
                        _error.value = "Failed to cancel order. It may be too late to cancel."
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error cancelling order: ${e.message}"
            }
        }
    }
    
    fun getOrderById(orderId: String): Flow<Order?> {
        return flow {
            _isLoading.value = true
            try {
                orders.value.find { it.id == orderId }?.let { 
                    emit(it) 
                } ?: run {
                    // Try to load from repository if not found in current list
                    repository.getOrderById(orderId).collect { order ->
                        emit(order)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load order details: ${e.message}"
                emit(null)
            } finally {
                _isLoading.value = false
            }
        }
    }
} 