package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.viewmodel.state.HomeSortOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SharedHomeViewModelTest : MainDispatcherTest() {

    // --- Debounced search pipeline ---

    @Test
    fun typingBurstFiresOneDebouncedSearchForTheLastQuery() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", name = "Margarita"),
            testCocktail("2", name = "Mojito")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()
        harness.search.advancedSearchCalls.clear()

        vm.updateSearchQuery("m")
        vm.updateSearchQuery("ma")
        vm.updateSearchQuery("mar")
        advanceUntilIdle()

        assertEquals(
            listOf("mar"),
            harness.search.advancedSearchCalls.map { it.query },
            "a typing burst must fire exactly one search, for the final query"
        )
        assertEquals(listOf("1"), vm.uiState.value.cocktails.map { it.id })
        assertEquals("mar", vm.uiState.value.searchQuery)
    }

    @Test
    fun clearSearchCancelsPendingDebouncedSearch() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(testCocktail("1", name = "Margarita", category = "Cocktail"))
        val vm = harness.viewModel()
        advanceUntilIdle()
        harness.search.advancedSearchCalls.clear()

        vm.updateSearchQuery("marg")
        vm.clearSearch() // user hits clear before the debounce window elapses
        advanceUntilIdle()

        // Only the immediate clear-path search ran; the pending "marg" never fired.
        assertTrue(
            harness.search.advancedSearchCalls.none { it.query == "marg" },
            "clearing must cancel the pending debounced search"
        )
    }

    @Test
    fun emptyQueryAfterTypingFallsBackToCategoryListing() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", name = "Margarita", category = "Cocktail"),
            testCocktail("2", name = "Beer", category = "Beer")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.updateSearchQuery("marg")
        advanceUntilIdle()
        assertEquals(listOf("1"), vm.uiState.value.cocktails.map { it.id })

        // Deleting back to empty must clear stale results, not leave them.
        vm.updateSearchQuery("")
        advanceUntilIdle()

        assertEquals(
            listOf("1"),
            vm.uiState.value.cocktails.map { it.id },
            "empty query falls back to the default category listing"
        )
        assertEquals("", vm.uiState.value.searchQuery)
    }

    @Test
    fun networkRestoreRetriesFailedLoadAndRecovers() = runTest {
        // Turbine: the init-block network observer must auto-retry a
        // failed load once connectivity is restored, emitting the recovered list.
        val harness = HomeViewModelHarness()
        harness.errorMessage = "api down"
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.cocktails.isEmpty())
        assertNotNull(vm.error.value, "the failed initial load must surface an error")

        // Backend recovers while the device is briefly offline; the restore
        // handler must auto-retry and refill the list.
        harness.errorMessage = null
        harness.all = listOf(testCocktail("1", category = "Cocktail"))
        harness.network.setOnline(false)
        advanceUntilIdle()

        vm.uiState.test {
            harness.network.setOnline(true)
            var state = awaitItem()
            while (state.cocktails.isEmpty()) state = awaitItem()
            assertEquals(listOf("1"), state.cocktails.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(vm.error.value, "a successful retry must clear the error")
    }

    @Test
    fun favoritesToggledElsewherePropagateThroughTheObservedFlow() = runTest {
        // A mutation made directly against the repository (e.g. from the
        // Detail screen's separate ViewModel) must reach this VM's state
        // without any manual loadFavorites() re-pull.
        val harness = HomeViewModelHarness()
        val cocktail = testCocktail("7")
        harness.all = listOf(cocktail)
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertFalse(vm.isFavorite("7"))

        harness.favorites.addToFavorites(cocktail)
        advanceUntilIdle()

        assertTrue(vm.isFavorite("7"), "repository mutations must propagate reactively")
    }

    @Test
    fun viewModelNeverStartsOrStopsTheSharedNetworkMonitor() = runTest {
        // Monitoring lifecycle belongs to app init; a second start from a
        // ViewModel double-registers the Android NetworkCallback and throws.
        val harness = HomeViewModelHarness()
        harness.viewModel()
        advanceUntilIdle()

        assertEquals(0, harness.network.startCalls)
        assertEquals(0, harness.network.stopCalls)
    }

    @Test
    fun initLoadsDefaultCategoryAndFavorites() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", category = "Cocktail"),
            testCocktail("2", category = "Cocktail"),
            testCocktail("3", category = "Shot")
        )
        harness.favorites.addToFavorites(testCocktail("2"))

        val vm = harness.viewModel()
        advanceUntilIdle()

        assertEquals(listOf("1", "2"), vm.uiState.value.cocktails.map { it.id })
        assertEquals(1, vm.uiState.value.favorites.size)
        assertTrue(vm.isFavorite("2"))
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun loadMorePagesWithinSelectedCategory() = runTest {
        val harness = HomeViewModelHarness()
        val shots = (1..12).map { testCocktail("shot$it", category = "Shot") }
        val defaults = (1..12).map { testCocktail("cocktail$it", category = "Cocktail") }
        harness.all = shots + defaults
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.selectCategory("Shot")
        advanceUntilIdle()
        assertEquals(10, vm.uiState.value.cocktails.size)
        assertTrue(vm.uiState.value.hasMoreData)

        vm.loadMoreCocktails()

        assertEquals(
            shots.map { it.id },
            vm.uiState.value.cocktails.map { it.id },
            "page 2 must come from the selected category"
        )
        assertEquals(2, vm.uiState.value.currentPage)
        assertFalse(vm.uiState.value.hasMoreData)
    }

    @Test
    fun loadMorePagesTheStoredPageWhenTheNetworkFails() = runTest {
        // Page 2 comes from the stored category page; a failing refresh
        // doesn't stop it.
        val harness = HomeViewModelHarness()
        harness.all = (1..12).map { testCocktail("c$it", category = "Cocktail") }
        val vm = harness.viewModel()
        advanceUntilIdle()

        harness.errorMessage = "api down"
        vm.loadMoreCocktails()

        assertEquals(12, vm.uiState.value.cocktails.size)
        assertFalse(vm.uiState.value.hasMoreData)
        assertNull(vm.error.value)
    }

    @Test
    fun loadMoreShowsMoreOfTheObservedPageWithoutARequest() = runTest {
        // The whole category is stored and observed: paging just shows more
        // of it, instead of re-running the load (and its refresh) per page.
        val harness = HomeViewModelHarness()
        harness.all = (1..25).map { testCocktail("c$it", category = "Cocktail") }
        val vm = harness.viewModel()
        advanceUntilIdle()
        val requests = harness.catalog.refreshCalls.toList()

        vm.loadMoreCocktails()
        assertEquals(20, vm.uiState.value.cocktails.size)
        assertTrue(vm.uiState.value.hasMoreData)

        vm.loadMoreCocktails()
        assertEquals(25, vm.uiState.value.cocktails.size)
        assertFalse(vm.uiState.value.hasMoreData)

        advanceUntilIdle()
        assertEquals(requests, harness.catalog.refreshCalls, "paging must not refetch the category")
        assertEquals((1..25).map { "c$it" }, vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun searchDisablesCategoryPaginationAndLoadMoreIsANoOp() = runTest {
        // The default category load leaves hasMoreData = true; a search must
        // clear it (results are complete, not paginated) and loadMoreCocktails
        // must never splice category pages into search results.
        val harness = HomeViewModelHarness()
        harness.all =
            (1..12).map { testCocktail("c$it", name = "Cocktail $it", category = "Cocktail") } +
            testCocktail("m1", name = "Margarita", category = "Ordinary Drink")
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasMoreData, "12-item category must page")

        vm.searchCocktails("marg")
        advanceUntilIdle()

        assertFalse(vm.uiState.value.hasMoreData, "search results are complete — no more pages")

        vm.loadMoreCocktails()

        assertEquals(
            listOf("m1"),
            vm.uiState.value.cocktails.map { it.id },
            "load-more during search must not append category items"
        )

        // Clearing the search restores the paginated category listing.
        vm.searchCocktails("")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasMoreData)
    }

    @Test
    fun searchFiltersByNameAndUpdatesQueryState() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", name = "Margarita"),
            testCocktail("2", name = "Mojito")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.searchCocktails("marg")
        advanceUntilIdle()

        assertEquals("marg", vm.uiState.value.searchQuery)
        assertEquals(listOf("Margarita"), vm.uiState.value.cocktails.map { it.name })
    }

    @Test
    fun blankSearchReloadsSelectedCategory() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", name = "Margarita"),
            testCocktail("2", name = "Mojito")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.searchCocktails("marg")
        advanceUntilIdle()

        vm.searchCocktails("")
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.cocktails.size)
    }

    @Test
    fun networkLossKeepsLoadedListAndPagesFromTheStoredCategory() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = (1..12).map { testCocktail("c$it", category = "Cocktail") }
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isNetworkAvailable)
        assertEquals(10, vm.uiState.value.cocktails.size)
        assertTrue(vm.uiState.value.hasMoreData)

        harness.network.setOnline(false)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isNetworkAvailable)

        // The next page is already stored: it shows offline too, no error
        vm.loadMoreCocktails()

        assertEquals(12, vm.uiState.value.cocktails.size)
        assertFalse(vm.uiState.value.hasMoreData)
        assertNull(vm.error.value, "offline load-more must not surface an error")
    }

    @Test
    fun networkRestoreAfterOfflineLoadRefetchesFreshData() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(testCocktail("cached", category = "Cocktail"))
        harness.network.setOnline(false)
        val vm = harness.viewModel()
        advanceUntilIdle()

        // Loaded while offline: content came from the cache-backed fallback.
        assertFalse(vm.uiState.value.isNetworkAvailable)
        assertEquals(listOf("cached"), vm.uiState.value.cocktails.map { it.id })

        // Fresh content is available once connectivity returns.
        harness.all = listOf(
            testCocktail("fresh1", category = "Cocktail"),
            testCocktail("fresh2", category = "Cocktail")
        )
        harness.network.setOnline(true)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isNetworkAvailable)
        assertEquals(
            setOf("fresh1", "fresh2"),
            vm.uiState.value.cocktails.map { it.id }.toSet(),
            "a restore after an offline load must refresh the stale cached list"
        )
    }

    @Test
    fun networkRestoreDuringInFlightOfflineLoadStillRefreshes() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(testCocktail("cached", category = "Cocktail"))
        harness.catalog.refreshDelayMs = 1_000
        harness.network.setOnline(false)
        val vm = harness.viewModel()
        advanceTimeBy(500) // initial offline load is suspended mid-flight

        // Connectivity returns before the offline load publishes its snapshot.
        harness.all = listOf(
            testCocktail("fresh1", category = "Cocktail"),
            testCocktail("fresh2", category = "Cocktail")
        )
        harness.network.setOnline(true)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isNetworkAvailable)
        assertEquals(
            setOf("fresh1", "fresh2"),
            vm.uiState.value.cocktails.map { it.id }.toSet(),
            "a restore during an in-flight offline load must still refresh the stale snapshot"
        )
    }

    @Test
    fun sortByPriceReordersCurrentList() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", price = Money(3000)),
            testCocktail("2", price = Money(1000)),
            testCocktail("3", price = Money(2000))
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setSortOption(HomeSortOption.PRICE_ASC)
        assertEquals(listOf("2", "3", "1"), vm.uiState.value.cocktails.map { it.id })

        vm.setSortOption(HomeSortOption.PRICE_DESC)
        assertEquals(listOf("1", "3", "2"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun toggleFavoriteRefreshesFavoritesState() = runTest {
        val harness = HomeViewModelHarness()
        val cocktail = testCocktail("1")
        harness.all = listOf(cocktail)
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.toggleFavorite(cocktail)
        advanceUntilIdle() // state arrives via the observed favorites flow
        assertTrue(vm.isFavorite("1"))

        vm.toggleFavorite(cocktail)
        advanceUntilIdle()
        assertFalse(vm.isFavorite("1"))
    }

    @Test
    fun applyFiltersPopulatesSearchFiltersAndCocktails() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", name = "Margarita", category = "Ordinary Drink"),
            testCocktail("2", name = "Mojito", category = "Cocktail"),
            testCocktail("3", name = "Daiquiri", category = "Cocktail")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.applyFilters(SearchFilters(category = "Cocktail"))
        advanceUntilIdle()

        assertEquals(SearchFilters(category = "Cocktail"), vm.uiState.value.searchFilters)
        assertEquals(listOf("2", "3"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun clearSearchFiltersResetsFiltersAndReloadsDefault() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", category = "Cocktail"),
            testCocktail("2", category = "Cocktail"),
            testCocktail("3", category = "Shot")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.applyFilters(SearchFilters(category = "Shot"))
        advanceUntilIdle()
        assertEquals(listOf("3"), vm.uiState.value.cocktails.map { it.id })

        vm.clearSearchFilters()
        advanceUntilIdle()

        assertEquals(SearchFilters(), vm.uiState.value.searchFilters)
        assertEquals("", vm.uiState.value.searchQuery)
        assertEquals(listOf("1", "2"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun searchComposesWithActiveFilters() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", name = "Mojito", category = "Cocktail"),
            testCocktail("2", name = "Mojito Especial", category = "Ordinary Drink"),
            testCocktail("3", name = "Daiquiri", category = "Cocktail")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.applyFilters(SearchFilters(category = "Cocktail"))
        advanceUntilIdle()
        vm.searchCocktails("mojito")
        advanceUntilIdle()

        assertEquals("mojito", vm.uiState.value.searchQuery)
        assertEquals(SearchFilters(query = "mojito", category = "Cocktail"), vm.uiState.value.searchFilters)
        assertEquals(listOf("1"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun toggleFavoriteFailureIsReportedNotDropped() = runTest {
        val harness = HomeViewModelHarness()
        val cocktail = testCocktail("1")
        harness.all = listOf(cocktail)
        val vm = harness.viewModel()
        advanceUntilIdle()

        harness.favorites.failingMutations = 1
        vm.toggleFavorite(cocktail)
        advanceUntilIdle()

        assertNotNull(vm.error.value, "a Result.Error from toggle must reach the error channel")
        assertFalse(vm.isFavorite("1"))
        assertEquals(listOf("1"), vm.uiState.value.cocktails.map { it.id }, "the list stays on screen")
    }

    @Test
    fun filterFailureThenSuccessfulRetryClearsTheError() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(testCocktail("1", name = "Margarita"))
        val vm = harness.viewModel()
        advanceUntilIdle()

        harness.errorMessage = "api down"
        vm.applyFilters(SearchFilters(query = "marg"))
        advanceUntilIdle()
        assertNotNull(vm.error.value)

        harness.errorMessage = null
        vm.applyFilters(SearchFilters(query = "marg"))
        advanceUntilIdle()

        assertNull(vm.error.value, "the next attempt must clear the earlier failure")
        assertEquals(listOf("1"), vm.uiState.value.cocktails.map { it.id })
    }

    // --- Database-backed category page: stale-while-revalidate ---

    @Test
    fun storedPageShowsWhileTheRefreshIsInFlight() = runTest {
        val harness = HomeViewModelHarness()
        harness.catalog.store("Cocktail", listOf(testCocktail("cached")))
        harness.all = listOf(testCocktail("fresh"))
        harness.catalog.refreshDelayMs = 1_000
        val vm = harness.viewModel()
        advanceTimeBy(500)

        assertEquals(listOf("cached"), vm.uiState.value.cocktails.map { it.id }, "cached rows show right away")
        assertTrue(vm.uiState.value.isLoading, "the refresh is still running")

        advanceUntilIdle()

        assertEquals(listOf("fresh"), vm.uiState.value.cocktails.map { it.id }, "the refresh lands through the flow")
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun failedRefreshKeepsTheStoredPageWithoutAnError() = runTest {
        val harness = HomeViewModelHarness()
        harness.catalog.store("Cocktail", listOf(testCocktail("cached")))
        harness.errorMessage = "api down"

        val vm = harness.viewModel()
        advanceUntilIdle()

        assertEquals(listOf("cached"), vm.uiState.value.cocktails.map { it.id })
        assertNull(vm.error.value, "with content on screen a failed refresh is not an error state")
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun storedChangesReachTheListAndKeepTheSortOrder() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", price = Money(3000)),
            testCocktail("2", price = Money(1000))
        )
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.setSortOption(HomeSortOption.PRICE_ASC)
        assertEquals(listOf("2", "1"), vm.uiState.value.cocktails.map { it.id })

        // e.g. a detail lookup upgrading id 1 in the database
        harness.catalog.store(
            "Cocktail",
            listOf(
                testCocktail("1", name = "Upgraded", price = Money(3000)),
                testCocktail("2", price = Money(1000))
            )
        )
        advanceUntilIdle()

        assertEquals(listOf("2", "1"), vm.uiState.value.cocktails.map { it.id }, "same cocktails: keep the sort")
        assertEquals("Upgraded", vm.uiState.value.cocktails.last().name)
    }

    @Test
    fun retryForcesARefreshWhileLoadsOnlyRefreshWhenStale() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(testCocktail("1"))
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.retry()
        advanceUntilIdle()

        assertEquals(listOf("Cocktail" to false, "Cocktail" to true), harness.catalog.refreshCalls)
    }

    @Test
    fun storedPageChangesDoNotOverwriteSearchResults() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = listOf(
            testCocktail("1", name = "Margarita"),
            testCocktail("2", name = "Mojito")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.searchCocktails("marg")
        advanceUntilIdle()
        harness.catalog.store("Cocktail", listOf(testCocktail("3")))
        advanceUntilIdle()

        assertEquals(listOf("1"), vm.uiState.value.cocktails.map { it.id })
    }

    private fun SharedHomeViewModel.isFavorite(cocktailId: String): Boolean =
        uiState.value.favorites.any { it.id == cocktailId }
}
