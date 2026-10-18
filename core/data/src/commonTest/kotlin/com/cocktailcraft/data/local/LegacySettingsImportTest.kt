package com.cocktailcraft.data.local

import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.local.LegacySettingsImport.Companion.CART_KEY
import com.cocktailcraft.data.local.LegacySettingsImport.Companion.FAVORITES_KEY
import com.cocktailcraft.data.local.LegacySettingsImport.Companion.ORDERS_KEY
import com.cocktailcraft.data.local.LegacySettingsImport.Companion.REVIEWS_KEY
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.data.repository.ReviewRepositoryImpl
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The one-time move of the cart, orders, reviews and favorites out of the
 * Settings JSON blobs older versions wrote. Fixtures are encoded exactly as
 * the old repositories encoded them.
 */
class LegacySettingsImportTest {

    private val legacyKeys = listOf(CART_KEY, ORDERS_KEY, REVIEWS_KEY, FAVORITES_KEY)

    private val margarita = testCocktail("1", name = "Margarita", price = 9.5)
    private val mojito = testCocktail("2", name = "Mojito", price = 11.0)

    private val legacyOrders = listOf(
        Order(
            id = "ORD-1",
            date = "2026-06-01",
            items = listOf(OrderItem("Margarita", 2, 9.5), OrderItem("Mojito", 1, 11.0)),
            total = 30.0,
            status = OrderStatus.DELIVERED
        ),
        Order(id = "ORD-2", date = "2026-06-02", items = emptyList(), total = 0.0, status = OrderStatus.PROCESSING)
    )

    private val legacyReviews = listOf(
        Review(id = "r1", cocktailId = "1", userName = "Robert", rating = 4.5f, comment = "Lovely and tart.", date = "2026-06-01"),
        Review(id = "r2", cocktailId = "2", userName = "Ana", rating = 3.0f, comment = "A bit too minty.", date = "2026-06-03")
    )

    private fun Settings.seedCart(items: List<CocktailCartItem>) =
        putString(CART_KEY, testJson.encodeToString(ListSerializer(CocktailCartItem.serializer()), items))

    private fun Settings.seedOrders(orders: List<Order>) =
        putString(ORDERS_KEY, testJson.encodeToString(ListSerializer(Order.serializer()), orders))

    private fun Settings.seedReviews(reviews: List<Review>) =
        putString(REVIEWS_KEY, testJson.encodeToString(ListSerializer(Review.serializer()), reviews))

    private fun Settings.seedAll() {
        seedCart(listOf(CocktailCartItem(margarita, 2), CocktailCartItem(mojito, 1)))
        seedOrders(legacyOrders)
        seedReviews(legacyReviews)
        putString(FAVORITES_KEY, "2,1")
    }

    private fun favoriteIds(db: CocktailCraftDatabase) =
        db.favoriteCocktailQueries.selectAll().executeAsList().map { it.id }

    @Test
    fun firstAccessImportsEveryStoreAndRemovesTheLegacyKeys() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings().apply { seedAll() }
        val local = createTestLocalDatabase(db, settings)

        val cart = CartRepositoryImpl(local, testJson).getCartItems().getOrThrow()
        assertEquals(listOf(CocktailCartItem(margarita, 2), CocktailCartItem(mojito, 1)), cart)
        assertEquals(legacyOrders, OrderRepositoryImpl(local).getOrders().getOrThrow())
        assertEquals(legacyReviews, ReviewRepositoryImpl(local).getReviews().getOrThrow())
        assertEquals(listOf("2", "1"), favoriteIds(db))
        // Favorites were ids only; the favorites repository resolves them later.
        assertTrue(db.favoriteCocktailQueries.selectAll().executeAsList().all { it.payload == null })

