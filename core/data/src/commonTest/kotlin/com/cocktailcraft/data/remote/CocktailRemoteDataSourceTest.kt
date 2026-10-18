package com.cocktailcraft.data.remote

import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.testutil.FakeCocktailApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CocktailRemoteDataSourceTest {

    private val interval = CocktailCacheManager.MIN_API_CALL_INTERVAL_MS

    private val drinks = listOf(
        CocktailDto(id = "1", name = "Margarita", category = "Cocktail", instructions = "Shake."),
        CocktailDto(id = "2", name = "Mojito", category = "Cocktail", instructions = "Muddle.")
    )

    /** Data source on the test scheduler's virtual clock. */
    private fun TestScope.remote(cacheManager: CocktailCacheManager = CocktailCacheManager()) =
        CocktailRemoteDataSource(FakeCocktailApi(drinks), cacheManager, nowMs = { testScheduler.currentTime })

    @Test
    fun concurrentCallsEachGetTheirOwnRateLimitSlot() = runTest {
        // The old check-then-record limiter let every concurrent caller read
        // the same "last call" time and fire together.
        val remote = remote()
        val callTimes = mutableListOf<Long>()

        repeat(3) {
            launch {
                remote.searchByName("a")
                callTimes += testScheduler.currentTime
            }
        }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(0L, interval, 2 * interval), callTimes)
    }

    @Test
    fun searchFilterAndLookupEndpointsShareOneLimiter() = runTest {
        val remote = remote()

        remote.filterByCategoryRaw("Cocktail")
        remote.searchByName("Margarita")
        remote.filterByIngredient("Tequila")
        remote.filterByAlcoholic(true)
        remote.getById("1")

        assertEquals(4 * interval, testScheduler.currentTime)
    }

    @Test
    fun callsFurtherApartThanTheIntervalAreNotDelayed() = runTest {
        val remote = remote()

        remote.searchByName("a")
        testScheduler.advanceTimeBy(interval * 5)
        val before = testScheduler.currentTime
        remote.searchByName("b")

        assertEquals(before, testScheduler.currentTime)
    }

    @Test
    fun listEndpointsAreNotThrottled() = runTest {
        val remote = remote()

        remote.searchByName("a")
        remote.getCategories()
        remote.getIngredients()
        remote.getRandom()

        assertEquals(0L, testScheduler.currentTime)
    }
}
