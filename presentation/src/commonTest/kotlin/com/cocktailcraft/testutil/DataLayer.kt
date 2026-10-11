package com.cocktailcraft.testutil

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.di.dataModule
import com.cocktailcraft.di.secureSettingsQualifier
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest

/**
 * Base for the ViewModel integration tests that run against the production
 * data layer (VM + use case + real repository + storage). :core:data is a
 * test-only dependency of this module, and its repository implementations
 * are internal, so [dataLayer] resolves them through dataModule instead of
 * constructing them.
 */
abstract class DataLayerTest : MainDispatcherTest() {

    private val graphs = mutableListOf<KoinApplication>()

    /**
     * An isolated (non-global) Koin graph over a fresh in-memory database,
     * [settings] for both Settings bindings, and [ioDispatcher] for the
     * database/Settings work (pass the test's scheduler).
     */
    protected fun dataLayer(
        ioDispatcher: CoroutineDispatcher,
        settings: Settings = MapSettings()
    ): Koin = koinApplication {
        modules(
            dataModule,
            module {
                single<Settings> { settings }
                single<Settings>(secureSettingsQualifier) { settings }
                single<SqlDriver> { createInMemorySqlDriver(CocktailCraftDatabase.Schema) }
                // Overrides dataModule's Dispatchers.IO binding
                single<CoroutineDispatcher>(named("ioDispatcher")) { ioDispatcher }
            }
        )
    }.also { graphs += it }.koin

    // dataModule is a single Module object whose singles keep their instance
    // until the graph that created them closes — close every graph so the
    // next test (or the DI graph tests) never sees this test's repositories.
    @AfterTest
    fun closeDataLayers() {
        graphs.forEach { it.close() }
        graphs.clear()
    }
}
