package com.cocktailcraft.data.remote

import com.cocktailcraft.testutil.FakeCocktailApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CocktailRemoteDataSourceTest {

    private val interval = ApiRateLimiter.MIN_API_CALL_INTERVAL_MS

    private val drinks = listOf(
        CocktailDto(id = "1", name = "Margarita", category = "Cocktail", instructions = "Shake."),
        CocktailDto(id = "2", name = "Mojito", category = "Cocktail", instructions = "Muddle.")
    )

    /** Data source on the test scheduler's virtual clock. */
    private fun TestScope.remote(rateLimiter: ApiRateLimiter = ApiRateLimiter()) =
        CocktailRemoteDataSource(FakeCocktailApi(drinks), rateLimiter, nowMs = { testScheduler.currentTime })

    @Test
    fun concurrentCallsEachGetTheirOwnRateLimitSlot() = runTest {
        // The old check-then-record limiter let every concurrent caller read
        // the same "last call" time and fire together.
        val remote = remote()
        val callTimes = mutableListOf<Long>()

        repeat(3) {
            launch {
                remote.filterByIngredient("Gin")
                callTimes += testScheduler.currentTime
            }
        }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(0L, interval, 2 * interval), callTimes)
    }

    @Test
    fun filterEndpointsShareOneLimiter() = runTest {
        val remote = remote()

        remote.filterByCategoryRaw("Cocktail")
        remote.filterByIngredient("Tequila")
        remote.filterByAlcoholic(true)

        assertEquals(2 * interval, testScheduler.currentTime)
    }

    @Test
    fun callsFurtherApartThanTheIntervalAreNotDelayed() = runTest {
        val remote = remote()

        remote.filterByIngredient("Gin")
        testScheduler.advanceTimeBy(interval * 5)
        val before = testScheduler.currentTime
        remote.filterByIngredient("Rum")

        assertEquals(before, testScheduler.currentTime)
    }

    @Test
    fun userInitiatedAndListEndpointsAreNotThrottled() = runTest {
        // Name search (already debounced) and opening a cocktail must never
        // wait behind a category page's slot.
        val remote = remote()

        remote.filterByCategoryRaw("Cocktail")
        remote.searchByName("a")
        remote.getById("1")
        remote.searchByName("b")
        remote.getCategories()
        remote.getIngredients()
        remote.getRandom()

        assertEquals(0L, testScheduler.currentTime)
    }
}
