package com.cocktailcraft.data.cache

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.CocktailCodec
import com.cocktailcraft.data.local.CocktailEntity
import com.cocktailcraft.data.local.IngredientEntity
import com.cocktailcraft.data.local.toEntity
import com.cocktailcraft.data.remote.ApiRateLimiter
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.data.repository.CocktailFavoritesRepositoryImpl
import com.cocktailcraft.data.repository.CocktailOfflineRepositoryImpl
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.LegacySchema
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.partialCocktail
import com.cocktailcraft.testutil.testCartRepository
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Migration 5.sqm (version 5 -> 6) makes cached_cocktail the only copy of
 * each cocktail: favorites, cart lines and recently viewed entries drop
 * their JSON copies and refer to it by id. verifySqlDelightMigration checks
 * the resulting schema against the 6.db snapshot; this checks that no
 * cocktail a version 5 install held is lost, and that full details win.
 */
class SingleCocktailCopyMigrationTest {

    private val codec = CocktailCodec(testJson)

    /** A payload as version 5 wrote it: a partial record carried placeholder text. */
    private fun v5Payload(full: Boolean, id: String, name: String, category: String = "Cocktail"): String =
        if (full) {
            codec.encode(testCocktail(id, name = name, category = category))
        } else {
            val partial = partialCocktail(id, name).copy(category = category).toEntity().copy(
                instructions = "Tap to view recipe",
                ingredients = listOf(IngredientEntity("Tap to view ingredients", ""))
            )
            Json.encodeToString(CocktailEntity.serializer(), partial)
        }

    private fun SqlDriver.insert(sql: String, vararg args: Any?) =
        execute(null, sql, args.size) {
            args.forEachIndexed { index, arg ->
                when (arg) {
                    null -> bindString(index, null)
                    is String -> bindString(index, arg)
                    is Long -> bindLong(index, arg)
                    is Int -> bindLong(index, arg.toLong())
                    else -> error("unsupported $arg")
                }
            }
        }

    /**
     * A version 5 database:
     * - "full": cached with full details, on the Cocktail page
     * - "partial": cached partial (from a page), a favorite holding it in full
     * - "shot": not cached, a favorite (full) and a cart line (partial)
     * - "dollars": not cached, a cart line written before cents
     * - "viewed": not cached, recently viewed
     * - "unresolved": an id-only favorite from the old Settings list
     */
    private fun v5Database(): CocktailCraftDatabase {
        val driver = createInMemorySqlDriver(LegacySchema.V5)
        val cached =
            "INSERT INTO cached_cocktail(id, category, payload, cached_at, last_accessed) VALUES (?, ?, ?, ?, ?)"
        driver.insert(cached, "full", "Cocktail", v5Payload(full = true, "full", "Mojito"), 1_000L, 77L)
        driver.insert(cached, "partial", "Cocktail", v5Payload(full = false, "partial", "Aviation"), 1_000L, 78L)
        driver.insert("INSERT INTO category_page VALUES ('Cocktail', 'full', 0, 5)")
        driver.insert("INSERT INTO category_page VALUES ('Cocktail', 'partial', 1, 5)")

        val favorite = "INSERT INTO favorite_cocktail(id, payload, position) VALUES (?, ?, ?)"
        driver.insert(favorite, "partial", v5Payload(full = true, "partial", "Aviation"), 1)
        driver.insert(favorite, "shot", v5Payload(full = true, "shot", "B-52", category = "Shot"), 2)
        driver.insert(favorite, "unresolved", null, 3)

        val cart = "INSERT INTO cart_item(cocktail_id, payload, quantity, position) VALUES (?, ?, ?, ?)"
        driver.insert(cart, "shot", v5Payload(full = false, "shot", "B-52", category = "Shot"), 2, 1)
        driver.insert(cart, "dollars", """{"id":"dollars","name":"Old Fashioned","price":12.5}""", 1, 2)

        driver.insert(
            "INSERT INTO recently_viewed_cocktail(id, payload, viewed_at) VALUES (?, ?, ?)",
            "viewed",
            v5Payload(full = true, "viewed", "Negroni"),
            99L
        )

        CocktailCraftDatabase.Schema.migrate(driver, oldVersion = 5, newVersion = CocktailCraftDatabase.Schema.version)
        return CocktailCraftDatabase(driver)
    }

