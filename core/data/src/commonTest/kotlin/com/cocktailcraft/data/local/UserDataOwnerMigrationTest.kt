package com.cocktailcraft.data.local

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.data.repository.ReviewRepositoryImpl
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.LegacySchema
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.legacyCartJson
import com.cocktailcraft.testutil.legacyCocktailsJson
import com.cocktailcraft.testutil.legacyOrdersJson
import com.cocktailcraft.testutil.testCartRepository
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Migration 6.sqm (version 6 -> 7) gives the cart, favorites, recently
 * viewed list, orders and reviews an owner. verifySqlDelightMigration checks
 * the resulting schema against the 7.db snapshot; this checks that the rows
 * an upgraded install already had (in the database, or still in the
 * Settings of older versions) go to whoever is signed in on the first launch
 * after the upgrade, else the guest, and that nothing is lost.
 */
class UserDataOwnerMigrationTest {

    private val codec = CocktailCodec(testJson)

    private fun SqlDriver.run(sql: String, vararg args: Any?) =
        execute(null, sql, args.size) {
            args.forEachIndexed { index, arg ->
                when (arg) {
                    is String -> bindString(index, arg)
                    is Long -> bindLong(index, arg)
                    else -> error("unsupported $arg")
                }
            }
        }

    /** A version 6 database with a cart line, favorite, view, order and review, migrated to today's version. */
    private fun upgradedDatabase(): CocktailCraftDatabase {
        val driver = createInMemorySqlDriver(LegacySchema.V5)
        CocktailCraftDatabase.Schema.migrate(driver, oldVersion = 5, newVersion = 6)
        listOf("1", "2", "3").forEach { id ->
            driver.run(
                "INSERT INTO cached_cocktail(id, category, payload, full_details, cached_at) " +
                    "VALUES (?, 'Cocktail', ?, 1, 0)",
                id,
                codec.encode(testCocktail(id))
            )
        }
        driver.run("INSERT INTO cart_item(cocktail_id, quantity, position, price_cents) VALUES ('1', 2, 1, 900)")
        driver.run("INSERT INTO favorite_cocktail(cocktail_id, position) VALUES ('2', 1)")
        driver.run("INSERT INTO recently_viewed_cocktail(cocktail_id, viewed_at) VALUES ('3', 5)")
        driver.run(
            "INSERT INTO customer_order(id, date, total_cents, status, position) " +
                "VALUES ('ORD-1', '2026-10-01', 2399, 'Delivered', 1)"
        )
        driver.run(
            "INSERT INTO customer_order_item(order_id, position, cocktail_id, name, quantity, price_cents) " +
                "VALUES ('ORD-1', 0, '1', 'Cocktail 1', 2, 900)"
        )
        driver.run(
            "INSERT INTO cocktail_review(id, cocktail_id, user_name, rating, comment, date, position) " +
                "VALUES ('r1', '1', 'Ana', 4.0, 'Nice', '2026-10-01', 1)"
        )

        CocktailCraftDatabase.Schema.migrate(driver, oldVersion = 6, newVersion = CocktailCraftDatabase.Schema.version)
        return CocktailCraftDatabase(driver)
    }

    /** What [ownerId] sees: cart, favorites, recently viewed, orders and reviews. */
    private suspend fun dataOf(db: CocktailCraftDatabase, ownerId: String): List<List<String>> {
        val owner = FixedOwner(ownerId)
        val local = createTestLocalDatabase(db, owner = owner)
        val cache = CocktailCache(db, MapSettings(), testJson, AppConfigImpl(), currentOwner = owner)
        return listOf(
            testCartRepository(local).getCartItems().getOrThrow().map { it.cocktail.id },
            db.favoriteCocktailQueries.selectAll(ownerId).executeAsList().map { it.cocktail_id },
            cache.getRecentlyViewedCocktails().map { it.id },
            OrderRepositoryImpl(local).getOrders().getOrThrow().map { it.id },
            ReviewRepositoryImpl(local).getReviews().getOrThrow().map { it.id }
        )
    }

    private val everything = listOf(listOf("1"), listOf("2"), listOf("3"), listOf("ORD-1"), listOf("r1"))
    private val nothing = List(5) { emptyList<String>() }

