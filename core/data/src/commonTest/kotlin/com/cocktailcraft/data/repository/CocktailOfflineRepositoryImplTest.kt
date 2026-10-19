package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CocktailOfflineRepositoryImplTest {

    private val config = AppConfigImpl()

    private class Fixture(val repo: CocktailOfflineRepositoryImpl, val cache: CocktailCache)

    private fun fixture(online: Boolean = true): Fixture {
        val policy = OfflineModePolicy(FakeNetworkMonitor(online))
        val cache = CocktailCache(
            createTestDatabase(),
            MapSettings(),
            Json { ignoreUnknownKeys = true },
            config,
            offlineModePolicy = policy
        )
        return Fixture(CocktailOfflineRepositoryImpl(offlineModePolicy = policy, cocktailCache = cache), cache)
    }

    @Test
    fun isOfflineMirrorsNetworkState() = runTest {
        assertFalse(fixture(online = true).repo.isOffline())
        assertTrue(fixture(online = false).repo.isOffline())
    }

    @Test
    fun recentlyViewedCocktailsComeFromThePersistentCache() = runTest {
        val fixture = fixture()
        fixture.cache.addToRecentlyViewed(testCocktail("1"))

        val result = fixture.repo.getRecentlyViewedCocktails()

        assertEquals(listOf("1"), (result as Result.Success).data.map { it.id })
    }
}
