package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CartViewModel(
    private val cartRepository: CartRepository? = null
) : BaseViewModel() {
    
    // Use injected repository if not provided in constructor (for production)
    private val injectedCartRepository: CartRepository by inject()
    
    // Use the provided repository or the injected one
    private val repository: CartRepository
        get() = cartRepository ?: injectedCartRepository
    
    private val _cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())
    val cartItems: StateFlow<List<CocktailCartItem>> = _cartItems.asStateFlow()
    
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()
    
    // Keep _errorString for backward compatibility
    private val _errorString = MutableStateFlow<String?>(null)
    val errorString: StateFlow<String?> = _errorString.asStateFlow()
    
    init {
        loadCartItems()
    }
    
    fun loadCartItems() {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                repository.getCartItems().collect { items ->
                    _cartItems.value = items
                }
                
                repository.getCartTotal().collect { total ->
                    _totalPrice.value = total
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to load cart: ${e.message}"
                handleException(e, "Failed to load cart")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun addToCart(cocktail: Cocktail, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                val cartItem = CocktailCartItem(cocktail, quantity)
                repository.addToCart(cartItem)
                loadCartItems()
            } catch (e: Exception) {
                _errorString.value = "Failed to add to cart: ${e.message}"
                handleException(e, "Failed to add to cart")
            }
        }
    }
    
    fun removeFromCart(cocktailId: String) {
        viewModelScope.launch {
            try {
                repository.removeFromCart(cocktailId)
                loadCartItems()
            } catch (e: Exception) {
                _errorString.value = "Failed to remove from cart: ${e.message}"
                handleException(e, "Failed to remove from cart")
            }
        }
    }
    
    fun updateQuantity(cocktailId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                repository.updateQuantity(cocktailId, quantity)
                loadCartItems()
            } catch (e: Exception) {
                _errorString.value = "Failed to update quantity: ${e.message}"
                handleException(e, "Failed to update quantity")
            }
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            try {
                repository.clearCart()
                loadCartItems()
            } catch (e: Exception) {
                _errorString.value = "Failed to clear cart: ${e.message}"
                handleException(e, "Failed to clear cart")
            }
        }
    }
} 