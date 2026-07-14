package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.usecase.AnalyzeCocktailUseCase
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeFavoritesRepository
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

/** A detail source that always fails, for error-path tests. */
private class FailingDetailRepository(
    private val message: String = "detail lookup failed"
) : CocktailDetailRepository {
    override suspend fun getCocktailById(id: String): Result<Cocktail?> = Result.Error(message)
    override suspend fun refreshCocktailById(id: String): Result<Cocktail?> = Result.Error(message)
    override suspend fun getRandomCocktail(): Result<Cocktail?> = Result.Error(message)
    override fun getCocktailImageUrl(cocktail: Cocktail): String = ""
}

@OptIn(ExperimentalCoroutinesApi::class)
class SharedCocktailDetailViewModelTest : MainDispatcherTest() {

    /** All fakes behind the real use-case stack, so tests exercise VM + domain together. */
    private class Harness {
        val detail = FakeDetailRepository()
        val favorites = FakeFavoritesRepository()
        val search = FakeSearchRepository()
        val cart = FakeCartRepository()

        fun viewModel(detailRepository: CocktailDetailRepository = detail) = SharedCocktailDetailViewModel(
            getCocktailDetailUseCase = GetCocktailDetailUseCase(detailRepository, favorites, search),
            manageFavoritesUseCase = ManageFavoritesUseCase(favorites, detailRepository),
            manageCartUseCase = ManageCartUseCase(cart, detailRepository),
            analyzeCocktailUseCase = AnalyzeCocktailUseCase()
        )
    }

    @Test
    fun loadCocktailPopulatesDetailState() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", name = "Martini")
        val vm = harness.viewModel()

        vm.loadCocktail("1")
        advanceUntilIdle()

        assertEquals("Martini", vm.uiState.value.cocktail?.name)
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isFavorite)
        assertEquals(mapOf("Spirits" to listOf("Gin")), vm.uiState.value.ingredientsByType)
        assertNull(vm.error.value)
    }

    @Test
    fun loadCocktailMarksExistingFavorite() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1")
        harness.favorites.addToFavorites(testCocktail("1"))
        val vm = harness.viewModel()

        vm.loadCocktail("1")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isFavorite)
    }

    @Test
    fun loadCocktailLoadsRelatedCocktailsFromSameCategory() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", category = "Cocktail")
        harness.search.all = listOf(
            testCocktail("1", category = "Cocktail"),
            testCocktail("2", category = "Cocktail"),
            testCocktail("3", category = "Cocktail"),
            testCocktail("4", category = "Shot")
        )
        val vm = harness.viewModel()

        vm.loadCocktail("1")
        advanceUntilIdle()

        assertEquals(setOf("2", "3"), vm.uiState.value.relatedCocktails.map { it.id }.toSet())
    }

    @Test
    fun loadCocktailErrorSurfacesErrorAndStopsLoading() = runTest {
        val harness = Harness()
        val vm = harness.viewModel(FailingDetailRepository())

        vm.loadCocktail("1")
        advanceUntilIdle()

        val error = assertNotNull(vm.error.value)
        assertEquals("detail lookup failed", error.message)
        assertNull(vm.uiState.value.cocktail)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun loadMissingCocktailLeavesStateEmptyWithoutError() = runTest {
        val harness = Harness()
        val vm = harness.viewModel()

        vm.loadCocktail("does-not-exist")
        advanceUntilIdle()

        assertNull(vm.uiState.value.cocktail)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.error.value)
    }

    @Test
    fun toggleFavoritePersistsAndFlipsState() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1")
        val vm = harness.viewModel()
        vm.loadCocktail("1")
        advanceUntilIdle()

        vm.toggleFavorite()
        assertTrue(vm.uiState.value.isFavorite)
        assertEquals(listOf("1"), harness.favorites.stored.map { it.id })

        vm.toggleFavorite()
        assertFalse(vm.uiState.value.isFavorite)
        assertTrue(harness.favorites.stored.isEmpty())
    }

    @Test
    fun toggleFavoriteWithoutLoadedCocktailIsNoOp() = runTest {
        val harness = Harness()
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.toggleFavorite()

        assertFalse(vm.uiState.value.isFavorite)
        assertTrue(harness.favorites.stored.isEmpty())
    }

    @Test
    fun addToCartReflectsQuantityInState() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1")
        val vm = harness.viewModel()
        vm.loadCocktail("1")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isInCart)

        vm.addToCart(quantity = 2)

        assertTrue(vm.uiState.value.isInCart)
        assertEquals(2, vm.uiState.value.cartQuantity)
    }

    @Test
    fun updateCartQuantityToZeroRemovesFromCart() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1")
        val vm = harness.viewModel()
        vm.loadCocktail("1")
        advanceUntilIdle()
        vm.addToCart(quantity = 2)

        vm.updateCartQuantity(5)
        assertEquals(5, vm.uiState.value.cartQuantity)

        vm.updateCartQuantity(0)
        assertFalse(vm.uiState.value.isInCart)
        assertEquals(0, vm.uiState.value.cartQuantity)
    }
}
