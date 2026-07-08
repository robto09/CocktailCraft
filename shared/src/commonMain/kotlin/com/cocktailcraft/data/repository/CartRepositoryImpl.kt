package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CartRepository
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

internal class CartRepositoryImpl(
    private val settings: Settings,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CartRepository {

    // Use a StateFlow to ensure proper state updates
    private val _cartItems = MutableStateFlow<List<CocktailCartItem>>(emptyList())

    private val loadMutex = Mutex()
    private var loaded = false

    // Persisted state loads lazily on first use — a Koin `single` must not
    // do disk I/O on whichever thread first resolves it.
    private suspend fun ensureLoaded() {
        if (loaded) return
        loadMutex.withLock {
            if (!loaded) {
                withContext(ioDispatcher) { loadCartItems() }
                loaded = true
            }
        }
    }

    override fun observeCartItems(): Flow<List<CocktailCartItem>> =
        _cartItems.asStateFlow().onStart { ensureLoaded() }

    override suspend fun getCartItems(): Result<List<CocktailCartItem>> {
        ensureLoaded()
        return Result.Success(_cartItems.value)
    }

    private fun loadCartItems() {
        val cartJson = settings.getStringOrNull(CART_ITEMS_KEY) ?: "[]"
        try {
            val cartItems = json.decodeFromString<List<CocktailCartItem>>(cartJson)
            _cartItems.value = cartItems
        } catch (e: Exception) {
            _cartItems.value = emptyList()
        }
    }

    override suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> = withContext(ioDispatcher) {
        ensureLoaded()
        try {
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

    override suspend fun removeFromCart(cocktailId: String): Result<Unit> = withContext(ioDispatcher) {
        ensureLoaded()
        try {
            val currentItems = _cartItems.value.toMutableList()
            currentItems.removeAll { it.cocktail.id == cocktailId }
            saveCartItems(currentItems)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to remove from cart")
        }
    }

    override suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit> = withContext(ioDispatcher) {
        ensureLoaded()
        try {
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

    override suspend fun clearCart(): Result<Unit> = withContext(ioDispatcher) {
        ensureLoaded()
        try {
            settings.remove(CART_ITEMS_KEY)
            _cartItems.value = emptyList()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to clear cart")
        }
    }

    override suspend fun getCartTotal(): Result<Double> {
        ensureLoaded()
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
