package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Use case for managing cart operations.
 * This use case handles the business logic of cart management,
 * including error handling and result transformation.
 */
class ManageCartUseCase(
    private val cartRepository: CartRepository
) {
    /**
     * Get all cart items.
     * @return Flow of Result containing either a list of cart items or an error
     */
    suspend fun getCartItems(): Flow<Result<List<CocktailCartItem>>> {
        return cartRepository.getCartItems()
            .map { items -> Result.Success(items) as Result<List<CocktailCartItem>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Failed to get cart items"))
            }
    }
    
    /**
     * Get the total price of all items in the cart.
     * @return Flow of Result containing either the total price or an error
     */
    suspend fun getCartTotal(): Flow<Result<Double>> {
        return cartRepository.getCartTotal()
            .map { total -> Result.Success(total) as Result<Double> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Failed to calculate cart total"))
            }
    }
    
    /**
     * Add a cocktail to the cart.
     * @param cocktail The cocktail to add
     * @param quantity The quantity to add
     * @return Flow of Result containing either a success message or an error
     */
    suspend fun addToCart(cocktail: Cocktail, quantity: Int = 1): Flow<Result<String>> = flow {
        try {
            val cartItem = CocktailCartItem(cocktail, quantity)
            cartRepository.addToCart(cartItem)
            emit(Result.Success("Added to cart"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to add to cart"))
        }
    }
    
    /**
     * Remove a cocktail from the cart.
     * @param cocktailId The ID of the cocktail to remove
     * @return Flow of Result containing either a success message or an error
     */
    suspend fun removeFromCart(cocktailId: String): Flow<Result<String>> = flow {
        try {
            cartRepository.removeFromCart(cocktailId)
            emit(Result.Success("Removed from cart"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to remove from cart"))
        }
    }
    
    /**
     * Update the quantity of a cocktail in the cart.
     * @param cocktailId The ID of the cocktail to update
     * @param quantity The new quantity
     * @return Flow of Result containing either a success message or an error
     */
    suspend fun updateQuantity(cocktailId: String, quantity: Int): Flow<Result<String>> = flow {
        try {
            cartRepository.updateQuantity(cocktailId, quantity)
            emit(Result.Success("Updated quantity"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to update quantity"))
        }
    }
    
    /**
     * Clear all items from the cart.
     * @return Flow of Result containing either a success message or an error
     */
    suspend fun clearCart(): Flow<Result<String>> = flow {
        try {
            cartRepository.clearCart()
            emit(Result.Success("Cart cleared"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to clear cart"))
        }
    }
}
