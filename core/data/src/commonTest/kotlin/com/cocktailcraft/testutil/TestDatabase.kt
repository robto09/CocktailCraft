package com.cocktailcraft.testutil

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.CurrentOwner
import com.cocktailcraft.data.local.FixedOwner
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.data.repository.CartRepositoryImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json

/**
 * A fresh, empty in-memory SQLite driver with the app database's schema
 * already created (see :core:testing's createInMemorySqlDriver(schema)).
 * Each call returns an independent database.
 */
internal fun createInMemorySqlDriver(): SqlDriver = createInMemorySqlDriver(CocktailCraftDatabase.Schema)

/** A fresh in-memory [CocktailCraftDatabase] for cache and repository tests. */
internal fun createTestDatabase(driver: SqlDriver = createInMemorySqlDriver()): CocktailCraftDatabase =
    CocktailCraftDatabase(driver)

/** JSON settings matching the tests' expectations of the production instance. */
internal val testJson = Json { ignoreUnknownKeys = true }

/** A [LocalDatabase] over [database] for repository tests. */
internal fun createTestLocalDatabase(
    database: CocktailCraftDatabase = createTestDatabase(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    owner: CurrentOwner = FixedOwner()
): LocalDatabase = LocalDatabase(database, ioDispatcher, owner)

/** A [CocktailCache] over the same database as this [LocalDatabase]. */
internal fun LocalDatabase.testCache(): CocktailCache =
    CocktailCache(database, testJson, AppConfigImpl(), FixedOwner())

/** The cart over [local], with the cocktail cache its lines refer to. */
internal fun testCartRepository(local: LocalDatabase): CartRepositoryImpl =
    CartRepositoryImpl(local, local.testCache(), testJson)
