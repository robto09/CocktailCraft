package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.sumOfMoney
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
 */
internal class CartRepositoryImpl(
    private val localDatabase: LocalDatabase,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CartRepository {

    private fun toCartItem(row: CartItemRow): CocktailCartItem? =
        try {
            CocktailCartItem(
                cocktail = json.decodeFromString(Cocktail.serializer(), row.payload),
                quantity = row.quantity.toInt()
            )
        } catch (e: Exception) {
            Logger.w(e) { "Skipping unreadable cart item ${row.cocktail_id}" }
            null
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

    override suspend fun addToCart(cartItem: CocktailCartItem): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to add to cart") {
            val db = localDatabase.get()
            db.transaction { db.add(cartItem, json) }
            Result.Success(Unit)
        }
    }

    // One transaction: a failure part-way adds none of them (reorder)
    override suspend fun addAllToCart(cartItems: List<CocktailCartItem>): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to add to cart") {
            val db = localDatabase.get()
            db.transaction { cartItems.forEach { db.add(it, json) } }
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

    override suspend fun getCartTotal(): Result<Money> = withContext(ioDispatcher) {
        runCatchingResult("Failed to get cart total") {
            Result.Success(localDatabase.get().readItems().sumOfMoney { it.lineTotal })
        }
    }
}

/**
 * Adds [cartItem]; call inside a transaction. An item already in the cart
 * keeps its stored cocktail and position; only the quantity grows.
 */
private fun CocktailCraftDatabase.add(cartItem: CocktailCartItem, json: Json) {
    val id = cartItem.cocktail.id
    if (cartItemQueries.exists(id).executeAsOne() > 0L) {
        cartItemQueries.addQuantity(delta = cartItem.quantity.toLong(), cocktailId = id)
    } else {
        cartItemQueries.insert(
            cocktailId = id,
            payload = json.encodeToString(Cocktail.serializer(), cartItem.cocktail),
            quantity = cartItem.quantity.toLong()
        )
    }
}
