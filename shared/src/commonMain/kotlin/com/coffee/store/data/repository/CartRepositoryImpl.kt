package com.coffee.store.data.repository

import com.coffee.store.domain.model.CocktailCartItem
import com.coffee.store.domain.repository.CartRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CartRepositoryImpl(
    private val settings: Settings,
    private val json: Json
) : CartRepository {

    override suspend fun getCartItems(): Flow<List<CocktailCartItem>> = flow {
        val cartJson = settings.getStringOrNull(CART_ITEMS_KEY) ?: "[]"
        try {
            val cartItems = json.decodeFromString<List<CocktailCartItem>>(cartJson)
            emit(cartItems)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addToCart(cartItem: CocktailCartItem) {
        val currentItems = getCurrentCartItems().toMutableList()
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
        val currentItems = getCurrentCartItems().toMutableList()
        currentItems.removeAll { it.cocktail.id == cocktailId }
        saveCartItems(currentItems)
    }

    override suspend fun updateQuantity(cocktailId: String, quantity: Int) {
        val currentItems = getCurrentCartItems().toMutableList()
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
    }

    override suspend fun getCartTotal(): Flow<Double> = flow {
        val cartItems = getCurrentCartItems()
        val total = cartItems.sumOf { it.cocktail.price * it.quantity }
        emit(total)
    }

    private fun getCurrentCartItems(): List<CocktailCartItem> {
        val cartJson = settings.getStringOrNull(CART_ITEMS_KEY) ?: "[]"
        return try {
            json.decodeFromString(cartJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCartItems(items: List<CocktailCartItem>) {
        val cartJson = json.encodeToString(items)
        settings.putString(CART_ITEMS_KEY, cartJson)
    }

    companion object {
        private const val CART_ITEMS_KEY = "cocktail_cart_items"
    }
} 