    @Test
    fun schemaVersionIsSeven() {
        assertEquals(7L, CocktailCraftDatabase.Schema.version)
    }

    @Test
    fun existingRowsGoToTheUserSignedInAtTheFirstLaunch() = runTest {
        val db = upgradedDatabase()

        assertEquals(everything, dataOf(db, "ana"))
        assertEquals(nothing, dataOf(db, OwnerIds.GUEST), "nobody else sees them afterwards")
        assertEquals(everything, dataOf(db, "ana"))
    }

    @Test
    fun existingRowsGoToTheGuestWhenNobodyIsSignedIn() = runTest {
        val db = upgradedDatabase()

        assertEquals(everything, dataOf(db, OwnerIds.GUEST))
        assertEquals(nothing, dataOf(db, "ana"))
    }

    @Test
    fun cartPricesAndOrderDetailsSurviveTheRebuild() = runTest {
        val db = upgradedDatabase()
        val local = createTestLocalDatabase(db, owner = FixedOwner("ana"))

        val line = testCartRepository(local).getCartItems().getOrThrow().single()
        assertEquals(2, line.quantity)
        assertEquals(Money(900), line.cocktail.price)
        assertEquals(
            Order(
                id = "ORD-1",
                date = "2026-10-01",
                items = listOf(OrderItem(name = "Cocktail 1", quantity = 2, price = Money(900), cocktailId = "1")),
                total = Money(2399),
                status = OrderStatus.DELIVERED
            ),
            OrderRepositoryImpl(local).getOrderById("ORD-1").getOrThrow()
        )
    }

    @Test
    fun legacySettingsDataGoesToTheUserSignedInAtTheFirstLaunch() = runTest {
        // An upgrade from the version that kept the user's data in Settings
        val db = createTestDatabase()
        val settings = MapSettings().apply {
            putString(LegacySettingsImport.CART_KEY, legacyCartJson(listOf(CocktailCartItem(testCocktail("1"), 2))))
            putString(
                LegacySettingsImport.ORDERS_KEY,
                legacyOrdersJson(
                    listOf(Order("ORD-1", "2026-06-01", emptyList(), Money(599), OrderStatus.DELIVERED))
                )
            )
            putString(
                LegacySettingsImport.REVIEWS_KEY,
                testJson.encodeToString(
                    ListSerializer(Review.serializer()),
                    listOf(Review("r1", "1", "Ana", 4f, "Nice", "2026-06-01"))
                )
            )
            putString(LegacySettingsImport.FAVORITES_KEY, "1")
        }
        val anas = createTestLocalDatabase(db, settings, owner = FixedOwner("ana"))

        assertEquals(listOf("1"), testCartRepository(anas).getCartItems().getOrThrow().map { it.cocktail.id })
        assertEquals(listOf("ORD-1"), OrderRepositoryImpl(anas).getOrders().getOrThrow().map { it.id })
        assertEquals(listOf("r1"), ReviewRepositoryImpl(anas).getReviews().getOrThrow().map { it.id })
        assertEquals(listOf("1"), db.favoriteCocktailQueries.selectAll("ana").executeAsList().map { it.cocktail_id })

        val guests = createTestLocalDatabase(db, owner = FixedOwner(OwnerIds.GUEST))
        assertTrue(testCartRepository(guests).getCartItems().getOrThrow().isEmpty())
        assertTrue(OrderRepositoryImpl(guests).getOrders().getOrThrow().isEmpty())
    }

    @Test
    fun legacyRecentlyViewedGoesToTheUserSignedInAtTheFirstLaunch() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings().apply {
            putString(
                CocktailCache.RECENTLY_VIEWED_KEY,
                legacyCocktailsJson(listOf(testCocktail("1")))
            )
        }

        val anas = CocktailCache(db, settings, testJson, AppConfigImpl(), currentOwner = FixedOwner("ana"))
        assertEquals(listOf("1"), anas.getRecentlyViewedCocktails().map { it.id })

        val guest = FixedOwner(OwnerIds.GUEST)
        val guests = CocktailCache(db, MapSettings(), testJson, AppConfigImpl(), currentOwner = guest)
        assertTrue(guests.getRecentlyViewedCocktails().isEmpty())
    }
}