        legacyKeys.forEach { assertNull(settings.getStringOrNull(it), "$it must be removed after import") }
    }

    @Test
    fun reRunningTheImportNeverDuplicatesOrOverwritesRows() = runTest {
        // A crash between the commit and the key removal leaves the keys in
        // place, so the next launch imports the same data again.
        val db = createTestDatabase()
        val settings = MapSettings()
        val import = LegacySettingsImport(settings, testJson)
        settings.seedAll()
        import.run(db)

        // Meanwhile the user changed things; the database is now newer.
        val local = createTestLocalDatabase(db)
        CartRepositoryImpl(local, testJson).updateQuantity("1", 7).getOrThrow()
        OrderRepositoryImpl(local).updateOrderStatus("ORD-2", OrderStatus.CANCELLED).getOrThrow()
        ReviewRepositoryImpl(local).updateReview("r1", 1.0f, "Changed my mind.", "2026-06-09").getOrThrow()

        settings.seedAll()
        import.run(db)

        val cart = CartRepositoryImpl(local, testJson).getCartItems().getOrThrow()
        assertEquals(listOf("1" to 7, "2" to 1), cart.map { it.cocktail.id to it.quantity })
        val orders = OrderRepositoryImpl(local).getOrders().getOrThrow()
        assertEquals(listOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED), orders.map { it.status })
        assertEquals(listOf(2, 0), orders.map { it.items.size })
        val reviews = ReviewRepositoryImpl(local).getReviews().getOrThrow()
        assertEquals(listOf(1.0f, 3.0f), reviews.map { it.rating })
        assertEquals(listOf("2", "1"), favoriteIds(db))
    }

    @Test
    fun importAppendsAfterRowsAlreadyInTheDatabase() = runTest {
        val db = createTestDatabase()
        val local = createTestLocalDatabase(db)
        CartRepositoryImpl(local, testJson).addToCart(CocktailCartItem(testCocktail("9"), 1)).getOrThrow()
        val settings = MapSettings().apply { seedCart(listOf(CocktailCartItem(margarita, 2))) }

        LegacySettingsImport(settings, testJson).run(db)

        val cart = CartRepositoryImpl(local, testJson).getCartItems().getOrThrow()
        assertEquals(listOf("9", "1"), cart.map { it.cocktail.id })
    }

    @Test
    fun missingKeysImportNothing() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings().apply { putString("unrelated", "kept") }
        val local = createTestLocalDatabase(db, settings)

        assertTrue(CartRepositoryImpl(local, testJson).getCartItems().getOrThrow().isEmpty())
        assertTrue(OrderRepositoryImpl(local).getOrders().getOrThrow().isEmpty())
        assertTrue(ReviewRepositoryImpl(local).getReviews().getOrThrow().isEmpty())
        assertTrue(favoriteIds(db).isEmpty())
        assertEquals("kept", settings.getStringOrNull("unrelated"))
    }

    @Test
    fun unreadableBlobsImportNothingAndAreRemoved() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings().apply {
            putString(CART_KEY, "{not valid json")
            putString(ORDERS_KEY, "")
            putString(REVIEWS_KEY, """{"id":"r1"}""") // valid JSON, but not a list
            putString(FAVORITES_KEY, " , ,")
        }
        val local = createTestLocalDatabase(db, settings)

        assertTrue(CartRepositoryImpl(local, testJson).getCartItems().getOrThrow().isEmpty())
        assertTrue(OrderRepositoryImpl(local).getOrders().getOrThrow().isEmpty())
        assertTrue(ReviewRepositoryImpl(local).getReviews().getOrThrow().isEmpty())
        assertTrue(favoriteIds(db).isEmpty())
        // Unreadable data can never be imported, so it isn't retried forever.
        legacyKeys.forEach { assertFalse(settings.hasKey(it), "$it must be removed") }
    }

    @Test
    fun readableEntriesSurviveAnUnreadableNeighbour() = runTest {
        val validOrder = testJson.encodeToString(Order.serializer(), legacyOrders.first())
        val validItem = testJson.encodeToString(CocktailCartItem.serializer(), CocktailCartItem(mojito, 3))
        val settings = MapSettings().apply {
            putString(ORDERS_KEY, """[{"id":"broken"}, $validOrder, 42]""")
            putString(CART_KEY, """[$validItem, {"quantity":1}]""")
        }
        val local = createTestLocalDatabase(createTestDatabase(), settings)

        assertEquals(listOf("ORD-1"), OrderRepositoryImpl(local).getOrders().getOrThrow().map { it.id })
        assertEquals(
            listOf(CocktailCartItem(mojito, 3)),
            CartRepositoryImpl(local, testJson).getCartItems().getOrThrow()
        )
    }

    @Test
    fun legacyOrderJsonWithStringStatusesImportsAsTheMatchingEnum() = runTest {
        // Written by older versions, when Order.status was a raw String
        val settings = MapSettings().apply {
            putString(
                ORDERS_KEY,
                """[{"id":"ORD-1","date":"2026-06-01","items":[],"total":0.0,"status":"Processing"},""" +
                    """{"id":"ORD-2","date":"2026-06-02","items":[],"total":0.0,"status":"Cancelled"}]"""
            )
        }
        val local = createTestLocalDatabase(createTestDatabase(), settings)

        assertEquals(
            listOf(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderRepositoryImpl(local).getOrders().getOrThrow().map { it.status }
        )
    }

    @Test
    fun duplicateIdsInALegacyListKeepTheFirstEntry() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings().apply {
            seedOrders(listOf(legacyOrders.first(), legacyOrders.first().copy(status = OrderStatus.CANCELLED, items = emptyList())))
            putString(FAVORITES_KEY, "1,1,2")
        }
        val local = createTestLocalDatabase(db, settings)

        assertEquals(listOf(legacyOrders.first()), OrderRepositoryImpl(local).getOrders().getOrThrow())
        assertEquals(listOf("1", "2"), favoriteIds(db))
    }

    @Test
    fun aFailedImportKeepsTheKeysForTheNextLaunch() = runTest {
        val settings = MapSettings().apply { seedAll() }
        val brokenDriver = createInMemorySqlDriver().also { it.close() }

        LegacySettingsImport(settings, testJson).run(CocktailCraftDatabase(brokenDriver))

        legacyKeys.forEach { assertTrue(settings.hasKey(it), "$it must survive a failed import") }

        // Next launch, with a working database, the import completes.
        val db = createTestDatabase()
        val local = createTestLocalDatabase(db, settings)
        assertEquals(legacyOrders, OrderRepositoryImpl(local).getOrders().getOrThrow())
        legacyKeys.forEach { assertFalse(settings.hasKey(it)) }
    }

    @Test
    fun importRunsOncePerLocalDatabase() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings()
        val local = createTestLocalDatabase(db, settings)
        val cart = CartRepositoryImpl(local, testJson)
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())

        // Nothing writes these keys any more; the check is a one-time cost.
        settings.seedCart(listOf(CocktailCartItem(margarita, 1)))

        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
        assertTrue(settings.hasKey(CART_KEY))
    }

    @Test
    fun importedCartPayloadsRoundTripTheFullCocktail() = runTest {
        val detailed = margarita.copy(instructions = "Shake well.", tags = listOf("IBA", "Classic"))
        val settings = MapSettings().apply { seedCart(listOf(CocktailCartItem(detailed, 1))) }
        val local = createTestLocalDatabase(createTestDatabase(), settings)

        val stored: Cocktail = CartRepositoryImpl(local, testJson).getCartItems().getOrThrow().single().cocktail
        assertEquals(detailed, stored)
    }
}