    private fun CocktailCraftDatabase.stored(id: String) = cachedCocktailQueries.selectById(id).executeAsOneOrNull()

    @Test
    fun everyCopyIsKeptInCachedCocktail() {
        val db = v5Database()

        assertEquals(
            setOf("full", "partial", "shot", "dollars", "viewed"),
            db.cachedCocktailQueries.selectAll(storedCocktail).executeAsList().map { it.id }.toSet()
        )
        // Copies that weren't cached are stale, so the next view refreshes them
        assertEquals(0L, db.stored("shot")?.cached_at)
        assertEquals(1_000L, db.stored("full")?.cached_at)
    }

    @Test
    fun aFullCopyWinsOverAPartialRecord() {
        val db = v5Database()

        val partial = db.stored("partial")!!
        assertEquals(1L, partial.full_details, "the favorite's full copy upgrades the cached partial record")
        assertTrue(codec.decode(partial.payload, hasFullDetails = true).ingredients.isNotEmpty())

        val shot = db.stored("shot")!!
        assertEquals(1L, shot.full_details, "the favorite's full copy beats the cart line's partial one")
    }

    @Test
    fun copiedRowsKeepTheirCategoryAndFlags() {
        val db = v5Database()

        assertEquals("Shot", db.cachedCocktailQueries.selectStored(listOf("shot")).executeAsOne().category)
        assertNull(db.cachedCocktailQueries.selectStored(listOf("dollars")).executeAsOne().category)
        assertEquals(0L, db.stored("dollars")?.full_details, "no ingredients: partial")
        assertEquals(1L, db.stored("full")?.full_details)
    }

    @Test
    fun accessStampsMoveToTheirOwnTable() {
        val db = v5Database()

        assertEquals(78L, db.cachedCocktailQueries.maxLastAccessed().executeAsOne().value_)
        // Deleting a cocktail takes its stamp along
        db.cachedCocktailQueries.deleteById("partial")
        assertEquals(77L, db.cachedCocktailQueries.maxLastAccessed().executeAsOne().value_)
    }

    @Test
    fun favoritesCartAndRecentlyViewedReadTheMigratedRows() = runTest {
        val db = v5Database()
        val local = createTestLocalDatabase(db)
        val cache = CocktailCache(db, testJson, AppConfigImpl())
        val policy = OfflineModePolicy(FakeNetworkMonitor(false))
        val favorites = CocktailFavoritesRepositoryImpl(
            localDatabase = local,
            json = testJson,
            cocktailCache = cache,
            remote = CocktailRemoteDataSource(FakeCocktailApi(), ApiRateLimiter()),
            offlineRepository = CocktailOfflineRepositoryImpl(policy, cache),
            appScope = backgroundScope
        )

        assertEquals(listOf("Aviation", "B-52"), favorites.getFavoriteCocktails().getOrThrow().map { it.name })
        assertTrue(favorites.isCocktailFavorite("unresolved").getOrThrow(), "still a favorite, resolved later")

        val cart = testCartRepository(local).getCartItems().getOrThrow()
        assertEquals(listOf("shot", "dollars"), cart.map { it.cocktail.id })
        assertEquals(listOf(2, 1), cart.map { it.quantity })
        assertEquals(Money(1250), cart.last().cocktail.price, "the old dollar price reads as cents")

        assertEquals(listOf("Negroni"), cache.getRecentlyViewedCocktails().map { it.name })
    }

    @Test
    fun referencedRowsArePinnedAgainstEvictionAndExpiry() = runTest {
        val db = v5Database()
        val oneRowCap = object : AppConfig by AppConfigImpl() {
            override val maxOfflineCocktails: Int = 1
        }
        val cache = CocktailCache(db, testJson, oneRowCap)

        // Opening the cache trims to the cap and sweeps expired rows (cached_at
        // 0 and 1000 are long expired), but every row is referenced by a page,
        // a favorite, the cart or the recently viewed list
        assertEquals(5, cache.getAllCachedCocktails().size)
        cache.cacheCocktails(listOf(testCocktail("new1"), testCocktail("new2")))
        assertFalse(cache.isCocktailCached("new1"), "an unpinned row over the cap is evicted")
        assertTrue(cache.isCocktailCached("shot"))
    }
}
