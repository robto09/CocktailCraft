package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingNonCancellation
import kotlinx.coroutines.flow.Flow
import kotlin.native.HiddenFromObjC

@HiddenFromObjC // Public for :presentation; not Swift API (AR-12)
class ManageCartUseCase(
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
     * Reorder: puts [order]'s items back in the cart with their quantities
     * (added to any already there), all or nothing. Each cocktail is looked
     * up like any add (stored copy first, else fetched), so the cart holds
     * its current details and price; one that can't be looked up (offline,
     * not stored) goes in as the order recorded it, with its name and the
     * price paid. Returns how many units were added.
     *
     * Fails with [ErrorCode.INVALID_DATA], leaving the cart as it was, for an
     * order that can't be reordered ([Order.canReorder]: items placed before
     * they recorded their cocktail id).
     */
    suspend fun reorder(order: Order): Result<Int> {
        if (!order.canReorder) {
            return Result.Error("Order ${order.id} has items without a cocktail id", ErrorCode.INVALID_DATA)
        }
        val cartItems = order.items.mapNotNull { item ->
            item.cocktailId?.let { id ->
                val ordered = Cocktail(id = id, name = item.name, price = item.price, hasFullDetails = false)
                CocktailCartItem(hydrated(ordered), item.quantity)
            }
        }
        return cartRepository.addAllToCart(cartItems).map { cartItems.sumOf { it.quantity } }
    }

    /**
     * List endpoints return partial records; fetch full details before
     * persisting so stored items are complete. Falls back to the partial
     * object when offline or on lookup failure.
     */
    private suspend fun hydrated(cocktail: Cocktail): Cocktail {
        if (cocktail.hasFullDetails) return cocktail
        return runCatchingNonCancellation(onFailure = { cocktail }) {
            detailRepository.getCocktailById(cocktail.id).getOrNull() ?: cocktail
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
}

