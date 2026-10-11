package com.cocktailcraft.testutil

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase

/**
 * A fresh, empty in-memory SQLite driver with the schema already created:
 * the JDBC sqlite driver on the Android host, the native driver's in-memory
 * mode on iOS. Each call returns an independent database.
 */
internal expect fun createInMemorySqlDriver(): SqlDriver

/** A fresh in-memory [CocktailCraftDatabase] for cache and repository tests. */
internal fun createTestDatabase(driver: SqlDriver = createInMemorySqlDriver()): CocktailCraftDatabase =
    CocktailCraftDatabase(driver)
