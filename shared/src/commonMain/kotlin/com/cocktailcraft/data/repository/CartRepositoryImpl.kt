package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CartRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CartRepositoryImpl(
    private val settings: Settings,
    private val json: Json
) : CartRepository {

    // Use a StateFlow to ensure proper state updates
    private val _cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())

    init {
        // Load initial cart items
        loadCartItems()
    }

    override suspend fun getCartItems(): Flow<List<CocktailCartItem>> = _cartItems.asStateFlow()

    private fun loadCartItems() {
        val cartJson = settings.getStringOrNull(CART_ITEMS_KEY) ?: "[]"
        try {
            val cartItems = json.decodeFromString<List<CocktailCartItem>>(cartJson)
            _cartItems.value = cartItems
        } catch (e: Exception) {
            _cartItems.value = emptyList()
        }
    }

    override suspend fun addToCart(cartItem: CocktailCartItem) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.cocktail.id == cartItem.cocktail.id }

        if (existingItemIndex != -1) {
            // Update existing item
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + cartItem.quantity
            )
        } else {
            // Add new item
            currentItems.add(cartItem)
        }

        saveCartItems(currentItems)
    }

    override suspend fun removeFromCart(cocktailId: String) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { it.cocktail.id == cocktailId }
        saveCartItems(currentItems)
    }

    override suspend fun updateQuantity(cocktailId: String, quantity: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.cocktail.id == cocktailId }

        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            if (quantity <= 0) {
                currentItems.removeAt(itemIndex)
            } else {
                currentItems[itemIndex] = item.copy(quantity = quantity)
            }
            saveCartItems(currentItems)
        }
    }

    override suspend fun clearCart() {
        settings.remove(CART_ITEMS_KEY)
        _cartItems.value = emptyList()
    }

    override suspend fun getCartTotal(): Flow<Double> = flow {
        val cartItems = _cartItems.value
        val total = cartItems.sumOf { it.cocktail.price * it.quantity }
        emit(total)
    }

    private fun saveCartItems(items: List<CocktailCartItem>) {
        val cartJson = json.encodeToString(items)
        settings.putString(CART_ITEMS_KEY, cartJson)
        // Update the StateFlow to trigger UI updates
        _cartItems.value = items
    }

    companion object {
        private const val CART_ITEMS_KEY = "cocktail_cart_items"
    }
}
