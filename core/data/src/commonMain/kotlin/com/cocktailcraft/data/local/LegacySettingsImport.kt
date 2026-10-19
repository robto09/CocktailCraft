package com.cocktailcraft.data.local

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.repository.insertOrderWithItems
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.Review
import com.russhwolf.settings.Settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

/**
 * One-time import of the user data older app versions kept in [Settings]: the
 * cart, orders and reviews as JSON lists, and favorites as a comma-separated
 * id list. [LocalDatabase] runs it on the first database access after an
 * upgrade; once every key is gone it is a no-op.
 *
 * Each store is imported in its own transaction, and its key is removed only
 * after that transaction commits:
 * - an import interrupted part-way rolls back and is retried next launch;
 * - one that dies between the commit and the key removal re-runs with
 *   INSERT OR IGNORE, which never duplicates or overwrites rows (anything
 *   already in the database is newer than the legacy blob and wins);
 * - a store whose import throws keeps its key without blocking the others.
 *
 * Rows are imported without an owner ([OwnerIds.UNASSIGNED]); [LocalDatabase]
 * assigns them to whoever is signed in right after (see
 * UserDataOwnership.kt).
 *
 * Unreadable data is tolerated rather than retried forever: entries that still
 * decode are imported and the rest dropped, and a blob that isn't a JSON list
 * at all imports nothing. Older versions already treated such data as empty.
 */
internal class LegacySettingsImport(
    private val settings: Settings,
    private val json: Json
) {
    private val codec = CocktailCodec(json)

    companion object {
        // Settings keys of the legacy stores. internal (not private) so tests
        // can seed fixtures under the real keys.
        internal const val CART_KEY = "cocktail_cart_items"
        internal const val ORDERS_KEY = "orders_data"
        internal const val REVIEWS_KEY = "user_reviews"
        internal const val FAVORITES_KEY = "favorite_cocktails"
    }

    fun run(db: CocktailCraftDatabase) {
        importKey(CART_KEY, "cart items") { importCart(db, it) }
        importKey(ORDERS_KEY, "orders") { importOrders(db, it) }
        importKey(REVIEWS_KEY, "reviews") { importReviews(db, it) }
        importKey(FAVORITES_KEY, "favorites") { importFavorites(db, it) }
    }

    /** Imports the value under [key], if any, and removes the key once [import] succeeds. */
    private fun importKey(key: String, label: String, import: (String) -> Int) {
        val raw = settings.getStringOrNull(key) ?: return
        try {
            val count = import(raw)
            settings.remove(key)
            Logger.i { "Imported $count $label from Settings" }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to import $label from Settings, will retry next launch: ${e.message}" }
        }
    }

    /**
     * Each line's cocktail goes into cached_cocktail (unless a record as good
     * is stored there) and the line refers to it, keeping the price it held.
     */
    private fun importCart(db: CocktailCraftDatabase, raw: String): Int {
        val items = decodeList(raw, LegacyCartItem.serializer(), "cart")
        db.transaction {
            // Legacy lists are in display order; positions follow insertion.
            items.forEach { item ->
                val cocktail = item.cocktail.toDomain()
                db.storeLegacyCocktail(cocktail)
                db.cartItemQueries.insertIfAbsent(
                    ownerId = OwnerIds.UNASSIGNED,
                    cocktailId = cocktail.id,
                    quantity = item.quantity.toLong(),
                    priceCents = cocktail.price.cents
                )
            }
        }
        return items.size
    }

    /**
     * Stores [cocktail] unless the database holds it already, upgrading a
     * stored partial record to a full one. Written as never accessed and
     * cached at 0 (stale, so the next view refreshes it); the cart line
     * referring to it pins it.
     */
    private fun CocktailCraftDatabase.storeLegacyCocktail(cocktail: Cocktail) {
        val payload = codec.encode(cocktail)
        val category = cocktail.category?.takeIf { it.isNotBlank() }
        val fullDetails = if (cocktail.hasFullDetails) 1L else 0L
        cachedCocktailQueries.insertIfAbsent(cocktail.id, category, payload, fullDetails, cached_at = 0L)
        if (cocktail.hasFullDetails) cachedCocktailQueries.upgradeToFull(payload = payload, id = cocktail.id)
        cachedCocktailQueries.touchIfAbsent(id = cocktail.id, lastAccessed = 0L)
    }

    private fun importOrders(db: CocktailCraftDatabase, raw: String): Int {
        val orders = decodeList(raw, LegacyOrder.serializer(), "orders").map { it.toDomain() }
        db.transaction {
            orders.forEach { order ->
                // The INSERT OR IGNORE equivalent for an order and its items:
                // an order already in the database (or a duplicate id in the
                // legacy list) keeps exactly the items it has.
                if (db.customerOrderQueries.exists(order.id).executeAsOne() == 0L) {
                    db.insertOrderWithItems(order, ownerId = OwnerIds.UNASSIGNED)
                }
            }
        }
        return orders.size
    }

    private fun importReviews(db: CocktailCraftDatabase, raw: String): Int {
        val reviews = decodeList(raw, Review.serializer(), "reviews")
        db.transaction {
            reviews.forEach { review ->
                db.cocktailReviewQueries.insertIfAbsent(
                    ownerId = OwnerIds.UNASSIGNED,
                    id = review.id,
                    cocktailId = review.cocktailId,
                    userName = review.userName,
                    rating = review.rating.toDouble(),
                    comment = review.comment,
                    date = review.date
                )
            }
        }
        return reviews.size
    }

    /**
     * Favorites were stored as ids only; the full cocktails were looked up in
     * the offline cache or the API on every read. They still are: a favorite
     * refers to its cached_cocktail row, which CocktailFavoritesRepositoryImpl
     * fills in when it is missing.
     */
    private fun importFavorites(db: CocktailCraftDatabase, raw: String): Int {
        val ids = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        db.transaction {
            ids.forEach { id ->
                db.favoriteCocktailQueries.insertIfAbsent(ownerId = OwnerIds.UNASSIGNED, cocktailId = id)
            }
        }
        return ids.size
    }

    /**
     * Decodes a JSON list entry by entry, so one unreadable entry costs only
     * itself rather than the whole list.
     */
    private fun <T> decodeList(raw: String, serializer: KSerializer<T>, label: String): List<T> {
        val array = try {
            json.parseToJsonElement(raw) as? JsonArray
        } catch (e: Exception) {
            null
        }
        if (array == null) {
            Logger.w { "Discarding unreadable legacy $label data" }
            return emptyList()
        }
        return array.mapNotNull { element ->
            try {
                json.decodeFromJsonElement(serializer, element)
            } catch (e: Exception) {
                Logger.w(e) { "Discarding an unreadable legacy $label entry" }
                null
            }
        }
    }
}
