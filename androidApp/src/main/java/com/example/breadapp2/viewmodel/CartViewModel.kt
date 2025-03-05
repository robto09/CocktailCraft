package com.example.breadapp2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffee.store.domain.model.CocktailCartItem
import com.coffee.store.domain.model.Cocktail
import com.coffee.store.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CartViewModel : ViewModel(), KoinComponent {
    private val cartRepository: CartRepository by inject()
    
    private val _cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())
    val cartItems: StateFlow<List<CocktailCartItem>> = _cartItems.asStateFlow()
    
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadCartItems()
    }
    
    fun loadCartItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                cartRepository.getCartItems().collect { items ->
                    _cartItems.value = items
                }
                
                cartRepository.getCartTotal().collect { total ->
                    _totalPrice.value = total
                }
            } catch (e: Exception) {
                _error.value = "Failed to load cart: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addToCart(cocktail: Cocktail, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                val cartItem = CocktailCartItem(cocktail, quantity)
                cartRepository.addToCart(cartItem)
                loadCartItems()
            } catch (e: Exception) {
                _error.value = "Failed to add to cart: ${e.message}"
            }
        }
    }
    
    fun removeFromCart(cocktailId: String) {
        viewModelScope.launch {
            try {
                cartRepository.removeFromCart(cocktailId)
                loadCartItems()
            } catch (e: Exception) {
                _error.value = "Failed to remove from cart: ${e.message}"
            }
        }
    }
    
    fun updateQuantity(cocktailId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                cartRepository.updateQuantity(cocktailId, quantity)
                loadCartItems()
            } catch (e: Exception) {
                _error.value = "Failed to update quantity: ${e.message}"
            }
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            try {
                cartRepository.clearCart()
                loadCartItems()
            } catch (e: Exception) {
                _error.value = "Failed to clear cart: ${e.message}"
            }
        }
    }
} 