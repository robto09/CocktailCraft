package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
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
 * [observeCartItems] re-emits after every write.
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
            val id = cartItem.cocktail.id
            db.transaction {
                // An item already in the cart keeps its stored cocktail and
                // position; only the quantity grows.
                if (db.cartItemQueries.exists(id).executeAsOne() > 0L) {
                    db.cartItemQueries.addQuantity(delta = cartItem.quantity.toLong(), cocktailId = id)
                } else {
                    db.cartItemQueries.insert(
                        cocktailId = id,
                        payload = json.encodeToString(Cocktail.serializer(), cartItem.cocktail),
                        quantity = cartItem.quantity.toLong()
                    )
                }
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

    override suspend fun getCartTotal(): Result<Double> = withContext(ioDispatcher) {
        runCatchingResult("Failed to get cart total") {
            Result.Success(localDatabase.get().readItems().sumOf { it.cocktail.price * it.quantity })
        }
    }
}
