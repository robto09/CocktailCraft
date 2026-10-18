package com.cocktailcraft.widget

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeFavoritesRepository
import com.cocktailcraft.testutil.FakeOfflineRepository
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WidgetDataSourceTest {

    private val live = testCocktail("11007", name = "Margarita")
    private val viewed = testCocktail("11000", name = "Mojito")
    private val alsoViewed = testCocktail("17222", name = "A1")

    private val detail = FakeDetailRepository()
    private val offline = FakeOfflineRepository().apply { recentlyViewed = listOf(viewed, alsoViewed) }
    private val favorites = FakeFavoritesRepository()

    /** API random calls so far */
    private var randomCalls = 0

    private fun source(
        random: suspend () -> Result<Cocktail?> = { Result.Success(live) },
        favoritesRepository: CocktailFavoritesRepository = favorites
    ): WidgetDataSource {
        val api = object : CocktailDetailRepository by detail {
            override suspend fun getRandomCocktail(): Result<Cocktail?> {
                randomCalls++
                return random()
            }
        }
        // Deterministic pick: the first stored cocktail
        return WidgetDataSource(api, offline, favoritesRepository, pickStored = { it.first() })
    }

    @Test
    fun theRandomPoolIsAFreshPickThenTheRecentlyViewed() = runTest {
        assertEquals(listOf(live, viewed, alsoViewed), source().randomCocktailPool(limit = 5))
        assertEquals(listOf(live, viewed), source().randomCocktailPool(limit = 2))
    }

    @Test
    fun aFreshPickAlsoRecentlyViewedIsListedOnce() = runTest {
        assertEquals(listOf(viewed, alsoViewed), source(random = { Result.Success(viewed) }).randomCocktailPool(5))
    }

    @Test
    fun offlineTheApiIsNotTriedAndThePoolIsTheStoredCocktails() = runTest {
        offline.offline = true

        assertEquals(listOf(viewed, alsoViewed), source().randomCocktailPool(5))
        assertEquals(viewed, source().randomCocktail())
        assertEquals(0, randomCalls)
    }

    @Test
    fun aFailedOrEmptyFetchIsSkipped() = runTest {
        val failing = source(random = { Result.Error("down", ErrorCode.NETWORK) })
        val empty = source(random = { Result.Success(null) })
        val throwing = source(random = { throw IllegalStateException("boom") })

        listOf(failing, empty, throwing).forEach { source ->
            assertEquals(listOf(viewed, alsoViewed), source.randomCocktailPool(5))
            assertEquals(viewed, source.randomCocktail())
        }
    }

    @Test
    fun oneRandomCocktailIsTheFreshPickWhenThereIsOne() = runTest {
        assertEquals(live, source().randomCocktail())
    }

    @Test
    fun nothingFetchedOrStoredIsNull() = runTest {
        offline.recentlyViewed = emptyList()
        val source = source(random = { Result.Success(null) })

        assertNull(source.randomCocktail())
        assertTrue(source.randomCocktailPool(5).isEmpty())
    }

    @Test
    fun cancellationIsNeverSwallowed() = runTest {
        assertFailsWith<CancellationException> {
            source(random = { throw CancellationException("stopped") }).randomCocktail()
        }
    }

    @Test
    fun favoritesAreReturnedAsStoredEmptyIncluded() = runTest {
        assertEquals(emptyList(), source().favoriteCocktails())

        favorites.addToFavorites(live)
        assertEquals(listOf(live), source().favoriteCocktails())
    }

    @Test
    fun aFailedFavoritesReadIsNullNotAnEmptyList() = runTest {
        val failing = object : CocktailFavoritesRepository by favorites {
            override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> =
                Result.Error("db", ErrorCode.UNKNOWN)
        }

        assertNull(source(favoritesRepository = failing).favoriteCocktails())
    }
}
