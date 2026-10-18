package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.usecase.ManageCartUseCase
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.cart_cleared
import com.cocktailcraft.resources.cart_item_removed
import com.cocktailcraft.resources.undo
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Undo for the cart's destructive actions: remove, decrement to zero, clear. */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedCartViewModelUndoTest : MainDispatcherTest() {

    private val repository = FakeCartRepository()

    private suspend fun TestScope.viewModelWith(vararg items: CocktailCartItem): SharedCartViewModel {
        items.forEach { repository.addToCart(it) }
        val vm = SharedCartViewModel(ManageCartUseCase(repository, FakeDetailRepository()))
        advanceUntilIdle()
        return vm
    }

    private fun SharedCartViewModel.quantities(): List<Pair<String, Int>> =
        uiState.value.cartItems.map { it.cocktail.id to it.quantity }

    @Test
    fun removingAnItemOffersUndoThatRestoresItsQuantity() = runTest {
        val vm = viewModelWith(CocktailCartItem(testCocktail("1", name = "Margarita"), quantity = 3))

        val event = assertIs<UiEvent.Success>(vm.eventFrom { vm.removeFromCart("1") })
        assertEquals(MR.strings.cart_item_removed.format("Margarita"), event.message)
        val undo = assertNotNull(event.action)
        assertEquals(MR.strings.undo.desc(), undo.label)
        assertTrue(vm.uiState.value.cartItems.isEmpty())

        undo.perform()
        advanceUntilIdle()

        assertEquals(listOf("1" to 3), vm.quantities())
        assertEquals(3, vm.uiState.value.itemCount)
    }

    @Test
    fun decrementingToZeroOffersUndo() = runTest {
        val vm = viewModelWith(
            CocktailCartItem(testCocktail("1"), quantity = 2),
            CocktailCartItem(testCocktail("2"), quantity = 1)
        )

        val undo = vm.undoFrom { vm.decrementQuantity("2") }
        assertEquals(listOf("1" to 2), vm.quantities())

        undo.perform()
        advanceUntilIdle()

        assertEquals(listOf("1" to 2, "2" to 1), vm.quantities())
    }

    @Test
    fun updatingTheQuantityToZeroIsARemovalWithUndo() = runTest {
        val vm = viewModelWith(CocktailCartItem(testCocktail("1"), quantity = 4))

        val undo = vm.undoFrom { vm.updateQuantity("1", 0) }
        assertTrue(vm.uiState.value.cartItems.isEmpty())

        undo.perform()
        advanceUntilIdle()

        assertEquals(listOf("1" to 4), vm.quantities())
    }

    @Test
    fun undoRunsOnlyOnce() = runTest {
        val vm = viewModelWith(CocktailCartItem(testCocktail("1"), quantity = 2))
        val undo = vm.undoFrom { vm.removeFromCart("1") }

        undo.perform()
        undo.perform()
        advanceUntilIdle()

        assertEquals(listOf("1" to 2), vm.quantities(), "a second tap must not add the item twice")
        assertTrue(undo.isPerformed)
    }

    @Test
    fun undoAfterTheItemWasReAddedRestoresItsOldQuantity() = runTest {
        val vm = viewModelWith(CocktailCartItem(testCocktail("1"), quantity = 3))
        val undo = vm.undoFrom { vm.removeFromCart("1") }
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1))
        advanceUntilIdle()

        undo.perform()
        advanceUntilIdle()

        assertEquals(listOf("1" to 3), vm.quantities())
    }

    @Test
    fun failedRemovalOffersNoUndo() = runTest {
        val vm = viewModelWith(CocktailCartItem(testCocktail("1"), quantity = 1))
        repository.failNextMutation = true

        vm.events.test {
            vm.removeFromCart("1")
            expectNoEvents()
        }
        assertEquals(listOf("1" to 1), vm.quantities())
    }

    @Test
    fun clearingTheCartOffersUndoThatRestoresEveryItemInOrder() = runTest {
        val vm = viewModelWith(
            CocktailCartItem(testCocktail("1"), quantity = 2),
            CocktailCartItem(testCocktail("2"), quantity = 1),
            CocktailCartItem(testCocktail("3"), quantity = 5)
        )

        val event = vm.eventFrom { vm.clearCart() }
        assertEquals(MR.strings.cart_cleared.desc(), event.message)
        assertTrue(vm.uiState.value.cartItems.isEmpty())

        assertNotNull(event.action).perform()
        advanceUntilIdle()

        assertEquals(listOf("1" to 2, "2" to 1, "3" to 5), vm.quantities())
        assertEquals(8, vm.uiState.value.itemCount)
    }
}
