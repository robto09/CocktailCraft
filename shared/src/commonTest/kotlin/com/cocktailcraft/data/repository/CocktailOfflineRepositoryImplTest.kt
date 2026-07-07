package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CocktailOfflineRepositoryImplTest {

    private val config = AppConfigImpl()

    private fun repository(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): CocktailOfflineRepositoryImpl {
        val cache = CocktailCache(settings, Json { ignoreUnknownKeys = true }, config)
        val remote = CocktailRemoteDataSource(api, CocktailCacheManager())
        return CocktailOfflineRepositoryImpl(
            settings = settings,
            appConfig = config,
            networkMonitor = FakeNetworkMonitor(online),
            cocktailCache = cache,
            remote = remote
        )
    }

    @Test
    fun offlineModeFlagPersistsAcrossInstances() = runTest {
        val settings = MapSettings()
        val repo = repository(settings = settings)

        assertFalse(repo.isOfflineModeEnabled())
        repo.setOfflineMode(true)
        assertTrue(repo.isOfflineModeEnabled())
        assertTrue(settings.getBoolean(config.offlineModeEnabledKey, false))

        // A fresh instance over the same settings sees the persisted flag
        val second = repository(settings = settings)
        assertTrue(second.isOfflineModeEnabled())
    }
}
