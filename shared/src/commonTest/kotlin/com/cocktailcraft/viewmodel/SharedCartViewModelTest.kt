package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
        repository.addToCart(CocktailCartItem(testCocktail("1", price = 12.0), quantity = 2))

        val vm = viewModel(repository)
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.itemCount)
        assertEquals(24.0, vm.uiState.value.totalPrice)
        assertTrue(vm.hasItems)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun addToCartUpdatesItemsAndDerivedTotals() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.addToCart(testCocktail("7", price = 8.0), quantity = 3)
        advanceUntilIdle() // state arrives via the collected repository flow

        assertEquals(3, vm.uiState.value.itemCount)
        assertEquals(24.0, vm.uiState.value.totalPrice)
        assertTrue(vm.isInCart("7"))
        assertEquals(3, vm.getQuantity("7"))
    }

    @Test
    fun updateQuantityEmitsNewTotals() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = 10.0), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(1, awaitItem().itemCount)
            vm.updateQuantity("1", 5)
            val updated = awaitItem()
            assertEquals(5, updated.itemCount)
            assertEquals(50.0, updated.totalPrice)
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

        assertTrue(vm.isInCart("1"), "failed removal must be reverted")
        assertNotNull(vm.error.value)
    }

    @Test
    fun decrementAtQuantityOneRemovesTheItem() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.decrementQuantity("1")

        assertFalse(vm.isInCart("1"))
        assertTrue(vm.isEmpty)
    }

    @Test
    fun clearCartEmptiesStateAndRepository() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 2))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.clearCart()

        assertTrue(vm.isEmpty)
        assertEquals(0.0, vm.uiState.value.totalPrice)
        assertEquals(emptyList(), (repository.getCartItems() as Result.Success).data)
    }

    @Test
    fun deliveryFeeIsWaivedAtFiftyAndChargedBelow() = runTest {
        val repository = FakeCartRepository()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = 10.0), quantity = 1))
        val vm = viewModel(repository)
        advanceUntilIdle()

        assertFalse(vm.isFreeDelivery())
        assertEquals(5.99, vm.getDeliveryFee())
        assertEquals(15.99, vm.getFinalTotal())

        vm.updateQuantity("1", 5) // 50.0 total → free delivery threshold
        assertTrue(vm.isFreeDelivery())
        assertEquals(0.0, vm.getDeliveryFee())
        assertEquals(50.0, vm.getFinalTotal())
    }
}
