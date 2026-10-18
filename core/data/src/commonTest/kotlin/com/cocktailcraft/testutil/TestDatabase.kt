package com.cocktailcraft.testutil

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.CocktailEntity
import com.cocktailcraft.data.local.CurrentOwner
import com.cocktailcraft.data.local.FixedOwner
import com.cocktailcraft.data.local.LegacyCartItem
import com.cocktailcraft.data.local.LegacyOrder
import com.cocktailcraft.data.local.LegacyOrderItem
import com.cocktailcraft.data.local.LegacySettingsImport
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.data.local.toEntity
import com.cocktailcraft.data.repository.CartRepositoryImpl
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Order
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.builtins.ListSerializer
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

/**
 * A [LocalDatabase] over [database] for repository tests. Legacy data seeded
 * into [settings] is imported on first access, as on an upgraded install.
 */
internal fun createTestLocalDatabase(
    database: CocktailCraftDatabase = createTestDatabase(),
    settings: Settings = MapSettings(),
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    owner: CurrentOwner = FixedOwner()
): LocalDatabase = LocalDatabase(database, LegacySettingsImport(settings, testJson), ioDispatcher, owner)

/** A [CocktailCache] over the same database as this [LocalDatabase]. */
internal fun LocalDatabase.testCache(): CocktailCache =
    CocktailCache(database, MapSettings(), testJson, AppConfigImpl(), FixedOwner())

/** The cart over [local], with the cocktail cache its lines refer to. */
internal fun testCartRepository(local: LocalDatabase): CartRepositoryImpl =
    CartRepositoryImpl(local, local.testCache(), testJson)

// Settings blobs as older versions wrote them (with cents, not the even
// older dollars: those fixtures are spelled out as JSON where tested)

/** A Settings list of cocktails (the legacy cache and recently viewed keys). */
internal fun legacyCocktailsJson(cocktails: List<Cocktail>): String =
    testJson.encodeToString(ListSerializer(CocktailEntity.serializer()), cocktails.map { it.toEntity() })

internal fun CocktailCartItem.toLegacy() = LegacyCartItem(cocktail.toEntity(), quantity)

/** The Settings cart blob. */
internal fun legacyCartJson(items: List<CocktailCartItem>): String =
    testJson.encodeToString(ListSerializer(LegacyCartItem.serializer()), items.map { it.toLegacy() })

internal fun Order.toLegacy() = LegacyOrder(
    id = id,
    date = date,
    items = items.map { LegacyOrderItem(it.name, it.quantity, it.price.cents, it.cocktailId) },
    totalCents = total.cents,
    status = status.storageValue
)

/** The Settings orders blob. */
internal fun legacyOrdersJson(orders: List<Order>): String =
    testJson.encodeToString(ListSerializer(LegacyOrder.serializer()), orders.map { it.toLegacy() })
