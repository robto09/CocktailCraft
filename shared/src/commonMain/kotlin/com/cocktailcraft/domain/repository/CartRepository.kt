package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.CocktailCartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    suspend fun getCartItems(): Flow<List<CocktailCartItem>>
    suspend fun addToCart(cartItem: CocktailCartItem)
    suspend fun removeFromCart(cocktailId: String)
    suspend fun updateQuantity(cocktailId: String, quantity: Int)
    suspend fun clearCart()
    suspend fun getCartTotal(): Flow<Double>
} 