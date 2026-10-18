package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.usecase.ManageFavoritesUseCase
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeFavoritesRepository
import com.cocktailcraft.testutil.FlakyFavoritesRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
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
class SharedFavoritesViewModelTest : MainDispatcherTest() {

    private fun viewModel(
        favoritesRepository: FakeFavoritesRepository = FakeFavoritesRepository(),
        detailRepository: FakeDetailRepository = FakeDetailRepository()
    ) = SharedFavoritesViewModel(ManageFavoritesUseCase(favoritesRepository, detailRepository))

    @Test
    fun initialLoadPopulatesFavoritesFromRepository() = runTest {
        val repository = FakeFavoritesRepository()
        repository.addToFavorites(testCocktail("1"))
        repository.addToFavorites(testCocktail("2"))

        val vm = viewModel(repository)
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.favoriteCount)
        assertTrue(vm.isFavorite("1"))
    }

    @Test
    fun toggleAddsCocktailWhenNotFavorite() = runTest {
        val repository = FakeFavoritesRepository()
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.toggleFavorite(testCocktail("42"))
        advanceUntilIdle() // state arrives via the observed favorites flow (SH-4)

        assertTrue(vm.isFavorite("42"))
        assertEquals(1, vm.uiState.value.favoriteCount)
        assertTrue(repository.stored.any { it.id == "42" })
    }

    @Test
    fun toggleRemovesCocktailWhenAlreadyFavorite() = runTest {
        val repository = FakeFavoritesRepository()
        val cocktail = testCocktail("42")
        repository.addToFavorites(cocktail)
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.toggleFavorite(cocktail)
        advanceUntilIdle() // state arrives via the observed favorites flow (SH-4)

        assertFalse(vm.isFavorite("42"))
        assertTrue(vm.uiState.value.favorites.isEmpty())
        assertTrue(repository.stored.isEmpty())
    }

    @Test
    fun clearAllFavoritesEmptiesStateAndRepository() = runTest {
        val repository = FakeFavoritesRepository()
        repository.addToFavorites(testCocktail("1"))
        repository.addToFavorites(testCocktail("2"))
        val vm = viewModel(repository)
        advanceUntilIdle()

        vm.clearAllFavorites()
        advanceUntilIdle() // state arrives via the observed favorites flow (SH-4)

        assertTrue(vm.uiState.value.favorites.isEmpty())
        assertEquals(0, vm.uiState.value.favoriteCount)
        assertTrue(repository.stored.isEmpty())
    }

    @Test
    fun toggleFailureReportsErrorAndKeepsState() = runTest {
        val repository = FlakyFavoritesRepository()
        val vm = SharedFavoritesViewModel(ManageFavoritesUseCase(repository, FakeDetailRepository()))
        advanceUntilIdle()

        repository.failingMutations = 1
        vm.toggleFavorite(testCocktail("42"))
        advanceUntilIdle()

        val error = assertNotNull(vm.error.value, "a failed toggle must not be silently dropped")
        assertEquals(StringDesc.Raw("favorites store unavailable"), error.message)
        assertFalse(vm.isFavorite("42"))
    }

    @Test
    fun nextToggleClearsAnEarlierFailure() = runTest {
        // The ViewModel is a Koin single: one failure must not stick for the session
        val repository = FlakyFavoritesRepository()
        val vm = SharedFavoritesViewModel(ManageFavoritesUseCase(repository, FakeDetailRepository()))
        advanceUntilIdle()
        repository.failingMutations = 1
        vm.toggleFavorite(testCocktail("42"))
        assertNotNull(vm.error.value)

        vm.toggleFavorite(testCocktail("42"))
        advanceUntilIdle()

        assertNull(vm.error.value, "a successful retry must clear the earlier error")
        assertTrue(vm.isFavorite("42"))
    }

    @Test
    fun clearAllFavoritesReportsAFailedRemoval() = runTest {
        val repository = FlakyFavoritesRepository()
        repository.addToFavorites(testCocktail("1"))
        repository.addToFavorites(testCocktail("2"))
        val vm = SharedFavoritesViewModel(ManageFavoritesUseCase(repository, FakeDetailRepository()))
        advanceUntilIdle()

        repository.failingMutations = 1
        vm.clearAllFavorites()
        advanceUntilIdle()

        assertNotNull(vm.error.value, "a failed removal must be reported")
        // The other removal still went through
        assertEquals(listOf("1"), vm.uiState.value.favorites.map { it.id })
    }

    private fun SharedFavoritesViewModel.isFavorite(cocktailId: String): Boolean =
        uiState.value.favorites.any { it.id == cocktailId }
}
