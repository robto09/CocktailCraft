package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.local.CocktailCodec
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.repository.CartRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import com.cocktailcraft.data.cache.db.cartItem.SelectAll as CartItemRow

/**
 * The cart in the cart_item table (see CartItem.sq), one row per cocktail.
 * [observeCartItems] re-emits after every write, including the clear that
 * [com.cocktailcraft.domain.repository.OrderRepository.placeOrderAndClearCart]
 * does in the same transaction as the order insert.
 *
 * A line refers to the cocktail's cached_cocktail row ([CocktailCache]),
 * which it pins, and keeps only the unit price it was added at.
 */
internal class CartRepositoryImpl(
    private val localDatabase: LocalDatabase,
    private val cocktailCache: CocktailCache,
    json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CartRepository {

    private val codec = CocktailCodec(json)

    private fun toCartItem(row: CartItemRow): CocktailCartItem? {
        val cocktail = codec.decodeOrNull(row.payload, row.full_details != 0L) ?: return null
        return CocktailCartItem(
            cocktail = row.price_cents?.let { cocktail.copy(price = Money(it)) } ?: cocktail,
            quantity = row.quantity.toInt()
        )
    }

    private fun CocktailCraftDatabase.readItems(): List<CocktailCartItem> =
        cartItemQueries.selectAll().executeAsList().mapNotNull(::toCartItem)

    override fun observeCartItems(): Flow<List<CocktailCartItem>> =
        localDatabase.observe(ioDispatcher, { cartItemQueries.selectAll() }) { rows ->
            rows.mapNotNull(::toCartItem)
        }

    override suspend fun getCartItems(): Result<List<CocktailCartItem>> = withContext(ioDispatcher) {
        runCatchingResult("Failed to load cart") {
            Result.Success(localDatabase.get().readItems())
        }
    }

    override suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> = addAll(listOf(cartItem))

    // One transaction: a failure part-way adds none of them (reorder)
    override suspend fun addAllToCart(cartItems: List<CocktailCartItem>): Result<Unit> = addAll(cartItems)

    private suspend fun addAll(cartItems: List<CocktailCartItem>): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to add to cart") {
            localDatabase.get()
            cocktailCache.storeAndReference(cartItems.map { it.cocktail }) {
                cartItems.forEach { add(it) }
            }
            Result.Success(Unit)
        }
    }

    override suspend fun removeFromCart(cocktailId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to remove from cart") {
            localDatabase.get().cartItemQueries.deleteById(cocktailId)
            Result.Success(Unit)
        }
    }

    override suspend fun updateQuantity(cocktailId: String, quantity: Int): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to update quantity") {
            val queries = localDatabase.get().cartItemQueries
            if (quantity <= 0) {
                queries.deleteById(cocktailId)
            } else {
                queries.setQuantity(quantity = quantity.toLong(), cocktailId = cocktailId)
            }
            Result.Success(Unit)
        }
    }

    override suspend fun clearCart(): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to clear cart") {
            localDatabase.get().cartItemQueries.deleteAll()
            Result.Success(Unit)
        }
    }
}

/**
 * Adds [cartItem]; call inside a transaction. An item already in the cart
 * keeps its price and position; only the quantity grows.
 */
private fun CocktailCraftDatabase.add(cartItem: CocktailCartItem) {
    val id = cartItem.cocktail.id
    if (cartItemQueries.exists(id).executeAsOne() > 0L) {
        cartItemQueries.addQuantity(delta = cartItem.quantity.toLong(), cocktailId = id)
    } else {
        cartItemQueries.insert(
            cocktailId = id,
            quantity = cartItem.quantity.toLong(),
            priceCents = cartItem.cocktail.price.cents
        )
    }
}
