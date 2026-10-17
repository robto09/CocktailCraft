package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CocktailSearchRepositoryImplTest {

    private val config = AppConfigImpl()

    private class Fixture(
        val search: CocktailSearchRepositoryImpl,
        val network: FakeNetworkMonitor,
        val cache: CocktailCache
    )

    // Mirrors the DataModule wiring: remote source + focused offline repo +
    // the shared category fetcher backing the search impl.
    private fun fixture(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): Fixture {
        val networkMonitor = FakeNetworkMonitor(online)
        val policy = OfflineModePolicy(networkMonitor)
        val cache = CocktailCache(createTestDatabase(), settings, Json { ignoreUnknownKeys = true }, config, offlineModePolicy = policy)
        val cacheManager = CocktailCacheManager()
        val remote = CocktailRemoteDataSource(api, cacheManager)
        val offline = CocktailOfflineRepositoryImpl(
            offlineModePolicy = policy,
            cocktailCache = cache
        )
        val fetcher = CocktailCategoryFetcher(
            remote = remote,
            cocktailCache = cache,
            cacheManager = cacheManager,
            offlineRepository = offline
        )
        val search = CocktailSearchRepositoryImpl(
            remote = remote,
            cocktailCache = cache,
            offlineRepository = offline,
            categoryFetcher = fetcher
        )
        return Fixture(search, networkMonitor, cache)
    }

    private fun repository(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): CocktailSearchRepositoryImpl = fixture(api, settings, online).search

    private fun margaritaDto() = CocktailDto(
        id = "11007",
        name = "Margarita",
        category = "Ordinary Drink",
        alcoholic = "Alcoholic",
        glass = "Cocktail glass",
        instructions = "Shake with ice.",
        imageUrl = "https://example.com/margarita.jpg",
        ingredient1 = "Tequila",
        measure1 = "1 1/2 oz"
    )

    @Test
    fun searchByNameMapsDtoToDomainModel() = runTest {
        val repo = repository(api = FakeCocktailApi(drinks = listOf(margaritaDto())))

        val result = repo.searchCocktailsByName("marg")

        val cocktails = (result as Result.Success).data
        assertEquals(1, cocktails.size)
        val cocktail = cocktails.single()
        assertEquals("11007", cocktail.id)
        assertEquals("Margarita", cocktail.name)
        assertEquals("Ordinary Drink", cocktail.category)
        assertEquals("Tequila", cocktail.ingredients.first().name)
    }

    @Test
    fun demoValuesAreDeterministicPerCocktailId() = runTest {
        val repo = repository(api = FakeCocktailApi(drinks = listOf(margaritaDto())))

        val first = (repo.searchCocktailsByName("marg") as Result.Success).data.single()
        val second = (repo.searchCocktailsByName("marg") as Result.Success).data.single()

        assertEquals(first.price, second.price, "price must be stable across fetches")
        assertEquals(first.rating, second.rating)
        assertEquals(first.popularity, second.popularity)
        assertTrue(first.price in 5.0..15.0)
        assertTrue(first.rating in 3.0f..5.0f)
        assertTrue(first.popularity in 1..100)
    }

    // --- advancedSearch: ID-set intersection over the 5 supported filters ---

    private fun drinkDto(
        id: String,
        name: String,
        category: String,
        alcoholic: String,
        glass: String,
        ingredient: String
    ) = CocktailDto(
        id = id,
        name = name,
        category = category,
        alcoholic = alcoholic,
        glass = glass,
        instructions = "Mix well.",
        imageUrl = "https://example.com/$id.jpg",
        ingredient1 = ingredient,
        measure1 = "1 oz"
    )

    private fun sampleDrinks() = listOf(
        drinkDto("1", "Margarita", "Ordinary Drink", "Alcoholic", "Cocktail glass", "Tequila"),
        drinkDto("2", "Mojito", "Cocktail", "Alcoholic", "Highball glass", "Rum"),
        drinkDto("3", "Virgin Mojito", "Cocktail", "Non alcoholic", "Highball glass", "Mint"),
        drinkDto("5", "Mojito Especial", "Ordinary Drink", "Alcoholic", "Highball glass", "Rum")
    )

    @Test
    fun advancedSearchSingleCategoryFilterReturnsThatSet() = runTest {
        val repo = repository(api = FakeCocktailApi(drinks = sampleDrinks()))

        val result = repo.advancedSearch(SearchFilters(category = "Cocktail"))

        val ids = (result as Result.Success).data.map { it.id }.toSet()
        assertEquals(setOf("2", "3"), ids)
    }

    @Test
    fun advancedSearchIntersectsQueryAndCategoryById() = runTest {
        val repo = repository(api = FakeCocktailApi(drinks = sampleDrinks()))

        // "mojito" matches ids 2,3,5; "Ordinary Drink" narrows to 1,5 → intersection {5}
        val result = repo.advancedSearch(SearchFilters(query = "mojito", category = "Ordinary Drink"))

        assertEquals(listOf("5"), (result as Result.Success).data.map { it.id })
    }

    @Test
    fun advancedSearchIntersectsCategoryAndAlcoholicById() = runTest {
        val repo = repository(api = FakeCocktailApi(drinks = sampleDrinks()))

        // Cocktail = {2,3}; Alcoholic = {1,2,5} → intersection {2}
        val result = repo.advancedSearch(SearchFilters(category = "Cocktail", alcoholic = true))

        assertEquals(listOf("2"), (result as Result.Success).data.map { it.id })
    }

    @Test
    fun advancedSearchReturnsErrorWhenAnActiveFilterCallFails() = runTest {
        val api = FakeCocktailApi(drinks = sampleDrinks())
        api.endpointError = RuntimeException("boom")
        val repo = repository(api = api)

        // category succeeds, the alcoholic call throws → the filter is never silently dropped
        val result = repo.advancedSearch(SearchFilters(category = "Cocktail", alcoholic = true))

        assertTrue(result is Result.Error)
    }

    @Test
    fun advancedSearchWithNoFiltersReturnsDefaultCocktailList() = runTest {
        val repo = repository(api = FakeCocktailApi(drinks = sampleDrinks()))

        // Nothing active → default browse (filter.php?c=Cocktail)
        val result = repo.advancedSearch(SearchFilters())

        val ids = (result as Result.Success).data.map { it.id }.toSet()
        assertEquals(setOf("2", "3"), ids)
    }

    // --- SH-1: category fetch failures surface as errors, not empty success ---

    @Test
    fun filterByCategoryReturnsErrorWhenApiFailsAndNoCacheExists() = runTest {
        val api = FakeCocktailApi(drinks = sampleDrinks())
        api.categoryEndpointError = RuntimeException("API down")
        val repo = repository(api = api)

        val result = repo.filterByCategory("Cocktail")

        assertTrue(result is Result.Error, "a real failure with no cache must not become an empty success")
    }

    @Test
    fun filterByCategoryFallsBackToGeneralCacheWhenApiFails() = runTest {
        val api = FakeCocktailApi(drinks = sampleDrinks())
        val fixture = fixture(api = api)

        // A successful name search caches full records for ids 2, 3, 5.
        fixture.search.advancedSearch(SearchFilters(query = "mojito"))

        api.categoryEndpointError = RuntimeException("API down")
        val result = fixture.search.filterByCategory("Cocktail")

        val ids = (result as Result.Success).data.map { it.id }.toSet()
        assertEquals(setOf("2", "3"), ids)
    }

    @Test
    fun filterByCategoryReturnsEmptySuccessForGenuinelyEmptyCategory() = runTest {
        val repo = repository(api = FakeCocktailApi(drinks = sampleDrinks()))

        val result = repo.filterByCategory("Shot")

        assertTrue((result as Result.Success).data.isEmpty())
    }

    // --- offline cache fallback: category stamping + legacy entries + fall-through ---

    @Test
    fun filterByCategoryStampsFetchCategoryOnPartialResults() = runTest {
        val api = FakeCocktailApi(drinks = sampleDrinks())
        api.categoryFilterReturnsPartialDtos = true
        val repo = repository(api = api)

        val result = repo.filterByCategory("Cocktail")

        val cocktails = (result as Result.Success).data
        assertEquals(setOf("2", "3"), cocktails.map { it.id }.toSet())
        assertTrue(cocktails.all { it.category == "Cocktail" },
            "partial filter.php records must be stamped with the category they were fetched by")
    }

    @Test
    fun filterByCategoryOfflineServesLegacyCacheEntriesWithoutCategory() = runTest {
        val fixture = fixture(api = FakeCocktailApi())

        // Entries persisted before category stamping existed carry no category.
        fixture.cache.cacheCocktails(listOf(
            testCocktail("legacy1", category = null),
            testCocktail("legacy2", category = null)
        ))
        fixture.network.setOnline(false)

        val result = fixture.search.filterByCategory(CocktailCategories.DEFAULT)

        assertEquals(setOf("legacy1", "legacy2"),
            (result as Result.Success).data.map { it.id }.toSet(),
            "the default offline browse must surface cached entries that predate category stamping")
    }

    @Test
    fun filterByCategoryOfflineWithEmptyCacheStillAttemptsTheNetwork() = runTest {
        // The network monitor can report a stale "offline" (path updates lag, and
        // the iOS simulator can drop them entirely). With nothing cached there is
        // nothing to lose: attempt the fetch instead of failing preemptively.
        val fixture = fixture(api = FakeCocktailApi(drinks = sampleDrinks()), online = false)

        val result = fixture.search.filterByCategory("Cocktail")

        assertEquals(setOf("2", "3"),
            (result as Result.Success).data.map { it.id }.toSet(),
            "an explicit fetch with an empty cache must try the network even when the monitor says offline")
    }

    @Test
    fun advancedSearchOfflineMatchesCachedNonAlcoholicDisplayString() = runTest {
        val settings = MapSettings()
        val fixture = fixture(api = FakeCocktailApi(drinks = sampleDrinks()), settings = settings)

        // Online pass caches full records; id 3 stores "Non alcoholic" — the
        // API's display string, not the Non_Alcoholic URL token.
        fixture.search.advancedSearch(SearchFilters(query = "mojito"))
        fixture.network.setOnline(false)

        val result = fixture.search.advancedSearch(SearchFilters(alcoholic = false))

        assertEquals(listOf("3"), (result as Result.Success).data.map { it.id })
    }
}
