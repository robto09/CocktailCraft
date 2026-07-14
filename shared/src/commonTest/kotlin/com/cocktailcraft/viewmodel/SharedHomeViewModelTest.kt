package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.LoadCocktailsByCategoryUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.usecase.ManageOfflineModeUseCase
import com.cocktailcraft.domain.usecase.SearchCocktailsUseCase
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import com.cocktailcraft.testutil.FakeCatalogRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeFavoritesRepository
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.FakeOfflineRepository
import com.cocktailcraft.testutil.FakeSearchRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    /** All fakes behind a real use-case stack, so tests exercise VM + domain together. */
    private class Harness {
        val search = FakeSearchRepository()
        val catalog = FakeCatalogRepository()
        val detail = FakeDetailRepository()
        val favorites = FakeFavoritesRepository()
        val offline = FakeOfflineRepository()
        val network = FakeNetworkMonitor()

        fun viewModel() = SharedHomeViewModel(
            searchCocktailsUseCase = SearchCocktailsUseCase(search),
            loadCocktailsByCategoryUseCase = LoadCocktailsByCategoryUseCase(search),
            sortCocktailsUseCase = SortCocktailsUseCase(),
            manageFavoritesUseCase = ManageFavoritesUseCase(favorites, detail),
            manageOfflineModeUseCase = ManageOfflineModeUseCase(offline, catalog),
            getCocktailDetailUseCase = GetCocktailDetailUseCase(detail, favorites, search),
            catalogRepository = catalog,
            networkMonitor = network
        )
    }

    // --- SH-13: debounced search pipeline ---

    @Test
    fun typingBurstFiresOneDebouncedSearchForTheLastQuery() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
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
        val harness = Harness()
        harness.search.all = listOf(testCocktail("1", name = "Margarita", category = "Cocktail"))
        val vm = harness.viewModel()
        advanceUntilIdle()
        harness.search.advancedSearchCalls.clear()

        vm.updateSearchQuery("marg")
        vm.clearSearch() // user hits clear before the debounce window elapses
        advanceUntilIdle()

        // Only the immediate clear-path search ran; the pending "marg" never fired.
        assertTrue(harness.search.advancedSearchCalls.none { it.query == "marg" },
            "clearing must cancel the pending debounced search")
    }

    @Test
    fun emptyQueryAfterTypingFallsBackToCategoryListing() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
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

        assertEquals(listOf("1"), vm.uiState.value.cocktails.map { it.id },
            "empty query falls back to the default category listing")
        assertEquals("", vm.uiState.value.searchQuery)
    }

    @Test
    fun networkRestoreRetriesFailedLoadAndRecovers() = runTest {
        // SH-12 (Turbine): the init-block network observer must auto-retry a
        // failed load once connectivity is restored, emitting the recovered list.
        val harness = Harness()
        harness.search.errorMessage = "api down"
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.cocktails.isEmpty())
        assertNotNull(vm.error.value, "the failed initial load must surface an error")

        // Backend recovers while the device is briefly offline; the restore
        // handler must auto-retry and refill the list.
        harness.search.errorMessage = null
        harness.search.all = listOf(testCocktail("1", category = "Cocktail"))
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
        // SH-4: a mutation made directly against the repository (e.g. from the
        // Detail screen's separate ViewModel) must reach this VM's state
        // without any manual loadFavorites() re-pull.
        val harness = Harness()
        val cocktail = testCocktail("7")
        harness.search.all = listOf(cocktail)
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertFalse(vm.isFavorite("7"))

        harness.favorites.addToFavorites(cocktail)
        advanceUntilIdle()

        assertTrue(vm.isFavorite("7"), "repository mutations must propagate reactively")
    }

    @Test
    fun getCocktailsByCategoryIsStableAcrossCalls() = runTest {
        // SH-10: shuffled() returned a different list every call, causing
        // pointless recomposition on Android and unstable results on iOS.
        val harness = Harness()
        harness.search.all = (1..8).map { testCocktail("id$it", category = "Cocktail") }
        val vm = harness.viewModel()
        advanceUntilIdle()

        val first = vm.getCocktailsByCategory("Cocktail", 3)
        val second = vm.getCocktailsByCategory("Cocktail", 3)

        assertEquals(first.map { it.id }, second.map { it.id })
        assertEquals(3, first.size)
    }

    @Test
    fun viewModelNeverStartsOrStopsTheSharedNetworkMonitor() = runTest {
        // SH-2: monitoring lifecycle belongs to app init; a second start from a
        // ViewModel double-registers the Android NetworkCallback and throws.
        val harness = Harness()
        harness.viewModel()
        advanceUntilIdle()

        assertEquals(0, harness.network.startCalls)
        assertEquals(0, harness.network.stopCalls)
    }

    @Test
    fun initLoadsDefaultCategoryAndFavorites() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
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
        val harness = Harness()
        val shots = (1..12).map { testCocktail("shot$it", category = "Shot") }
        val defaults = (1..12).map { testCocktail("cocktail$it", category = "Cocktail") }
        harness.search.all = shots + defaults
        // Mirrors the real repository, whose newest-sorted list only holds the default category.
        harness.catalog.all = defaults
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.loadCocktailsByCategory("Shot")
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
    fun loadMoreFailureSurfacesRetryableErrorInsteadOfEndOfList() = runTest {
        // A transient failure while fetching page 2 must surface an error with
        // a Retry action — not silently flip hasMoreData off, which is
        // indistinguishable from genuinely reaching the end of the list.
        val harness = Harness()
        harness.search.all = (1..12).map { testCocktail("c$it", category = "Cocktail") }
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertEquals(10, vm.uiState.value.cocktails.size)
        assertTrue(vm.uiState.value.hasMoreData)

        harness.search.errorMessage = "api down"
        vm.loadMoreCocktails()

        assertNotNull(vm.error.value, "a failed page fetch must surface an error")
        assertNotNull(vm.error.value?.recoveryAction, "the error must offer a retry")
        assertTrue(vm.uiState.value.hasMoreData, "a failure must not masquerade as end of list")
        assertFalse(vm.uiState.value.isLoadingMore)
        assertEquals(10, vm.uiState.value.cocktails.size, "the loaded list must be untouched")

        // Backend recovers: the retry path completes the pagination.
        harness.search.errorMessage = null
        vm.loadMoreCocktails()

        assertEquals(12, vm.uiState.value.cocktails.size)
        assertFalse(vm.uiState.value.hasMoreData)
        assertNull(vm.error.value, "a successful retry must clear the error")
    }

    @Test
    fun searchDisablesCategoryPaginationAndLoadMoreIsANoOp() = runTest {
        // The default category load leaves hasMoreData = true; a search must
        // clear it (results are complete, not paginated) and loadMoreCocktails
        // must never splice category pages into search results.
        val harness = Harness()
        harness.search.all =
            (1..12).map { testCocktail("c$it", name = "Cocktail $it", category = "Cocktail") } +
            testCocktail("m1", name = "Margarita", category = "Ordinary Drink")
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasMoreData, "12-item category must page")

        vm.searchCocktails("marg")

        assertFalse(vm.uiState.value.hasMoreData, "search results are complete — no more pages")

        vm.loadMoreCocktails()

        assertEquals(listOf("m1"), vm.uiState.value.cocktails.map { it.id },
            "load-more during search must not append category items")

        // Clearing the search restores the paginated category listing.
        vm.searchCocktails("")
        assertTrue(vm.uiState.value.hasMoreData)
    }

    @Test
    fun searchFiltersByNameAndUpdatesQueryState() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
            testCocktail("1", name = "Margarita"),
            testCocktail("2", name = "Mojito")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.searchCocktails("marg")

        assertEquals("marg", vm.uiState.value.searchQuery)
        assertEquals(listOf("Margarita"), vm.uiState.value.cocktails.map { it.name })
    }

    @Test
    fun blankSearchReloadsSelectedCategory() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
            testCocktail("1", name = "Margarita"),
            testCocktail("2", name = "Mojito")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.searchCocktails("marg")

        vm.searchCocktails("")

        assertEquals(2, vm.uiState.value.cocktails.size)
    }

    @Test
    fun networkLossAutoEnablesOfflineMode() = runTest {
        val harness = Harness()
        harness.search.all = listOf(testCocktail("1"))
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isNetworkAvailable)
        assertFalse(vm.uiState.value.isOfflineMode)

        harness.network.setOnline(false)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isNetworkAvailable)
        assertTrue(vm.uiState.value.isOfflineMode)
        assertTrue(harness.offline.offlineModeEnabled, "offline preference must be persisted")
    }

    @Test
    fun sortByPriceReordersCurrentList() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
            testCocktail("1", price = 30.0),
            testCocktail("2", price = 10.0),
            testCocktail("3", price = 20.0)
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.sortByPrice(ascending = true)
        assertEquals(listOf("2", "3", "1"), vm.uiState.value.cocktails.map { it.id })

        vm.sortByPrice(ascending = false)
        assertEquals(listOf("1", "3", "2"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun toggleFavoriteRefreshesFavoritesState() = runTest {
        val harness = Harness()
        val cocktail = testCocktail("1")
        harness.search.all = listOf(cocktail)
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.toggleFavorite(cocktail)
        advanceUntilIdle() // state arrives via the observed favorites flow (SH-4)
        assertTrue(vm.isFavorite("1"))

        vm.toggleFavorite(cocktail)
        advanceUntilIdle()
        assertFalse(vm.isFavorite("1"))
    }

    @Test
    fun applyFiltersPopulatesSearchFiltersAndCocktails() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
            testCocktail("1", name = "Margarita", category = "Ordinary Drink"),
            testCocktail("2", name = "Mojito", category = "Cocktail"),
            testCocktail("3", name = "Daiquiri", category = "Cocktail")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.applyFilters(SearchFilters(category = "Cocktail"))

        assertEquals(SearchFilters(category = "Cocktail"), vm.uiState.value.searchFilters)
        assertEquals(listOf("2", "3"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun clearSearchFiltersResetsFiltersAndReloadsDefault() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
            testCocktail("1", category = "Cocktail"),
            testCocktail("2", category = "Cocktail"),
            testCocktail("3", category = "Shot")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.applyFilters(SearchFilters(category = "Shot"))
        assertEquals(listOf("3"), vm.uiState.value.cocktails.map { it.id })

        vm.clearSearchFilters()
        advanceUntilIdle()

        assertEquals(SearchFilters(), vm.uiState.value.searchFilters)
        assertEquals("", vm.uiState.value.searchQuery)
        assertEquals(listOf("1", "2"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun searchComposesWithActiveFilters() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
            testCocktail("1", name = "Mojito", category = "Cocktail"),
            testCocktail("2", name = "Mojito Especial", category = "Ordinary Drink"),
            testCocktail("3", name = "Daiquiri", category = "Cocktail")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.applyFilters(SearchFilters(category = "Cocktail"))
        vm.searchCocktails("mojito")

        assertEquals("mojito", vm.uiState.value.searchQuery)
        assertEquals(SearchFilters(query = "mojito", category = "Cocktail"), vm.uiState.value.searchFilters)
        assertEquals(listOf("1"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun getCategoriesReturnsDistinctSortedCategories() = runTest {
        val harness = Harness()
        harness.search.all = listOf(
            testCocktail("1", category = "Cocktail"),
            testCocktail("2", category = "Cocktail")
        )
        val vm = harness.viewModel()
        advanceUntilIdle()

        assertEquals(listOf("Cocktail"), vm.getCategories())
    }
}
