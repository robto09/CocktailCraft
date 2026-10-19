package com.cocktailcraft.data.repository

import app.cash.turbine.test
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.ApiRateLimiter
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.partialCocktail
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

class CocktailDetailRepositoryImplTest {

    private val config = AppConfigImpl()

    private class Fixture(
        val detail: CocktailDetailRepositoryImpl,
        val cache: CocktailCache,
        val network: FakeNetworkMonitor
    ) {
        // Wall clock seen by the repository's freshness check
        var nowMs = 0L
    }

    // Mirrors the DataModule wiring: remote source + focused offline repo
    // backing the detail impl's cache-first lookups.
    private fun TestScope.fixture(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): Fixture {
        val networkMonitor = FakeNetworkMonitor(online)
        val policy = OfflineModePolicy(networkMonitor)
        val cache = CocktailCache(
            createTestDatabase(),
            settings,
            Json { ignoreUnknownKeys = true },
            config,
            offlineModePolicy = policy
        )
        val remote = CocktailRemoteDataSource(api, ApiRateLimiter())
        val offline = CocktailOfflineRepositoryImpl(
            offlineModePolicy = policy,
            cocktailCache = cache
        )
        lateinit var fixture: Fixture
        val detail = CocktailDetailRepositoryImpl(
            remote = remote,
            cocktailCache = cache,
            offlineRepository = offline,
            appScope = backgroundScope,
            nowMs = { fixture.nowMs }
        )
        fixture = Fixture(detail, cache, networkMonitor)
        // Rows are stamped with the real clock; start the repository's
        // clock there so freshly written rows count as fresh.
        fixture.nowMs = Clock.System.now().toEpochMilliseconds()
        return fixture
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
        // but while offline it must still be served rather than hit the API.
        val fixture = fixture(
            api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita"))),
            online = false
        )
        fixture.cache.cacheCocktail(partialCocktail("1", name = "Cached Partial"))

        val result = fixture.detail.getCocktailById("1")

        assertEquals("Cached Partial", (result as Result.Success).data?.name)
    }

    @Test
    fun offlineWithNothingCachedReturnsNullSuccess() = runTest {
        val fixture = fixture(
            api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita"))),
            online = false
        )

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

    // --- observeCocktail + refreshCocktail: the database is the source of truth ---

    @Test
    fun observeEmitsTheCachedCopyWithoutHittingRemote() = runTest {
        val api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita")))
        val fixture = fixture(api = api)
        fixture.cache.cacheCocktail(testCocktail("1", name = "Cached Margarita"))

        assertEquals("Cached Margarita", fixture.detail.observeCocktail("1").first()?.name)
        assertEquals(0, api.lookupRequests)
    }

    @Test
    fun forcedRefreshWritesThroughToObservers() = runTest {
        val fixture = fixture(api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Fresh Margarita"))))
        fixture.cache.cacheCocktail(testCocktail("1", name = "Stale Margarita"))

        fixture.detail.observeCocktail("1").test {
            assertEquals("Stale Margarita", awaitItem()?.name)

            assertIs<Result.Success<Unit>>(fixture.detail.refreshCocktail("1", force = true))

            var item = awaitItem()
            while (item?.name != "Fresh Margarita") item = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("Fresh Margarita", fixture.cache.getCachedCocktail("1")?.name)
    }

    @Test
    fun refreshSkipsAFreshFullCopyAndRefetchesWhenStale() = runTest {
        val api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita")))
        val fixture = fixture(api = api)
        fixture.cache.cacheCocktail(testCocktail("1", name = "Cached Margarita"))

        fixture.detail.refreshCocktail("1")
        assertEquals(0, api.lookupRequests, "a fresh full copy is not refetched")

        // Past the window even allowing for the real-clock write stamp
        fixture.nowMs += CatalogRefreshPolicy.FRESHNESS_MS + 60_000
        fixture.detail.refreshCocktail("1")
        assertEquals(1, api.lookupRequests, "a stale copy is refetched")
        assertEquals("Remote Margarita", fixture.cache.getCachedCocktail("1")?.name)
    }

    @Test
    fun refreshAlwaysFetchesAPartialCopy() = runTest {
        val api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita")))
        val fixture = fixture(api = api)
        fixture.cache.cacheCocktail(partialCocktail("1"))

        fixture.detail.refreshCocktail("1")

        assertEquals(1, api.lookupRequests)
        assertTrue(fixture.cache.getCachedCocktail("1")?.hasFullDetails == true)
    }

    @Test
    fun failedRefreshReturnsErrorAndKeepsTheCachedCopy() = runTest {
        val api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita")))
        api.lookupError = RuntimeException("API down")
        val fixture = fixture(api = api)
        fixture.cache.cacheCocktail(testCocktail("1", name = "Cached Margarita"))

        val result = fixture.detail.refreshCocktail("1", force = true)

        assertIs<Result.Error>(result, "the failure must be surfaced, not swallowed")
        assertEquals("Cached Margarita", fixture.detail.observeCocktail("1").first()?.name)
    }

    @Test
    fun refreshWhileOfflineKeepsTheCacheWithoutFetching() = runTest {
        val api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita")))
        val fixture = fixture(api = api, online = false)
        fixture.cache.cacheCocktail(testCocktail("1", name = "Cached Margarita"))

        val result = fixture.detail.refreshCocktail("1", force = true)

        assertEquals(ErrorCode.NETWORK, (result as Result.Error).code)
        assertEquals(0, api.lookupRequests)
        assertEquals("Cached Margarita", fixture.detail.observeCocktail("1").first()?.name)
    }

    @Test
    fun concurrentRefreshesOfOneCocktailShareOneRequest() = runTest {
        val api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita"))).apply { lookupDelayMs = 500 }
        val fixture = fixture(api = api)

        val results = List(3) { async { fixture.detail.refreshCocktail("1", force = true) } }.awaitAll()

        assertEquals(1, api.lookupRequests)
        results.forEach { assertIs<Result.Success<Unit>>(it) }
    }

    @Test
    fun aRefreshCompletesIntoTheCacheWhenItsCallerIsCancelled() = runTest {
        // The detail screen closing mid-fetch must not throw the response away
        val api = FakeCocktailApi(drinks = listOf(remoteDto("1", "Remote Margarita"))).apply { lookupDelayMs = 500 }
        val fixture = fixture(api = api)

        val caller = launch { fixture.detail.refreshCocktail("1", force = true) }
        while (api.lookupRequests == 0) yield()
        caller.cancel()

        assertEquals("Remote Margarita", fixture.detail.observeCocktail("1").first { it != null }?.name)
    }
}
