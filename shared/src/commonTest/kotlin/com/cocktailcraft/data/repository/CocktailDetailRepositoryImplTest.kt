package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.partialCocktail
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CocktailDetailRepositoryImplTest {

    private val config = AppConfigImpl()

    private class Fixture(
        val detail: CocktailDetailRepositoryImpl,
        val offline: CocktailOfflineRepositoryImpl,
        val cache: CocktailCache
    )

    // Mirrors the DataModule wiring: remote source + focused offline repo
    // backing the detail impl's cache-first lookups.
    private fun fixture(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): Fixture {
        val networkMonitor = FakeNetworkMonitor(online)
        val policy = OfflineModePolicy(settings, config, networkMonitor)
        val cache = CocktailCache(settings, Json { ignoreUnknownKeys = true }, config, offlineModePolicy = policy)
        val cacheManager = CocktailCacheManager()
        val remote = CocktailRemoteDataSource(api, cacheManager)
        val offline = CocktailOfflineRepositoryImpl(
            offlineModePolicy = policy,
            cocktailCache = cache,
            cacheManager = cacheManager,
            remote = remote
        )
        val detail = CocktailDetailRepositoryImpl(
            remote = remote,
            cocktailCache = cache,
            appConfig = config,
            offlineRepository = offline
        )
        return Fixture(detail, offline, cache)
    }

    private fun remoteDto(id: String, name: String) = CocktailDto(
        id = id,
        name = name,
        category = "Ordinary Drink",
        alcoholic = "Alcoholic",
        glass = "Cocktail glass",
        instructions = "Shake with ice.",
        imageUrl = "https://example.com/$id.jpg",
        ingredient1 = "Tequila",
        measure1 = "1 1/2 oz"
    )

    // --- getCocktailById: cache-first with API fallback ---

    @Test
    fun cachedFullDetailsAreServedWithoutHittingRemote() = runTest {
        // If the remote were consulted, the name would differ.
        val fixture = fixture(api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita"))))
        fixture.cache.cacheCocktail(testCocktail("1", name = "Cached Margarita"))

        val result = fixture.detail.getCocktailById("1")

        assertEquals("Cached Margarita", (result as Result.Success).data?.name)
    }

    @Test
    fun offlineReturnsCachedPartialInsteadOfFetching() = runTest {
        // A partial cache entry doesn't satisfy the full-details fast path,
        // but offline mode must still serve it rather than hit the API.
        val fixture = fixture(api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita"))))
        fixture.cache.cacheCocktail(partialCocktail("1", name = "Cached Partial"))
        fixture.offline.setOfflineMode(true)

        val result = fixture.detail.getCocktailById("1")

        assertEquals("Cached Partial", (result as Result.Success).data?.name)
    }

    @Test
    fun offlineWithNothingCachedReturnsNullSuccess() = runTest {
        val fixture = fixture(api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita"))))
        fixture.offline.setOfflineMode(true)

        val result = fixture.detail.getCocktailById("1")

        assertNull((result as Result.Success).data)
    }

    @Test
    fun apiFallbackCachesTheFetchedCocktail() = runTest {
        val api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita")))
        val fixture = fixture(api = api)

        val first = fixture.detail.getCocktailById("1")

        assertEquals("Remote Margarita", (first as Result.Success).data?.name)
        assertEquals("Remote Margarita", fixture.cache.getCachedCocktail("1")?.name)

        // The cached copy now serves lookups even if the API stops returning it.
        api.drinks = emptyList()
        val second = fixture.detail.getCocktailById("1")

        assertEquals("Remote Margarita", (second as Result.Success).data?.name)
    }

    // --- refreshCocktailById: always refetch, then update the cache ---

    @Test
    fun refreshReplacesStaleCacheWithFreshApiData() = runTest {
        val fixture = fixture(api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Fresh Margarita"))))
        fixture.cache.cacheCocktail(testCocktail("1", name = "Stale Margarita"))

        val result = fixture.detail.refreshCocktailById("1")

        assertEquals("Fresh Margarita", (result as Result.Success).data?.name)
        assertEquals("Fresh Margarita", fixture.cache.getCachedCocktail("1")?.name)
    }

    @Test
    fun refreshWhileOfflineReturnsCachedWithoutFetching() = runTest {
        val fixture = fixture(api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita"))))
        fixture.cache.cacheCocktail(testCocktail("1", name = "Cached Margarita"))
        fixture.offline.setOfflineMode(true)

        val result = fixture.detail.refreshCocktailById("1")

        assertEquals("Cached Margarita", (result as Result.Success).data?.name)
    }

    // --- getCocktailImageUrl derivation ---

    @Test
    fun imageUrlPrefersTheCocktailsOwnUrl() = runTest {
        val fixture = fixture()

        val url = fixture.detail.getCocktailImageUrl(testCocktail("11007"))

        assertEquals("https://example.com/11007.jpg", url)
    }

    @Test
    fun imageUrlIsDerivedFromIdWhenMissing() = runTest {
        val fixture = fixture()
        val cocktail = testCocktail("11007").copy(imageUrl = null)

        val url = fixture.detail.getCocktailImageUrl(cocktail)

        assertEquals("${config.imageBaseUrl}/${config.cocktailsImagePath}/11007.jpg", url)
    }

    @Test
    fun imageUrlIsEmptyWhenBothUrlAndIdAreBlank() = runTest {
        val fixture = fixture()
        val cocktail = testCocktail("x").copy(id = "", imageUrl = "")

        assertEquals("", fixture.detail.getCocktailImageUrl(cocktail))
    }
}
