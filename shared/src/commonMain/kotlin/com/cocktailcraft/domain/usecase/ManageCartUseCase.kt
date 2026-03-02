package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault

class ManageCartUseCase(
    private val cartRepository: CartRepository
) {
    suspend fun getCartItems(): Result<List<CocktailCartItem>> {
        return cartRepository.getCartItems()
    }

    suspend fun addToCart(cocktail: Cocktail, quantity: Int = 1): Result<Unit> {
        val cartItem = CocktailCartItem(cocktail, quantity)
        return cartRepository.addToCart(cartItem)
    }

    suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> {
        return cartRepository.addToCart(cartItem)
    }

    suspend fun removeFromCart(cocktailId: String): Result<Unit> {
        return cartRepository.removeFromCart(cocktailId)
    }

    suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit> {
        return cartRepository.updateQuantity(cocktailId, quantity)
    }

    suspend fun clearCart(): Result<Unit> {
        return cartRepository.clearCart()
    }

    suspend fun getCartTotal(): Result<Double> {
        return cartRepository.getCartTotal()
    }
}

