package com.cocktailcraft.data.repository

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.ApiRateLimiter
import com.cocktailcraft.data.remote.CategoryDto
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.data.remote.IngredientDto
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CocktailCatalogRepositoryImplTest {

    private val config = AppConfigImpl()

    private class Fixture(
        val catalog: CocktailCatalogRepositoryImpl,
        val api: FakeCocktailApi,
        val cache: CocktailCache,
        val network: FakeNetworkMonitor,
        val offline: CocktailOfflineRepositoryImpl,
        val remote: CocktailRemoteDataSource
    ) {
        // Wall clock seen by the fetcher's freshness check
        var nowMs = 1_000_000_000L
    }

    // Mirrors the DataModule wiring: one cache and one category fetcher
    // behind the catalog impl. The cache runs on the test scheduler: on the
    // real Dispatchers.IO the scheduler idles while a query is off-thread and
    // skips FakeCocktailApi's delays, so overlapping-request tests flake.
    private fun TestScope.fixture(
        api: FakeCocktailApi = FakeCocktailApi(sampleDrinks()),
        online: Boolean = true
    ): Fixture {
        val network = FakeNetworkMonitor(online)
        val policy = OfflineModePolicy(network)
        val cache = CocktailCache(
            createTestDatabase(),
            MapSettings(),
            testJson,
            config,
            ioDispatcher = StandardTestDispatcher(testScheduler),
            offlineModePolicy = policy
        )
        val remote = CocktailRemoteDataSource(api, ApiRateLimiter())
        val offline = CocktailOfflineRepositoryImpl(offlineModePolicy = policy, cocktailCache = cache)
        lateinit var fixture: Fixture
        val fetcher = CocktailCategoryFetcher(
            remote = remote,
            cocktailCache = cache,
            offlineRepository = offline,
            appScope = backgroundScope,
            nowMs = { fixture.nowMs }
        )
        fixture = Fixture(CocktailCatalogRepositoryImpl(remote, fetcher), api, cache, network, offline, remote)
        return fixture
    }

    /**
     * A new repository and fetcher over the same database, on the same
     * clock: the app after a restart (nothing carried over in memory).
     */
    private fun TestScope.restarted(fixture: Fixture): CocktailCatalogRepositoryImpl = with(fixture) {
        CocktailCatalogRepositoryImpl(
            remote,
            CocktailCategoryFetcher(remote, cache, offline, backgroundScope, nowMs = { nowMs })
        )
    }

    private fun drink(id: String, name: String, category: String = "Cocktail") = CocktailDto(
        id = id,
        name = name,
        category = category,
        alcoholic = "Alcoholic",
        instructions = "Mix well.",
        imageUrl = "https://example.com/$id.jpg",
        ingredient1 = "Rum",
        measure1 = "1 oz"
    )

    // API (alphabetical) order within each category
    private fun sampleDrinks() = listOf(
        drink("2", "Mojito"),
        drink("3", "Virgin Mojito"),
        drink("1", "Margarita", category = "Ordinary Drink")
    )

    private fun List<Cocktail>.ids() = map { it.id }

    /** Skips intermediate emissions (a write can land in two steps) until [predicate] holds. */
    private suspend fun ReceiveTurbine<List<Cocktail>>.awaitUntil(
        predicate: (List<Cocktail>) -> Boolean
    ): List<Cocktail> {
        var item = awaitItem()
        while (!predicate(item)) item = awaitItem()
        return item
    }

    // --- getCategories / getIngredients: list.php DTO name mapping ---

    @Test
    fun getCategoriesMapsDtoNames() = runTest {
        val fixture = fixture()
        fixture.api.categories = listOf(CategoryDto("Cocktail"), CategoryDto("Shot"))

        val result = fixture.catalog.getCategories()

        assertEquals(listOf("Cocktail", "Shot"), (result as Result.Success).data)
    }

    @Test
    fun getIngredientsMapsDtoNames() = runTest {
        val fixture = fixture()
        fixture.api.ingredients = listOf(IngredientDto("Gin"), IngredientDto("Tequila"))

        val result = fixture.catalog.getIngredients()

        assertEquals(listOf("Gin", "Tequila"), (result as Result.Success).data)
    }

    // --- Category pages: observed from the database, refreshed into it ---

    @Test
    fun observeEmitsCachedRowsBeforeAnyRefresh() = runTest {
        val fixture = fixture()
        fixture.cache.cacheCocktails(
            listOf(
                testCocktail("b", name = "Bramble"),
                testCocktail("a", name = "Aviation"),
                testCocktail("s", name = "Slammer", category = "Shot")
            )
        )

        val page = fixture.catalog.observeCategory("Cocktail").first()

        assertEquals(listOf("a", "b"), page.ids(), "stored rows of the category, by name")
        assertEquals(0, fixture.api.categoryRequests, "observing alone must not hit the network")
    }

    @Test
    fun refreshWritesThroughToObservers() = runTest {
        val fixture = fixture()

        fixture.catalog.observeCategory("Cocktail").test {
            assertTrue(awaitItem().isEmpty())

            assertIs<Result.Success<Unit>>(fixture.catalog.refreshCategory("Cocktail"))

            assertEquals(listOf("2", "3"), awaitUntil { it.isNotEmpty() }.ids())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("Mojito", fixture.cache.getCachedCocktail("2")?.name, "the page is written to the database")
    }

    @Test
    fun refreshedPageKeepsApiOrderAndCachedFullDetails() = runTest {
        val api = FakeCocktailApi(sampleDrinks()).apply { categoryFilterReturnsPartialDtos = true }
        val fixture = fixture(api)
        fixture.cache.cacheCocktail(testCocktail("3", name = "Virgin Mojito"))

        fixture.catalog.refreshCategory("Cocktail")
        val page = fixture.catalog.observeCategory("Cocktail").first()

        assertEquals(listOf("2", "3"), page.ids())
        assertFalse(page[0].hasFullDetails, "id 2 only has the filter endpoint's placeholder")
        assertTrue(page[1].hasFullDetails, "a placeholder must not replace the cached full record")
        assertTrue(page.all { it.category == "Cocktail" }, "partial records are stamped with their category")
    }

    @Test
    fun detailWrittenLaterReachesThePageObservers() = runTest {
        val api = FakeCocktailApi(sampleDrinks()).apply { categoryFilterReturnsPartialDtos = true }
        val fixture = fixture(api)
        fixture.catalog.refreshCategory("Cocktail")

        fixture.catalog.observeCategory("Cocktail").test {
            assertFalse(awaitItem().first { it.id == "2" }.hasFullDetails)

            // e.g. the detail screen fetching lookup.php for id 2
            fixture.cache.cacheCocktail(testCocktail("2", name = "Mojito"))

            val updated = awaitUntil { page -> page.first { it.id == "2" }.hasFullDetails }
            assertEquals(listOf("2", "3"), updated.ids())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun failedRefreshReturnsErrorAndKeepsTheStoredPage() = runTest {
        val fixture = fixture()
        fixture.catalog.refreshCategory("Cocktail")
        fixture.api.categoryEndpointError = RuntimeException("API down")

        val result = fixture.catalog.refreshCategory("Cocktail", force = true)

        assertIs<Result.Error>(result, "the failure must be surfaced, not swallowed")
        assertEquals(listOf("2", "3"), fixture.catalog.observeCategory("Cocktail").first().ids())
    }

    @Test
    fun offlineRefreshKeepsTheCacheWithoutHittingTheNetwork() = runTest {
        val fixture = fixture()
        fixture.catalog.refreshCategory("Cocktail")
        fixture.network.setOnline(false)

        val result = fixture.catalog.refreshCategory("Cocktail", force = true)

        assertEquals(ErrorCode.NETWORK, (result as Result.Error).code)
        assertEquals(1, fixture.api.categoryRequests, "offline with a stored page: no request")
        assertEquals(listOf("2", "3"), fixture.catalog.observeCategory("Cocktail").first().ids())
    }

    @Test
    fun concurrentRefreshesShareOneRequest() = runTest {
        val api = FakeCocktailApi(sampleDrinks()).apply { categoryDelayMs = 500 }
        val fixture = fixture(api)

        val results = List(3) { async { fixture.catalog.refreshCategory("Cocktail", force = true) } }.awaitAll()

        assertEquals(1, api.categoryRequests, "callers arriving mid-flight must share its result")
        results.forEach { assertIs<Result.Success<Unit>>(it) }
    }

    @Test
    fun concurrentRefreshesShareOneFailure() = runTest {
        val api = FakeCocktailApi(sampleDrinks()).apply {
            categoryDelayMs = 500
            categoryEndpointError = RuntimeException("API down")
        }
        val fixture = fixture(api)

        val results = List(2) { async { fixture.catalog.refreshCategory("Cocktail") } }.awaitAll()

        assertEquals(1, api.categoryRequests)
        results.forEach { assertIs<Result.Error>(it) }
    }

    @Test
    fun freshPageIsOnlyRefetchedWhenForcedOrStale() = runTest {
        val fixture = fixture()

        fixture.catalog.refreshCategory("Cocktail")
        fixture.catalog.refreshCategory("Cocktail")
        assertEquals(1, fixture.api.categoryRequests, "a fresh page is served from the database")

        fixture.catalog.refreshCategory("Cocktail", force = true)
        assertEquals(2, fixture.api.categoryRequests, "force always refetches")

        fixture.nowMs += CatalogRefreshPolicy.FRESHNESS_MS
        fixture.catalog.refreshCategory("Cocktail")
        assertEquals(3, fixture.api.categoryRequests, "a stale page is refetched")
    }

    @Test
    fun pageStillListsCocktailsLaterCachedWithoutCategory() = runTest {
        val api = FakeCocktailApi(sampleDrinks()).apply { categoryFilterReturnsPartialDtos = true }
        val fixture = fixture(api)
        fixture.catalog.refreshCategory("Cocktail")

        // The ingredient filter returns partial records without a category.
        fixture.cache.cacheCocktail(
            fixture.remote.mapToDomain(
                CocktailDto(id = "2", name = "Mojito", imageUrl = "https://example.com/2.jpg"),
                hasFullDetails = false
            )
        )

        assertEquals(listOf("2", "3"), fixture.catalog.observeCategory("Cocktail").first().ids())
        assertEquals(
            "Cocktail",
            fixture.cache.getCachedCocktail("2")?.category,
            "the row keeps its category for the category-column fallback"
        )
    }

    @Test
    fun pageOrderIsPersistedAcrossARestart() = runTest {
        // The API's order, deliberately not alphabetical
        val api = FakeCocktailApi(listOf(drink("z", "Zombie"), drink("a", "Aviation"), drink("m", "Mojito")))
        val fixture = fixture(api)
        fixture.catalog.refreshCategory("Cocktail")

        assertEquals(listOf("z", "a", "m"), restarted(fixture).observeCategory("Cocktail").first().ids())
    }

    @Test
    fun refetchedPageReplacesTheStoredPage() = runTest {
        val fixture = fixture()
        fixture.catalog.refreshCategory("Cocktail")

        fixture.api.drinks = listOf(drink("3", "Virgin Mojito"), drink("4", "Zombie"))
        fixture.catalog.refreshCategory("Cocktail", force = true)

        assertEquals(
            listOf("3", "4"),
            fixture.catalog.observeCategory("Cocktail").first().ids(),
            "a cocktail the API no longer lists drops off the page"
        )
    }

    @Test
    fun freshnessSurvivesANewRepositoryInstance() = runTest {
        val fixture = fixture()
        fixture.catalog.refreshCategory("Cocktail")

        val restarted = restarted(fixture)
        restarted.refreshCategory("Cocktail")
        assertEquals(1, fixture.api.categoryRequests, "the stored fetch time still counts as fresh")

        fixture.nowMs += CatalogRefreshPolicy.FRESHNESS_MS
        restarted.refreshCategory("Cocktail")
        assertEquals(2, fixture.api.categoryRequests, "and goes stale on the same clock")
    }

    @Test
    fun pageSurvivesARestartFromTheDatabase() = runTest {
        val fixture = fixture()
        fixture.catalog.refreshCategory("Cocktail")
        fixture.network.setOnline(false)

        val restarted = restarted(fixture)

        assertEquals(listOf("2", "3"), restarted.observeCategory("Cocktail").first().ids())
        assertIs<Result.Error>(restarted.refreshCategory("Cocktail", force = true), "offline: nothing refreshed")
        assertEquals(1, fixture.api.categoryRequests)
    }
}
