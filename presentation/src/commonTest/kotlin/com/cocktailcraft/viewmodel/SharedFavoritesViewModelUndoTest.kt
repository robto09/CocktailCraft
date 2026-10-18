package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.favorite_removed
import com.cocktailcraft.resources.favorites_cleared
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FlakyFavoritesRepository
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

/** Undo for unfavoriting from the Favorites screen, and for clearing all favorites. */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedFavoritesViewModelUndoTest : MainDispatcherTest() {

    private val repository = FlakyFavoritesRepository()

    private suspend fun TestScope.viewModelWith(vararg ids: String): SharedFavoritesViewModel {
        ids.forEach { repository.addToFavorites(testCocktail(it, name = "Cocktail $it")) }
        val vm = SharedFavoritesViewModel(ManageFavoritesUseCase(repository, FakeDetailRepository()))
        advanceUntilIdle()
        return vm
    }

    private fun SharedFavoritesViewModel.ids() = uiState.value.favorites.map { it.id }

    @Test
    fun removingAFavoriteOffersUndoThatFavoritesItAgain() = runTest {
        val vm = viewModelWith("1", "2")

        val removed = testCocktail("2", name = "Cocktail 2")
        val event = assertIs<UiEvent.Success>(vm.eventFrom { vm.removeFavorite(removed) })
        assertEquals(MR.strings.favorite_removed.format("Cocktail 2"), event.message)
        advanceUntilIdle()
        assertEquals(listOf("1"), vm.ids())

        assertNotNull(event.action).perform()
        advanceUntilIdle()

        assertEquals(listOf("1", "2"), vm.ids())
        assertEquals(listOf("1", "2"), repository.stored.map { it.id })
    }

    @Test
    fun failedRemovalReportsAndOffersNoUndo() = runTest {
        val vm = viewModelWith("1")
        repository.failingMutations = 1

        vm.events.test {
            vm.removeFavorite(testCocktail("1"))
            expectNoEvents()
        }

        assertNotNull(vm.error.value)
        assertEquals(listOf("1"), repository.stored.map { it.id })
    }

    @Test
    fun failedUndoIsReported() = runTest {
        val vm = viewModelWith("1")
        val undo = vm.undoFrom { vm.removeFavorite(testCocktail("1")) }
        repository.failingMutations = 1

        undo.perform()
        advanceUntilIdle()

        assertNotNull(vm.error.value)
        assertTrue(repository.stored.isEmpty())
    }

    @Test
    fun clearingAllFavoritesOffersUndoThatRestoresThemInOrder() = runTest {
        val vm = viewModelWith("1", "2", "3")

        val event = vm.eventFrom { vm.clearAllFavorites() }
        assertEquals(MR.strings.favorites_cleared.desc(), event.message)
        advanceUntilIdle()
        assertTrue(vm.ids().isEmpty())

        assertNotNull(event.action).perform()
        advanceUntilIdle()

        assertEquals(listOf("1", "2", "3"), vm.ids())
    }
}
