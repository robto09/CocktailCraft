package com.cocktailcraft.data.local

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.repository.OrderRepositoryImpl
import com.cocktailcraft.data.repository.ReviewRepositoryImpl
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.LegacySchema
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.testCartRepository
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

    /** What [ownerId] owns: cart, favorites, recently viewed, orders and reviews (everyone reads every review). */
    private suspend fun dataOf(db: CocktailCraftDatabase, ownerId: String): List<List<String>> {
        val owner = FixedOwner(ownerId)
        val local = createTestLocalDatabase(db, owner = owner)
        val cache = CocktailCache(db, testJson, AppConfigImpl(), currentOwner = owner)
        return listOf(
            testCartRepository(local).getCartItems().getOrThrow().map { it.cocktail.id },
            db.favoriteCocktailQueries.selectAll(ownerId).executeAsList().map { it.cocktail_id },
            cache.getRecentlyViewedCocktails().map { it.id },
            OrderRepositoryImpl(local).getOrders().getOrThrow().map { it.id },
            ReviewRepositoryImpl(local).getReviews().getOrThrow().filter { it.isMine }.map { it.id }
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
}
