package com.cocktailcraft.viewmodel

import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Home's "Recently viewed" row: contents, order and visibility rules. */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedHomeViewModelRecentlyViewedTest : MainDispatcherTest() {

    private val catalog = listOf(
        testCocktail("1", name = "Aperol Spritz"),
        testCocktail("2", name = "Mojito")
    )

    @Test
    fun recentlyViewedShowsTheLastTenNewestFirst() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = catalog
        harness.offline.recentlyViewed = (1..12).map { testCocktail("r$it") }
        val vm = harness.viewModel()
        advanceUntilIdle()

        assertEquals((1..10).map { "r$it" }, vm.uiState.value.recentlyViewed.map { it.id })
        assertTrue(vm.uiState.value.showRecentlyViewed)
    }

    @Test
    fun recentlyViewedIsHiddenWhenEmpty() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = catalog
        val vm = harness.viewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.recentlyViewed.isEmpty())
        assertFalse(vm.uiState.value.showRecentlyViewed)
    }

    @Test
    fun recentlyViewedIsHiddenWhileASearchQueryIsActive() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = catalog
        harness.offline.recentlyViewed = listOf(testCocktail("r1"))
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.updateSearchQuery("ape")
        assertFalse(vm.uiState.value.showRecentlyViewed, "hidden as soon as the query is typed")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.showRecentlyViewed)

        vm.clearSearch()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showRecentlyViewed, "back once the query is cleared")
    }

    @Test
    fun refreshingRecentlyViewedPicksUpACocktailViewedSince() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = catalog
        harness.offline.recentlyViewed = listOf(testCocktail("r1"))
        val vm = harness.viewModel()
        advanceUntilIdle()

        // The user opens a detail page and comes back to Home
        harness.offline.recentlyViewed = listOf(testCocktail("r2"), testCocktail("r1"))
        vm.refreshRecentlyViewed()
        advanceUntilIdle()

        assertEquals(listOf("r2", "r1"), vm.uiState.value.recentlyViewed.map { it.id })
    }
}
