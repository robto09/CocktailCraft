package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import kotlinx.coroutines.flow.Flow

internal class ManageCartUseCase(
    private val cartRepository: CartRepository,
    private val detailRepository: CocktailDetailRepository
) {
    /** Hot stream of cart contents, seeded from persistence. */
    fun observeCartItems(): Flow<List<CocktailCartItem>> = cartRepository.observeCartItems()

    suspend fun getCartItems(): Result<List<CocktailCartItem>> {
        return cartRepository.getCartItems()
    }

    suspend fun addToCart(cocktail: Cocktail, quantity: Int = 1): Result<Unit> {
        val cartItem = CocktailCartItem(hydrated(cocktail), quantity)
        return cartRepository.addToCart(cartItem)
    }

    suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> {
        val item =
            if (cartItem.cocktail.hasFullDetails) cartItem
            else cartItem.copy(cocktail = hydrated(cartItem.cocktail))
        return cartRepository.addToCart(item)
    }

    /**
     * List endpoints fabricate placeholder ingredients/instructions; fetch
     * full details before persisting so stored items are complete. Falls
     * back to the partial object when offline or on lookup failure.
     */
    private suspend fun hydrated(cocktail: Cocktail): Cocktail {
        if (cocktail.hasFullDetails) return cocktail
        return try {
            detailRepository.getCocktailById(cocktail.id).getOrNull() ?: cocktail
        } catch (e: Exception) {
            cocktail
        }
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

