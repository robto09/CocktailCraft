package com.cocktailcraft.data.cache

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.CocktailCodec
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.sumOfMoney
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.LegacySchema
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.testCartRepository
import com.cocktailcraft.testutil.testJson
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * Migration 4.sqm (version 4 -> 5) turns the REAL dollar columns of the
 * order tables into INTEGER cents and adds customer_order_item.cocktail_id.
 * verifySqlDelightMigration checks the resulting schema against the 5.db
 * snapshot; this checks real version 4 rows (and the JSON payloads other
 * tables kept) come through with the same amounts, through every later
 * migration too.
 */
class MoneyInCentsMigrationTest {

    // Demo prices were 5.0 + n / 100.0, which lands a hair off the cent in
    // binary for many n
    private val noisyPrice = 5.0 + 29 / 100.0
    private val otherNoisyPrice = 5.0 + 515 / 100.0

    private fun SqlDriver.insertLegacyOrder(id: String, total: Double, status: String, position: Long) =
        execute(null, "INSERT INTO customer_order(id, date, total, status, position) VALUES (?, ?, ?, ?, ?)", 5) {
            bindString(0, id)
            bindString(1, "2026-0$position-01")
            bindDouble(2, total)
            bindString(3, status)
            bindLong(4, position)
        }

    private fun SqlDriver.insertLegacyItem(
        orderId: String,
        position: Long,
        name: String,
        quantity: Long,
        price: Double
    ) =
        execute(
            null,
            "INSERT INTO customer_order_item(order_id, position, name, quantity, price) VALUES (?, ?, ?, ?, ?)",
            5
        ) {
            bindString(0, orderId)
            bindLong(1, position)
            bindString(2, name)
            bindLong(3, quantity)
            bindDouble(4, price)
        }

    /** A version 4 database holding orders and cocktail payloads as the previous version wrote them. */
    private fun legacyDriver(): SqlDriver = createInMemorySqlDriver(LegacySchema.V4).apply {
        // 2 x 12.50 + 5.29 + 5.99 delivery
        insertLegacyOrder("ORD-1", total = 2 * 12.5 + noisyPrice + 5.99, status = "Delivered", position = 1)
        insertLegacyItem("ORD-1", 0, "Margarita", 2, 12.5)
        insertLegacyItem("ORD-1", 1, "Mojito", 1, noisyPrice)
        // 3 x 16.66 + 10.15 = 60.13, free delivery
        insertLegacyOrder("ORD-2", total = 3 * 16.66 + otherNoisyPrice, status = "Processing", position = 2)
        insertLegacyItem("ORD-2", 0, "Negroni", 3, 16.66)
        insertLegacyItem("ORD-2", 1, "Aviation", 1, otherNoisyPrice)
        // Exactly half a cent: rounds away from zero
        insertLegacyOrder("ORD-3", total = 0.125, status = "Cancelled", position = 3)

        val now = Clock.System.now().toEpochMilliseconds()
        val margarita = """{"id":"11007","name":"Margarita","category":"Cocktail","price":12.5,"rating":4.5}"""
        val mojito = """{"id":"11000","name":"Mojito","category":"Cocktail","price":$noisyPrice}"""
        execute(null, "INSERT INTO cart_item(cocktail_id, payload, quantity, position) VALUES ('11007', ?, 2, 1)", 1) {
            bindString(0, margarita)
        }
        execute(null, "INSERT INTO cart_item(cocktail_id, payload, quantity, position) VALUES ('11000', ?, 1, 2)", 1) {
            bindString(0, mojito)
        }
        execute(null, "INSERT INTO favorite_cocktail(id, payload, position) VALUES ('11000', ?, 1)", 1) {
            bindString(0, mojito)
        }
        execute(
            null,
            "INSERT INTO cached_cocktail(id, category, payload, cached_at, last_accessed) " +
                "VALUES ('11007', 'Cocktail', ?, ?, ?)",
            3
        ) {
            bindString(0, margarita)
            bindLong(1, now)
            bindLong(2, now)
        }
        execute(null, "INSERT INTO recently_viewed_cocktail(id, payload, viewed_at) VALUES ('11000', ?, ?)", 2) {
            bindString(0, mojito)
            bindLong(1, now)
        }
    }

