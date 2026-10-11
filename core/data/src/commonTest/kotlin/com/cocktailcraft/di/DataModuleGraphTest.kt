package com.cocktailcraft.di

import app.cash.sqldelight.db.SqlDriver
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.createInMemorySqlDriver
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * The data layer's slice of the production Koin graph (networkModule +
 * dataModule) over the same platform stand-ins KoinDependencyGraphTest uses.
 * That full-graph test lives in :shared and cannot name this module's
 * internal singletons, so their wiring is verified here.
 */
class DataModuleGraphTest {

    /** Test stand-ins for the platform-supplied bindings. */
    private val testPlatformModule = module {
        single<AppConfig> { AppConfigImpl() }
        single<Settings> { MapSettings() }
        single<Settings>(secureSettingsQualifier) { MapSettings() }
        single<NetworkMonitor> { FakeNetworkMonitor() }
        single<SqlDriver> { createInMemorySqlDriver() }
    }

    private fun startTestKoin(): Koin = startKoin {
        modules(networkModule, dataModule, testPlatformModule)
    }.koin

    @AfterTest
    fun tearDownKoin() {
        stopKoin()
    }

    @Test
    fun appDatabaseResolves() {
        val koin = startTestKoin()

        // CocktailCache and LocalDatabase only inject the database lazily, so
        // resolve it directly to prove the SqlDriver -> database wiring is
        // complete.
        assertNotNull(koin.get<CocktailCraftDatabase>())
        assertNotNull(koin.get<CocktailCache>())
        assertSame(koin.get<LocalDatabase>(), koin.get<LocalDatabase>())
    }
}
