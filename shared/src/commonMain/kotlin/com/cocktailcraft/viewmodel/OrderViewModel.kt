package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.repository.OrderRepository
import com.cocktailcraft.domain.usecase.PlaceOrderUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.getCurrentTimeMillis
import com.cocktailcraft.util.formatDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OrderViewModel(
    private val orderRepository: OrderRepository? = null,
    private val placeOrderUseCase: PlaceOrderUseCase? = null
) : BaseViewModel() {
    
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
    
    // Keep _errorString for backward compatibility
    private val _errorString = MutableStateFlow<String?>(null)
    val errorString: StateFlow<String?> = _errorString.asStateFlow()
    
    init {
        loadOrders()
    }
    
    fun loadOrders() {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                repository.getOrderHistory().collect { orderList ->
                    _orders.value = orderList
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to load orders: ${e.message}"
                handleException(e, "Failed to load orders")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun addOrder(order: Order) {
        viewModelScope.launch {
            try {
                repository.addOrder(order)
                loadOrders() // Refresh the orders list
            } catch (e: Exception) {
                _errorString.value = "Failed to add order: ${e.message}"
                handleException(e, "Failed to add order")
            }
        }
    }
    
    fun placeOrderDirectly(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                // Create order object
                val orderId = "ORD-${getCurrentTimeMillis()}"
                val currentDate = formatDate(getCurrentTimeMillis(), "yyyy-MM-dd")
                
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
                        _errorString.value = "Failed to place order. Please try again."
                        setError("Order Failed", "Failed to place order. Please try again.")
                    }
                }
            } catch (e: Exception) {
                _errorString.value = "Error: ${e.message}"
                handleException(e, "Failed to place order")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun placeOrder(cartItems: List<CocktailCartItem>, totalPrice: Double) {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            useCase(cartItems, totalPrice).collect { result ->
                when (result) {
                    is Result.Success -> {
                        loadOrders() // Refresh the orders list
                    }
                    is Result.Error -> {
                        _errorString.value = "Failed to place order: ${result.message}"
                        setError("Order Failed", result.message)
                    }
                    is Result.Loading -> {
                        // Already handled by setting isLoading to true
                    }
                }
                setLoading(false)
            }
        }
    }
    
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, status)
                loadOrders() // Refresh the orders list
            } catch (e: Exception) {
                _errorString.value = "Failed to update order status: ${e.message}"
                handleException(e, "Failed to update order status")
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
                        _errorString.value = "Failed to cancel order. It may be too late to cancel."
                        setError("Cancel Failed", "Failed to cancel order. It may be too late to cancel.")
                    }
                }
            } catch (e: Exception) {
                _errorString.value = "Error cancelling order: ${e.message}"
                handleException(e, "Error cancelling order")
            }
        }
    }
    
    fun getOrderById(orderId: String): Flow<Order?> {
        return flow {
            setLoading(true)
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
                _errorString.value = "Failed to load order details: ${e.message}"
                handleException(e, "Failed to load order details")
                emit(null)
            } finally {
                setLoading(false)
            }
        }
    }
} 