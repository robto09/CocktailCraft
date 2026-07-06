package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CocktailDto
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

class CocktailRepositoryImplTest {

    private val config = AppConfigImpl()

    // Mirrors the DataModule wiring: remote source + focused offline/favorites
    // repos + the slim search/detail/catalog impl delegating to them.
    private fun repository(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): CocktailRepositoryImpl {
        val cache = CocktailCache(settings, Json { ignoreUnknownKeys = true }, config)
        val cacheManager = CocktailCacheManager()
        val remote = CocktailRemoteDataSource(api, cacheManager)
        val offline = CocktailOfflineRepositoryImpl(
            settings = settings,
            appConfig = config,
            networkMonitor = FakeNetworkMonitor(online),
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
