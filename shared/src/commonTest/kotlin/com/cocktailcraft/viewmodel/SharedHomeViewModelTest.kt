package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.usecase.FilterCocktailsUseCase
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
            loadCocktailsByCategoryUseCase = LoadCocktailsByCategoryUseCase(search, catalog),
            sortCocktailsUseCase = SortCocktailsUseCase(),
            filterCocktailsUseCase = FilterCocktailsUseCase(catalog),
            manageFavoritesUseCase = ManageFavoritesUseCase(favorites, detail),
            manageOfflineModeUseCase = ManageOfflineModeUseCase(offline, catalog),
            getCocktailDetailUseCase = GetCocktailDetailUseCase(detail, favorites, search),
            networkMonitor = network
        )
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
        assertTrue(vm.isFavorite("1"))

        vm.toggleFavorite(cocktail)
        assertFalse(vm.isFavorite("1"))
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
