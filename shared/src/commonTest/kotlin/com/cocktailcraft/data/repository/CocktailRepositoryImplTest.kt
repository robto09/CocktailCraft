package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.SearchFilters
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

class CocktailRepositoryImplTest {

    private val config = AppConfigImpl()

    // Mirrors the DataModule wiring: remote source + focused offline/favorites
    // repos + the slim search/detail/catalog impl delegating to them.
    private fun repository(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): CocktailRepositoryImpl {
        val networkMonitor = FakeNetworkMonitor(online)
        val cache = CocktailCache(settings, Json { ignoreUnknownKeys = true }, config, networkMonitor = networkMonitor)
        val cacheManager = CocktailCacheManager()
        val remote = CocktailRemoteDataSource(api, cacheManager)
        val offline = CocktailOfflineRepositoryImpl(
            settings = settings,
            appConfig = config,
            networkMonitor = networkMonitor,
            cocktailCache = cache,
            remote = remote
        )
        val favorites = CocktailFavoritesRepositoryImpl(
            settings = settings,
            appConfig = config,
            cocktailCache = cache,
            remote = remote,
            offlineRepository = offline
        )
        return CocktailRepositoryImpl(
            remote = remote,
            cocktailCache = cache,
            cacheManager = cacheManager,
            appConfig = config,
            offlineRepository = offline,
            favoritesRepository = favorites
        )
    }

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

    @Test
    fun favoritesRoundTripPersistsIdsInSettings() = runTest {
        val settings = MapSettings()
        val repo = repository(settings = settings)
        val cocktail = testCocktail("11007")

        repo.addToFavorites(cocktail)
        assertTrue((repo.isCocktailFavorite("11007") as Result.Success).data)
        assertEquals("11007", settings.getStringOrNull(config.favoritesStorageKey))

        // Adding twice must not duplicate the id
        repo.addToFavorites(cocktail)
        assertEquals("11007", settings.getStringOrNull(config.favoritesStorageKey))

        repo.removeFromFavorites(cocktail)
        assertFalse((repo.isCocktailFavorite("11007") as Result.Success).data)
    }

    @Test
    fun getFavoriteCocktailsResolvesIdsThroughApiWhenOnline() = runTest {
        val settings = MapSettings()
        val repo = repository(
            api = FakeCocktailApi(drinks = listOf(margaritaDto())),
            settings = settings
        )
        repo.addToFavorites(testCocktail("11007"))

        val favorites = (repo.getFavoriteCocktails() as Result.Success).data

        assertEquals(listOf("Margarita"), favorites.map { it.name })
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

    @Test
    fun advancedSearchOfflineMatchesCachedNonAlcoholicDisplayString() = runTest {
        val settings = MapSettings()
        val repo = repository(api = FakeCocktailApi(drinks = sampleDrinks()), settings = settings)

        // Online pass caches full records; id 3 stores "Non alcoholic" — the
        // API's display string, not the Non_Alcoholic URL token.
        repo.advancedSearch(SearchFilters(query = "mojito"))
        repo.setOfflineMode(true)

        val result = repo.advancedSearch(SearchFilters(alcoholic = false))

        assertEquals(listOf("3"), (result as Result.Success).data.map { it.id })
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