    private fun migratedDatabase(): CocktailCraftDatabase {
        val driver = legacyDriver()
        CocktailCraftDatabase.Schema.migrate(driver, oldVersion = 4, newVersion = CocktailCraftDatabase.Schema.version)
        return CocktailCraftDatabase(driver)
    }

    @Test
    fun orderTotalsAndItemPricesBecomeExactCents() = runTest {
        val orders = OrderRepositoryImpl(createTestLocalDatabase(migratedDatabase())).getOrders().getOrThrow()

        assertEquals(listOf("ORD-1", "ORD-2", "ORD-3"), orders.map { it.id }, "placement order is kept")
        val (first, second, third) = orders
        assertEquals(Money(2500 + 529 + 599), first.total)
        assertEquals(
            listOf(
                OrderItem(name = "Margarita", quantity = 2, price = Money(1250)),
                OrderItem(name = "Mojito", quantity = 1, price = Money(529))
            ),
            first.items
        )
        assertEquals(Money(3029), first.subtotal)
        assertEquals(Money(599), first.deliveryFee)
        assertEquals(OrderStatus.DELIVERED, first.status)

        assertEquals(Money(4998 + 1015), second.total)
        assertEquals(listOf(Money(1666), Money(1015)), second.items.map { it.price })
        assertEquals(Money.ZERO, second.deliveryFee)

        assertEquals(Money(13), third.total)
        assertTrue(third.items.isEmpty())
    }

    @Test
    fun migratedItemsHaveNoCocktailIdSoTheirOrdersCantBeReordered() = runTest {
        val orders = OrderRepositoryImpl(createTestLocalDatabase(migratedDatabase())).getOrders().getOrThrow()

        assertTrue(orders.flatMap { it.items }.all { it.cocktailId == null })
        assertTrue(orders.none { it.canReorder })
    }

    @Test
    fun ordersPlacedAfterTheUpgradeKeepTheirCocktailIds() = runTest {
        val repository = OrderRepositoryImpl(createTestLocalDatabase(migratedDatabase()))
        val order = Order(
            id = "ORD-4",
            date = "2026-10-18",
            items = listOf(OrderItem(name = "Margarita", quantity = 1, price = Money(1250), cocktailId = "11007")),
            total = Money(1849),
            status = OrderStatus.PROCESSING
        )

        repository.placeOrderAndClearCart(order).getOrThrow()

        assertEquals(order, repository.getOrderById("ORD-4").getOrThrow())
        assertTrue(repository.getOrderById("ORD-4").getOrThrow()!!.canReorder)
        assertFalse(repository.getOrderById("ORD-1").getOrThrow()!!.canReorder)
    }

    @Test
    fun legacyCartPayloadsReadTheirDollarPricesAsCents() = runTest {
        val cart = testCartRepository(createTestLocalDatabase(migratedDatabase()))

        val items = cart.getCartItems().getOrThrow()

        assertEquals(listOf(Money(1250), Money(529)), items.map { it.cocktail.price })
        assertEquals(listOf(2, 1), items.map { it.quantity })
        assertEquals(Money(2 * 1250 + 529), items.sumOfMoney { it.lineTotal })
    }

    @Test
    fun legacyCartItemsGainMoreQuantityWithoutLosingTheirPrice() = runTest {
        val cart = testCartRepository(createTestLocalDatabase(migratedDatabase()))

        val margarita = Cocktail(id = "11007", name = "Margarita", price = Money(1250))

        cart.addToCart(CocktailCartItem(margarita, 1)).getOrThrow()

        assertEquals(Money(1250) * 3, cart.getCartItems().getOrThrow().first().lineTotal)
    }

    @Test
    fun legacyCachedAndRecentlyViewedPayloadsReadTheirPricesAsCents() = runTest {
        val cache = CocktailCache(migratedDatabase(), MapSettings(), testJson, AppConfigImpl())

        assertEquals(Money(1250), cache.getCachedCocktail("11007")?.price)
        assertEquals(listOf(Money(529)), cache.getRecentlyViewedCocktails().map { it.price })
    }

    @Test
    fun legacyFavoritePayloadsReadTheirPricesAsCents() {
        val payload = migratedDatabase().favoriteCocktailQueries.selectAll().executeAsList().single().payload

        // The migration leaves payloads in their old shape; reads convert them
        assertFalse(payload!!.contains("priceCents"))
        assertEquals(Money(529), CocktailCodec(testJson).decode(payload).price)
    }
}
