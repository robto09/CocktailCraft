package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlin.native.HiddenFromObjC

@HiddenFromObjC // Kotlin-side port, not Swift API: Swift reaches data through the ViewModels
interface CartRepository {
    fun observeCartItems(): Flow<List<CocktailCartItem>>
    suspend fun getCartItems(): Result<List<CocktailCartItem>>
    suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit>

    /**
     * Adds every item as [addToCart] would, all or nothing. Implementations
     * backed by a database do it in one transaction; this default (for
     * in-memory stand-ins) adds them one at a time and stops at the first
     * failure.
     */
    suspend fun addAllToCart(cartItems: List<CocktailCartItem>): Result<Unit> {
        for (item in cartItems) {
            val result = addToCart(item)
            if (result !is Result.Success) return result
        }
        return Result.Success(Unit)
    }

    suspend fun removeFromCart(cocktailId: String): Result<Unit>
    suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit>
    suspend fun clearCart(): Result<Unit>
}