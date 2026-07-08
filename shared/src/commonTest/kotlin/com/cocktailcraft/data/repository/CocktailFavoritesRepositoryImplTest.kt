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

class CocktailFavoritesRepositoryImplTest {

    private val config = AppConfigImpl()

    // Mirrors the DataModule wiring: remote source + focused offline repo
    // backing the favorites impl.
    private fun repository(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): CocktailFavoritesRepositoryImpl {
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
        return CocktailFavoritesRepositoryImpl(
            settings = settings,
            appConfig = config,
            cocktailCache = cache,
            remote = remote,
            offlineRepository = offline
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
}
