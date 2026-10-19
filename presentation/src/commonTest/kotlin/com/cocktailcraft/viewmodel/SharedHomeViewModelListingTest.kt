package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCatalogRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Home's listing is one tracked load (latest request wins), selecting the
 * category already shown is a no-op, and the filter options load once.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedHomeViewModelListingTest : MainDispatcherTest() {

    /**
     * [FakeCatalogRepository] whose category refreshes take [delays] ms and
     * finish even when their caller is cancelled — like a request already on
     * the wire, or one shared with another caller.
     */
    private class SlowCatalog(
        private val delegate: FakeCatalogRepository,
        private val delays: Map<String, Long>
    ) : CocktailCatalogRepository by delegate {
        var listCalls = 0

        override suspend fun refreshCategory(category: String, force: Boolean): Result<Unit> =
            withContext(NonCancellable) {
                delay(delays[category] ?: 0)
                delegate.refreshCategory(category, force)
            }

        override suspend fun getCategories(): Result<List<String>> {
            listCalls++
            return delegate.getCategories()
        }

        override suspend fun getIngredients(): Result<List<String>> {
            listCalls++
            return delegate.getIngredients()
        }
    }

    private val shot = testCocktail("shot", category = "Shot")
    private val beer = testCocktail("beer", category = "Beer")
    private val cocktail = testCocktail("cocktail", category = "Cocktail")

    private fun HomeViewModelHarness.slowViewModel(delays: Map<String, Long>): SharedHomeViewModel {
        all = listOf(shot, beer, cocktail)
        return viewModel(SlowCatalog(catalog, delays))
    }

    private fun SharedHomeViewModel.ids(): List<String> = uiState.value.cocktails.map(Cocktail::id)

    @Test
    fun aSlowEarlierCategoryNeverOverwritesALaterOne() = runTest {
        val harness = HomeViewModelHarness()
        val vm = harness.slowViewModel(mapOf("Shot" to 2_000, "Beer" to 100))
        advanceUntilIdle()

        // Two chip taps: Shot's request is still out when Beer's comes back
        vm.selectCategory("Shot")
        vm.selectCategory("Beer")
        advanceTimeBy(500)
        assertEquals(listOf("beer"), vm.ids())

        advanceUntilIdle() // Shot's request lands after all

        assertEquals("Beer", vm.uiState.value.selectedCategory)
        assertEquals(listOf("beer"), vm.ids(), "the chip shows Beer, so the list must be Beer's")
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.error.value)
    }

    @Test
    fun aReplacedLoadDoesNotEndTheLoadThatReplacedIt() = runTest {
        val harness = HomeViewModelHarness()
        val vm = harness.slowViewModel(mapOf("Shot" to 100, "Beer" to 2_000))
        advanceUntilIdle()

        vm.selectCategory("Shot")
        vm.selectCategory("Beer")
        advanceTimeBy(500) // Shot's request is back; Beer's is still out

        assertTrue(vm.uiState.value.isLoading, "Beer is still loading")
        assertFalse("shot" in vm.ids(), "the replaced load must not publish")

        advanceUntilIdle()

        assertEquals(listOf("beer"), vm.ids())
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun concurrentCallersStillGetOnlyTheLatestCategory() = runTest {
        // iOS used to start an untracked Task per chip tap: overlapping
        // callers, each in its own coroutine
        val harness = HomeViewModelHarness()
        val vm = harness.slowViewModel(mapOf("Shot" to 2_000, "Beer" to 100))
        advanceUntilIdle()

        launch { vm.selectCategory("Shot") }
        advanceTimeBy(10)
        launch { vm.selectCategory("Beer") }
        advanceUntilIdle()

        assertEquals(listOf("beer"), vm.ids())
    }

    @Test
    fun reselectingTheShownCategoryDoesNotReload() = runTest {
        // A screen re-applying its selection on every return (Android) or
        // appearance (iOS) must not refetch, show the spinner or reset paging
        val harness = HomeViewModelHarness()
        harness.all = (1..15).map { testCocktail("c$it", category = "Shot") }
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.selectCategory("Shot")
        advanceUntilIdle()
        vm.loadMoreCocktails()
        val requests = harness.catalog.refreshCalls.toList()

        vm.selectCategory("Shot")
        assertFalse(vm.uiState.value.isLoading)
        advanceUntilIdle()

        assertEquals(requests, harness.catalog.refreshCalls)
        assertEquals(15, vm.uiState.value.cocktails.size, "paging is kept")
        assertEquals(2, vm.uiState.value.currentPage)
    }

    @Test
    fun theInitialLoadIsTheOnlyLoad() = runTest {
        // The ViewModel loads "All" itself; the screen then applying its
        // (default) selection must not start a second load
        val harness = HomeViewModelHarness()
        harness.all = listOf(cocktail)
        val vm = harness.viewModel()
        vm.selectCategory(null)
        advanceUntilIdle()
        vm.selectCategory(null)
        advanceUntilIdle()

        assertEquals(listOf("Cocktail" to false), harness.catalog.refreshCalls)
        assertEquals(listOf("cocktail"), vm.ids())
    }

    @Test
    fun reselectingUnderAnActiveSearchKeepsTheResults() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(testCocktail("1", name = "Margarita"), testCocktail("2", name = "Mojito"))
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.searchCocktails("marg")
        advanceUntilIdle()

        vm.selectCategory(null)
        advanceUntilIdle()

        assertEquals(listOf("1"), vm.ids())
    }

    @Test
    fun aFailedCategoryIsReloadedWhenSelectedAgain() = runTest {
        val harness = HomeViewModelHarness()
        harness.errorMessage = "api down"
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.ids().isEmpty())

        harness.errorMessage = null
        harness.all = listOf(cocktail)
        vm.selectCategory(null)
        advanceUntilIdle()

        assertEquals(listOf("cocktail"), vm.ids())
        assertNull(vm.error.value)
    }

    @Test
    fun filterOptionsLoadOnceHoweverOftenTheFilterUiOpens() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(shot, beer)
        val catalog = SlowCatalog(harness.catalog, emptyMap())
        val vm = harness.viewModel(catalog)
        advanceUntilIdle()
        assertEquals(0, catalog.listCalls, "nothing is fetched until the filter UI opens")

        vm.toggleAdvancedSearchMode(true)
        vm.loadFilterOptions()
        advanceUntilIdle()
        vm.toggleAdvancedSearchMode(false)
        vm.toggleAdvancedSearchMode(true)
        advanceUntilIdle()

        assertEquals(2, catalog.listCalls, "one categories and one ingredients request")
        assertEquals(listOf("Shot", "Beer"), vm.uiState.value.filterCategories)
    }
}
