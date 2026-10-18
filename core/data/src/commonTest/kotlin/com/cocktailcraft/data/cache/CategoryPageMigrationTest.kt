package com.cocktailcraft.data.cache

import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.CocktailCodec
import com.cocktailcraft.testutil.LegacySchema
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * Migration 3.sqm (version 3 -> 4) adds category_page to an existing
 * database without touching the rows already in it. verifySqlDelightMigration
 * checks the resulting schema against the 4.db snapshot; this checks the
 * upgrade path at runtime, through every later migration too.
 */
class CategoryPageMigrationTest {

    @Test
    fun upgradingFromVersion3AddsCategoryPagesAndKeepsCachedCocktails() = runTest {
        val driver = createInMemorySqlDriver(LegacySchema.V3)
        val now = Clock.System.now().toEpochMilliseconds()
        // Cached by a version 3 app
        listOf(testCocktail("1", name = "Mojito"), testCocktail("2", name = "Aviation")).forEach { cocktail ->
            driver.execute(
                null,
                "INSERT INTO cached_cocktail(id, category, payload, cached_at, last_accessed) VALUES (?, ?, ?, ?, ?)",
                5
            ) {
                bindString(0, cocktail.id)
                bindString(1, cocktail.category)
                bindString(2, CocktailCodec(testJson).encode(cocktail))
                bindLong(3, now)
                bindLong(4, now)
            }
        }

        CocktailCraftDatabase.Schema.migrate(driver, oldVersion = 3, newVersion = CocktailCraftDatabase.Schema.version)
        val cache = CocktailCache(CocktailCraftDatabase(driver), MapSettings(), testJson, AppConfigImpl())

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
