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
        val policy = OfflineModePolicy(settings, config, FakeNetworkMonitor(online))
        val cache = CocktailCache(settings, Json { ignoreUnknownKeys = true }, config, offlineModePolicy = policy)
        val cacheManager = CocktailCacheManager()
        val remote = CocktailRemoteDataSource(api, cacheManager)
        val offline = CocktailOfflineRepositoryImpl(
            offlineModePolicy = policy,
            cocktailCache = cache,
            cacheManager = cacheManager,
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
    fun getFavoriteCocktailsResolvesUncachedIdsThroughApiWhenOnline() = runTest {
        // Ids persisted before SH-4's add-time caching (or from another
        // install) still resolve through the API.
        val settings = MapSettings()
        settings.putString(config.favoritesStorageKey, "11007")
        val repo = repository(
            api = FakeCocktailApi(drinks = listOf(margaritaDto())),
            settings = settings
        )

        val favorites = (repo.getFavoriteCocktails() as Result.Success).data

        assertEquals(listOf("Margarita"), favorites.map { it.name })
    }

    @Test
    fun addToFavoritesUpgradesExistingEntryInPlaceWithoutDuplicating() = runTest {
        // Persist-then-hydrate: the shallow list object is stored first, the
        // hydrated copy re-added afterwards — that second add must replace the
        // entry (id list and published object), never duplicate it.
        val settings = MapSettings()
        val repo = repository(settings = settings)

        repo.addToFavorites(testCocktail("11007", name = "Shallow Margarita"))
        repo.addToFavorites(testCocktail("11007", name = "Full Margarita"))

        val favorites = (repo.getFavoriteCocktails() as Result.Success).data
        assertEquals(listOf("Full Margarita"), favorites.map { it.name })
        assertEquals("11007", settings.getStringOrNull(config.favoritesStorageKey))
    }

    @Test
    fun addToFavoritesCachesTheObjectSoOfflineResolutionSurvives() = runTest {
        // SH-4: the full object is cached at add-time, so a favorited item no
        // longer silently vanishes when the device is offline.
        val repo = repository(api = FakeCocktailApi(), online = false)

        repo.addToFavorites(testCocktail("42", name = "Negroni"))
        val favorites = (repo.getFavoriteCocktails() as Result.Success).data

        assertEquals(listOf("Negroni"), favorites.map { it.name })
    }
}
