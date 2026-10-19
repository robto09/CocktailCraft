package com.cocktailcraft.data.repository

import app.cash.turbine.test
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.local.LegacySettingsImport
import com.cocktailcraft.data.local.OwnerIds
import com.cocktailcraft.data.remote.ApiRateLimiter
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.partialCocktail
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CocktailFavoritesRepositoryImplTest {

    private val config = AppConfigImpl()

    // Mirrors the DataModule wiring: remote source + focused offline repo
    // backing the favorites impl, all over one database.
    private fun TestScope.repository(
        api: FakeCocktailApi = FakeCocktailApi(),
        db: CocktailCraftDatabase = createTestDatabase(),
        settings: Settings = MapSettings(),
        online: Boolean = true,
        cache: CocktailCache? = null
    ): CocktailFavoritesRepositoryImpl {
        val policy = OfflineModePolicy(FakeNetworkMonitor(online))
        val cocktailCache = cache ?: CocktailCache(db, settings, testJson, config, offlineModePolicy = policy)
        val remote = CocktailRemoteDataSource(api, ApiRateLimiter())
        val offline = CocktailOfflineRepositoryImpl(
            offlineModePolicy = policy,
            cocktailCache = cocktailCache
        )
        return CocktailFavoritesRepositoryImpl(
            localDatabase = createTestLocalDatabase(db, settings),
            json = testJson,
            cocktailCache = cocktailCache,
            remote = remote,
            offlineRepository = offline,
            appScope = backgroundScope
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

    private fun names(result: Result<List<Cocktail>>) = (result as Result.Success).data.map { it.name }

    @Test
    fun favoritesRoundTripPersistsInTheDatabase() = runTest {
        val db = createTestDatabase()
        val repo = repository(db = db)
        val cocktail = testCocktail("11007")

        repo.addToFavorites(cocktail)
        assertTrue(repo.isCocktailFavorite("11007").getOrThrow())

        // Adding twice must not duplicate the entry
        repo.addToFavorites(cocktail)
        val stored = db.favoriteCocktailQueries.selectAll(OwnerIds.GUEST).executeAsList()
        assertEquals(listOf("11007"), stored.map { it.cocktail_id })

        // A fresh repository over the same database (an app relaunch) sees it
        assertEquals(listOf(cocktail), repository(db = db).getFavoriteCocktails().getOrThrow())

        repo.removeFromFavorites(cocktail)
        assertFalse(repo.isCocktailFavorite("11007").getOrThrow())
        assertTrue(repository(db = db).getFavoriteCocktails().getOrThrow().isEmpty())
    }

    @Test
    fun favoritesKeepTheOrderTheyWereAddedIn() = runTest {
        val repo = repository()
        listOf("3", "1", "2").forEach { repo.addToFavorites(testCocktail(it, name = "Drink $it")) }

        assertEquals(listOf("Drink 3", "Drink 1", "Drink 2"), names(repo.getFavoriteCocktails()))
    }

    @Test
    fun legacyFavoriteIdsResolveThroughApiWhenOnline() = runTest {
        // Favorites imported from the old id-only Settings list have no
        // stored cocktail; they resolve through the API and are then stored.
        val db = createTestDatabase()
        val settings = MapSettings().apply { putString(LegacySettingsImport.FAVORITES_KEY, "11007") }
        val repo = repository(api = FakeCocktailApi(drinks = listOf(margaritaDto())), db = db, settings = settings)

        assertEquals(listOf("Margarita"), names(repo.getFavoriteCocktails()))
        assertTrue(
            db.favoriteCocktailQueries.selectAll(OwnerIds.GUEST).executeAsOne().payload != null,
            "the resolved cocktail must be stored with the favorite"
        )
    }

    @Test
    fun legacyFavoriteIdsResolveFromTheOfflineCacheWhenOffline() = runTest {
        val db = createTestDatabase()
        val settings = MapSettings().apply { putString(LegacySettingsImport.FAVORITES_KEY, "42,7") }
        val policy = OfflineModePolicy(FakeNetworkMonitor(false))
        val cache = CocktailCache(db, settings, testJson, config, offlineModePolicy = policy)
        cache.cacheCocktail(testCocktail("42", name = "Negroni"))
        val repo = repository(db = db, settings = settings, online = false, cache = cache)

        // "7" is neither cached nor fetchable offline: it stays a favorite
        // but hidden until it can be resolved, as unresolvable ids were.
        assertEquals(listOf("Negroni"), names(repo.getFavoriteCocktails()))
        assertTrue(repo.isCocktailFavorite("7").getOrThrow())
    }

    @Test
    fun observeFavoritesResolvesLegacyIdsAndEmitsThem() = runTest {
        val settings = MapSettings().apply { putString(LegacySettingsImport.FAVORITES_KEY, "11007") }
        val repo = repository(api = FakeCocktailApi(drinks = listOf(margaritaDto())), settings = settings)

        repo.observeFavorites().test {
            var favorites = awaitItem()
            while (favorites.isEmpty()) favorites = awaitItem()
            assertEquals(listOf("Margarita"), favorites.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addToFavoritesUpgradesExistingEntryInPlaceWithoutDuplicating() = runTest {
        // Persist-then-hydrate: the shallow list object is stored first, the
        // hydrated copy re-added afterwards — that second add must replace the
        // entry in place, never duplicate or reorder it.
        val db = createTestDatabase()
        val repo = repository(db = db)

        repo.addToFavorites(partialCocktail("11007", name = "Shallow Margarita"))
        repo.addToFavorites(testCocktail("1", name = "Mojito"))
        repo.addToFavorites(testCocktail("11007", name = "Full Margarita"))

        assertEquals(listOf("Full Margarita", "Mojito"), names(repo.getFavoriteCocktails()))
        assertEquals(2, db.favoriteCocktailQueries.selectAll(OwnerIds.GUEST).executeAsList().size)
    }

    @Test
    fun favoritesStayAvailableOfflineWhateverTheCacheEvicts() = runTest {
        // A favorite pins its cocktail, so an offline favorite survives
        // the offline cache filling up past its cap
        val db = createTestDatabase()
        val policy = OfflineModePolicy(FakeNetworkMonitor(false))
        val oneRowCap = object : AppConfig by config {
            override val maxOfflineCocktails: Int = 1
        }
        val cache = CocktailCache(db, MapSettings(), testJson, oneRowCap, offlineModePolicy = policy)
        val repo = repository(api = FakeCocktailApi(), db = db, online = false, cache = cache)

        repo.addToFavorites(testCocktail("42", name = "Negroni"))
        cache.cacheCocktails((1..5).map { testCocktail("$it") })

        assertEquals(listOf("Negroni"), names(repo.getFavoriteCocktails()))
        assertEquals("Negroni", cache.getCachedCocktail("42")?.name)
    }

    @Test
    fun aDetailRefreshShowsUpInTheFavorites() = runTest {
        // One copy of each cocktail: refreshing it updates the favorite too
        val db = createTestDatabase()
        val cache = CocktailCache(db, MapSettings(), testJson, config)
        val repo = repository(db = db, cache = cache)
        repo.addToFavorites(testCocktail("42", name = "Negroni"))

        cache.cacheCocktail(testCocktail("42", name = "Negroni Sbagliato"))

        assertEquals(listOf("Negroni Sbagliato"), names(repo.getFavoriteCocktails()))
    }

    @Test
    fun addToFavoritesAlsoCachesTheCocktailForOfflineDetail() = runTest {
        val db = createTestDatabase()
        val repo = repository(db = db)

        repo.addToFavorites(testCocktail("42", name = "Negroni"))

        assertTrue(db.cachedCocktailQueries.exists("42").executeAsOne() > 0L)
    }

    @Test
    fun observeFavoritesEmitsAfterAddAndRemove() = runTest {
        val repo = repository()
        val negroni = testCocktail("42", name = "Negroni")

        repo.observeFavorites().test {
            assertEquals(emptyList(), awaitItem())

            repo.addToFavorites(negroni).getOrThrow()
            assertEquals(listOf("Negroni"), awaitItem().map { it.name })

            repo.removeFromFavorites(negroni).getOrThrow()
            assertEquals(emptyList(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun everyObserverSharesOneFavoritesStream() = runTest {
        // Home, Favorites and the detail screen observe at once: one query
        // and one decode per change for all of them, not one each
        val repo = repository()
        repo.addToFavorites(testCocktail("42", name = "Negroni")).getOrThrow()

        val first = repo.observeFavorites().first { it.isNotEmpty() }
        val second = repo.observeFavorites().first { it.isNotEmpty() }

        assertSame(first, second, "the second observer gets the shared stream's list")
    }

    @Test
    fun unknownFavoriteIsNotAFavorite() = runTest {
        assertFalse(repository().isCocktailFavorite("nope").getOrThrow())
        assertNull((repository().getFavoriteCocktails() as Result.Success).data.firstOrNull())
    }
}
