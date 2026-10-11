package com.cocktailcraft.testutil

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.local.LegacySettingsImport
import com.cocktailcraft.data.local.LocalDatabase
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json

/**
 * A fresh, empty in-memory SQLite driver with the schema already created:
 * the JDBC sqlite driver on the Android host, the native driver's in-memory
 * mode on iOS. Each call returns an independent database.
 */
internal expect fun createInMemorySqlDriver(): SqlDriver

/** A fresh in-memory [CocktailCraftDatabase] for cache and repository tests. */
internal fun createTestDatabase(driver: SqlDriver = createInMemorySqlDriver()): CocktailCraftDatabase =
    CocktailCraftDatabase(driver)

/** JSON settings matching the tests' expectations of the production instance. */
internal val testJson = Json { ignoreUnknownKeys = true }

/**
 * A [LocalDatabase] over [database] for repository tests. Legacy data seeded
 * into [settings] is imported on first access, as on an upgraded install.
 */
internal fun createTestLocalDatabase(
    database: CocktailCraftDatabase = createTestDatabase(),
    settings: Settings = MapSettings(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): LocalDatabase = LocalDatabase(database, LegacySettingsImport(settings, testJson), ioDispatcher)
