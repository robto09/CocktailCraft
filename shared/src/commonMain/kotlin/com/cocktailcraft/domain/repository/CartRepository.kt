package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun observeCartItems(): Flow<List<CocktailCartItem>>
    suspend fun getCartItems(): Result<List<CocktailCartItem>>
    suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit>
    suspend fun removeFromCart(cocktailId: String): Result<Unit>
    suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit>
    suspend fun clearCart(): Result<Unit>
    suspend fun getCartTotal(): Result<Double>
}