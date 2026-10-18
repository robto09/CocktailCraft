package com.cocktailcraft.data.cache

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Migration 3.sqm (version 3 -> 4) adds category_page to an existing
 * database without touching the rows already in it. verifySqlDelightMigration
 * checks the resulting schema against the 4.db snapshot; this checks the
 * upgrade path at runtime.
 */
class CategoryPageMigrationTest {

    /**
     * The version 3 schema: today's schema minus category_page, which is
     * all migration 3.sqm adds.
     */
    private object Version3Schema : SqlSchema<QueryResult.Value<Unit>> {
        override val version: Long = 3

        override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
            CocktailCraftDatabase.Schema.create(driver)
            driver.execute(null, "DROP TABLE category_page", 0)
            return QueryResult.Unit
        }

        override fun migrate(
            driver: SqlDriver,
            oldVersion: Long,
            newVersion: Long,
            vararg callbacks: AfterVersion
        ): QueryResult.Value<Unit> = error("not used")
    }

    @Test
    fun upgradingFromVersion3AddsCategoryPagesAndKeepsCachedCocktails() = runTest {
        val driver = createInMemorySqlDriver(Version3Schema)
        val config = AppConfigImpl()
        // Cached by the previous app version
        CocktailCache(CocktailCraftDatabase(driver), MapSettings(), testJson, config)
            .cacheCocktails(listOf(testCocktail("1", name = "Mojito"), testCocktail("2", name = "Aviation")))

        CocktailCraftDatabase.Schema.migrate(driver, oldVersion = 3, newVersion = 4)
        val cache = CocktailCache(CocktailCraftDatabase(driver), MapSettings(), testJson, config)

        assertEquals(
            listOf("2", "1"),
            cache.observeCategoryPage("Cocktail").first().map { it.id },
            "before its first fetch a page falls back to the cached rows, by name"
        )
        cache.cacheCategoryPage("Cocktail", listOf(testCocktail("1"), testCocktail("2")), fetchedAtMs = 7L)
        assertEquals(listOf("1", "2"), cache.observeCategoryPage("Cocktail").first().map { it.id })
        assertEquals(7L, cache.categoryPageFetchedAt("Cocktail"))
    }
}
