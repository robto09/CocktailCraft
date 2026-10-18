package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.added_to_cart
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SharedCartViewModelTest : MainDispatcherTest() {

    private fun viewModel(
        cartRepository: FakeCartRepository = FakeCartRepository(),
        detailRepository: FakeDetailRepository = FakeDetailRepository()
    ) = SharedCartViewModel(ManageCartUseCase(cartRepository, detailRepository))

    @Test
    fun initialLoadPopulatesCartFromRepository() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = Money(1200)), quantity = 2))

        val vm = viewModel(repository)
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.itemCount)
        assertEquals(Money(2400), vm.uiState.value.totalPrice)
        assertTrue(vm.uiState.value.cartItems.isNotEmpty())
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun addToCartUpdatesItemsAndDerivedTotals() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.addToCart(testCocktail("7", price = Money(800)), quantity = 3)
        advanceUntilIdle() // state arrives via the collected repository flow

        assertEquals(3, vm.uiState.value.itemCount)
        assertEquals(Money(2400), vm.uiState.value.totalPrice)
        assertEquals(3, vm.quantityOf("7"))
    }

    @Test
    fun updateQuantityEmitsNewTotals() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = Money(1000)), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(1, awaitItem().itemCount)
            vm.updateQuantity("1", 5)
            val updated = awaitItem()
            assertEquals(5, updated.itemCount)
            assertEquals(Money(5000), updated.totalPrice)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeFromCartFailureEmitsOptimisticRemovalThenRevert() = runTest {
        // SH-12 (Turbine): assert the emission SEQUENCE — optimistic removal
        // first, then the revert when the repository rejects the mutation.
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(1, awaitItem().itemCount)
            repository.failNextMutation = true
            vm.removeFromCart("1")
            assertEquals(0, awaitItem().itemCount, "optimistic removal must emit immediately")
            assertEquals(1, awaitItem().itemCount, "failure must emit the reverted state")
            cancelAndIgnoreRemainingEvents()
        }
        assertNotNull(vm.error.value)
    }

    @Test
    fun removeFromCartFailureRevertsOptimisticUpdateAndSetsError() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()

        repository.failNextMutation = true
        vm.removeFromCart("1")

        assertEquals(1, vm.quantityOf("1"), "failed removal must be reverted")
        assertNotNull(vm.error.value)
    }

    @Test
    fun nextCartActionClearsAnEarlierFailure() = runTest {
        // The ViewModel is a Koin single: one failure must not stick for the session
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()
        repository.failNextMutation = true
        vm.updateQuantity("1", 2)
        assertNotNull(vm.error.value)

        vm.updateQuantity("1", 2)

        assertNull(vm.error.value, "a successful retry must clear the earlier error")
        assertEquals(2, vm.quantityOf("1"))
    }

    @Test
    fun addToCartSuccessAnnouncesAddedEvent() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.events.test {
            assertTrue(vm.addToCart(testCocktail("7", name = "Margarita")))
            val event = assertIs<UiEvent.Success>(awaitItem())
            assertEquals(MR.strings.added_to_cart.format("Margarita"), event.message)
        }
        assertNull(vm.error.value)
    }

    @Test
    fun addToCartFailureAnnouncesFailureWithoutTouchingTheCartError() = runTest {
        val repository = FakeCartRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()
        repository.failNextMutation = true

        vm.events.test {
            assertFalse(vm.addToCart(testCocktail("7")))
            val event = assertIs<UiEvent.Failure>(awaitItem())
            assertEquals(StringDesc.Raw("add failed"), event.message)
        }
        // Add-to-cart runs from other screens: its failure must not wait on
        // the cart's error channel to pop up later on the Cart tab
        assertNull(vm.error.value)
        assertTrue(vm.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun decrementAtQuantityOneRemovesTheItem() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.decrementQuantity("1")

        assertEquals(0, vm.quantityOf("1"))
        assertTrue(vm.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun clearCartEmptiesStateAndRepository() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 2))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.clearCart()

        assertTrue(vm.uiState.value.cartItems.isEmpty())
        assertEquals(Money.ZERO, vm.uiState.value.totalPrice)
        assertEquals(emptyList(), (repository.getCartItems() as Result.Success).data)
    }

    @Test
    fun deliveryFeeIsWaivedAtFiftyAndChargedBelow() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = Money(1000)), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.uiState.test {
            // Delivery pricing is derived on the emitted state, so every
            // emission carries fee/total consistent with its subtotal.
            val belowThreshold = awaitItem()
            assertEquals(Money(599), belowThreshold.deliveryFee)
            assertEquals(Money(1599), belowThreshold.finalTotal)

            vm.updateQuantity("1", 5) // $50.00 total → free delivery threshold
            val free = awaitItem()
            assertEquals(Money.ZERO, free.deliveryFee)
            assertEquals(Money(5000), free.finalTotal)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Quantity of [cocktailId] in the current cart state (0 when absent). */
    private fun SharedCartViewModel.quantityOf(cocktailId: String): Int =
        uiState.value.cartItems.find { it.cocktail.id == cocktailId }?.quantity ?: 0
}
