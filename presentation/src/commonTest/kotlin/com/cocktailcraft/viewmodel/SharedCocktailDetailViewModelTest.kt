package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.usecase.GetCocktailDetailUseCase
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.added_to_cart
import com.cocktailcraft.resources.error_add_item_to_cart_failed
import com.cocktailcraft.resources.error_load_cocktail_details_failed
import com.cocktailcraft.resources.favorite_removed
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeFavoritesRepository
import com.cocktailcraft.testutil.FakeSearchRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A detail source that always fails, for error-path tests. */
private class FailingDetailRepository(
    private val message: String = "detail lookup failed"
) : CocktailDetailRepository {
    override suspend fun getCocktailById(id: String): Result<Cocktail?> = Result.Error(message)
    override fun observeCocktail(id: String): Flow<Cocktail?> = flowOf(null)
    override suspend fun refreshCocktail(id: String, force: Boolean): Result<Unit> = Result.Error(message)
    override suspend fun getRandomCocktail(): Result<Cocktail?> = Result.Error(message)
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
            manageCartUseCase = ManageCartUseCase(cart, detailRepository)
        )
    }

    @Test
    fun unfavoritingOffersUndoThatFavoritesItAgain() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", name = "Martini")
        harness.favorites.addToFavorites(testCocktail("1", name = "Martini"))
        val vm = harness.viewModel()
        vm.loadCocktail("1")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isFavorite)

        val event = assertIs<UiEvent.Success>(vm.eventFrom { vm.toggleFavorite() })
        assertEquals(MR.strings.favorite_removed.format("Martini"), event.message)
        assertFalse(vm.uiState.value.isFavorite)
        assertTrue(harness.favorites.stored.isEmpty())

        assertNotNull(event.action).perform()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isFavorite)
        assertEquals(listOf("1"), harness.favorites.stored.map { it.id })
    }

    @Test
    fun favoritingOffersNoUndo() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1")
        val vm = harness.viewModel()
        vm.loadCocktail("1")
        advanceUntilIdle()

        vm.events.test {
            vm.toggleFavorite()
            expectNoEvents()
        }
        assertTrue(vm.uiState.value.isFavorite)
    }

    @Test
    fun addToCartAddsTheCocktailAndAnnouncesIt() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", name = "Martini")
        val vm = harness.viewModel()
        vm.loadCocktail("1")
        advanceUntilIdle()

        val event = assertIs<UiEvent.Success>(vm.eventFrom { vm.addToCart() })

        assertEquals(MR.strings.added_to_cart.format("Martini"), event.message)
        assertEquals(listOf("1" to 1), harness.cart.cartContents())
        assertFalse(vm.uiState.value.isAddingToCart)
        assertNull(vm.error.value)
    }

    @Test
    fun failedAddToCartAnnouncesTheFailureNotASuccess() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1")
        val vm = harness.viewModel()
        vm.loadCocktail("1")
        advanceUntilIdle()
        harness.cart.failNextMutation = true

        val event = assertIs<UiEvent.Failure>(vm.eventFrom { vm.addToCart() })

        assertEquals(MR.strings.error_add_item_to_cart_failed.desc(), event.message)
        assertTrue(harness.cart.cartContents().isEmpty())
        assertFalse(vm.uiState.value.isAddingToCart)
        // Toasted on this screen, not parked on the detail error channel
        assertNull(vm.error.value)
    }

    @Test
    fun addToCartWithoutLoadedCocktailIsNoOp() = runTest {
        val harness = Harness()
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.events.test {
            vm.addToCart()
            expectNoEvents()
        }
        assertTrue(harness.cart.cartContents().isEmpty())
    }

    private suspend fun FakeCartRepository.cartContents(): List<Pair<String, Int>> =
        (getCartItems() as Result.Success).data.map { it.cocktail.id to it.quantity }

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
        assertNull(vm.error.value)
    }

    @Test
    fun loadedStateCarriesShareText() = runTest {
        // Derived into uiState so both platforms observe it reactively
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", name = "Martini")
        val vm = harness.viewModel()
        assertEquals("", vm.uiState.value.shareableText)

        vm.loadCocktail("1")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.shareableText.startsWith("🍹 Martini"))
        assertTrue(state.shareableText.contains("• 50 ml Gin"))
        assertTrue(state.shareableText.endsWith("Category: Cocktail"))
    }

    @Test
    fun shareableTextHasNoStrayIndentWithSeveralIngredients() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", name = "Margarita").copy(
            ingredients = listOf(
                CocktailIngredient("Tequila", "1 1/2 oz"),
                CocktailIngredient("Triple sec", "1/2 oz"),
                CocktailIngredient("Salt", "")
            ),
            instructions = "Shake with ice."
        )
        val vm = harness.viewModel()

        vm.loadCocktail("1")
        advanceUntilIdle()

        assertEquals(
            """
            🍹 Margarita

            Ingredients:
            • 1 1/2 oz Tequila
            • 1/2 oz Triple sec
            • Salt

            Instructions:
            Shake with ice.

            Glass: Cocktail glass
            Category: Cocktail
            """.trimIndent(),
            vm.uiState.value.shareableText
        )
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
        assertEquals(MR.strings.error_load_cocktail_details_failed.desc(), error.message)
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
        assertTrue(vm.uiState.value.isFavorite, "the heart flips on tap")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isFavorite)
        assertEquals(listOf("1"), harness.favorites.stored.map { it.id })

        vm.toggleFavorite()
        assertFalse(vm.uiState.value.isFavorite, "the heart flips on tap")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isFavorite)
        assertTrue(harness.favorites.stored.isEmpty())
    }

    // --- Stale-while-revalidate through the stored cocktail's flow ---

    @Test
    fun backgroundRevalidationReachesTheScreenThroughTheFlow() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", name = "Cached Martini")
        harness.detail.remote["1"] = testCocktail("1", name = "Fresh Martini")
        val vm = harness.viewModel()

        vm.loadCocktail("1")
        advanceUntilIdle()

        assertEquals("Fresh Martini", vm.uiState.value.cocktail?.name)
        assertEquals(listOf("1" to false), harness.detail.refreshCalls, "only refetched when stale")
    }

    @Test
    fun failedBackgroundRevalidationKeepsTheCachedCopySilently() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", name = "Cached Martini")
        harness.detail.refreshError = "offline"
        val vm = harness.viewModel()

        vm.loadCocktail("1")
        advanceUntilIdle()

        assertEquals("Cached Martini", vm.uiState.value.cocktail?.name)
        assertNull(vm.error.value)
    }

    @Test
    fun explicitRefreshPublishesTheFetchedCopyAndReportsFailures() = runTest {
        val harness = Harness()
        harness.detail.byId["1"] = testCocktail("1", name = "Cached Martini")
        val vm = harness.viewModel()
        vm.loadCocktail("1")
        advanceUntilIdle()

        harness.detail.remote["1"] = testCocktail("1", name = "Fresh Martini")
        vm.refreshCocktail()
        advanceUntilIdle()
        assertEquals("Fresh Martini", vm.uiState.value.cocktail?.name)

        harness.detail.refreshError = "offline"
        vm.refreshCocktail()
        advanceUntilIdle()
        assertNotNull(vm.error.value, "a failed explicit refresh is reported")
        assertEquals("Fresh Martini", vm.uiState.value.cocktail?.name, "the stored copy stays on screen")
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
}
