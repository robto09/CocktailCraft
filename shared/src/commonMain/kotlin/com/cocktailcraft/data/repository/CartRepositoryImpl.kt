package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.util.Result
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class CartRepositoryImpl(
    private val settings: Settings,
    private val json: Json
) : CartRepository {

    // Use a StateFlow to ensure proper state updates
    private val _cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())

    init {
        // Load initial cart items
        loadCartItems()
    }

    override fun observeCartItems(): Flow<List<CocktailCartItem>> = _cartItems.asStateFlow()

    override suspend fun getCartItems(): Result<List<CocktailCartItem>> = Result.Success(_cartItems.value)

    private fun loadCartItems() {
        val cartJson = settings.getStringOrNull(CART_ITEMS_KEY) ?: "[]"
        try {
            val cartItems = json.decodeFromString<List<CocktailCartItem>>(cartJson)
            _cartItems.value = cartItems
        } catch (e: Exception) {
            _cartItems.value = emptyList()
        }
    }

    override suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> {
        return try {
            val currentItems = _cartItems.value.toMutableList()
            val existingItemIndex = currentItems.indexOfFirst { it.cocktail.id == cartItem.cocktail.id }

            if (existingItemIndex != -1) {
                val existingItem = currentItems[existingItemIndex]
                currentItems[existingItemIndex] = existingItem.copy(
                    quantity = existingItem.quantity + cartItem.quantity
                )
            } else {
                currentItems.add(cartItem)
            }

            saveCartItems(currentItems)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add to cart")
        }
    }

    override suspend fun removeFromCart(cocktailId: String): Result<Unit> {
        return try {
            val currentItems = _cartItems.value.toMutableList()
            currentItems.removeAll { it.cocktail.id == cocktailId }
            saveCartItems(currentItems)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to remove from cart")
        }
    }

    override suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit> {
        return try {
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
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update quantity")
        }
    }

    override suspend fun clearCart(): Result<Unit> {
        return try {
            settings.remove(CART_ITEMS_KEY)
            _cartItems.value = emptyList()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to clear cart")
        }
    }

    override suspend fun getCartTotal(): Result<Double> {
        return try {
            val cartItems = _cartItems.value
            Result.Success(cartItems.sumOf { it.cocktail.price * it.quantity })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cart total")
        }
    }

    private fun saveCartItems(items: List<CocktailCartItem>) {
        val cartJson = json.encodeToString(items)
        settings.putString(CART_ITEMS_KEY, cartJson)
        _cartItems.value = items
    }

    companion object {
        private const val CART_ITEMS_KEY = "cocktail_cart_items"
    }
}
