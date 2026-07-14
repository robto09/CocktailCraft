package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CocktailOfflineRepositoryImplTest {

    private val config = AppConfigImpl()

    private class Fixture(
        val repo: CocktailOfflineRepositoryImpl,
        val cache: CocktailCache,
        val cacheManager: CocktailCacheManager,
        val settings: Settings
    )

    private fun fixture(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): Fixture {
        val policy = OfflineModePolicy(settings, config, FakeNetworkMonitor(online))
        val cache = CocktailCache(settings, Json { ignoreUnknownKeys = true }, config, offlineModePolicy = policy)
        val cacheManager = CocktailCacheManager()
        val remote = CocktailRemoteDataSource(api, cacheManager)
        val repo = CocktailOfflineRepositoryImpl(
            offlineModePolicy = policy,
            cocktailCache = cache,
            cacheManager = cacheManager,
            remote = remote
        )
        return Fixture(repo, cache, cacheManager, settings)
    }

    private fun repository(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): CocktailOfflineRepositoryImpl = fixture(api, settings, online).repo

    @Test
    fun clearCachePurgesPersistedAndVolatileLayers() = runTest {
        // SH-3/SH-11: "Clear Cache" must empty the persistent Settings-backed
        // cache AND the volatile in-memory layer, or the data reappears.
        val fixture = fixture()
        fixture.cache.cacheCocktail(testCocktail("1"))
        fixture.cacheManager.setCategoryCache("Cocktail", listOf(testCocktail("1")))
        assertTrue(fixture.settings.hasKey(CocktailCache.ALL_COCKTAILS_KEY))

        val result = fixture.repo.clearCache()

        assertTrue(result is Result.Success)
        assertEquals(0, fixture.cache.getCachedCocktailCount())
        assertTrue(fixture.cacheManager.getCategoryCache("Cocktail").isEmpty())
        assertFalse(fixture.settings.hasKey(CocktailCache.ALL_COCKTAILS_KEY))
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
