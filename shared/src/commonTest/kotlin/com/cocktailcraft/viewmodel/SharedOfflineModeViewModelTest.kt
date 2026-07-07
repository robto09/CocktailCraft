package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.usecase.ManageOfflineModeUseCase
import com.cocktailcraft.testutil.FakeCatalogRepository
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.cocktailcraft.testutil.FakeOfflineRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SharedOfflineModeViewModelTest : MainDispatcherTest() {

    private class Harness(initiallyOnline: Boolean = true) {
        val offline = FakeOfflineRepository()
        val catalog = FakeCatalogRepository()
        val network = FakeNetworkMonitor(initiallyOnline)

        fun viewModel() = SharedOfflineModeViewModel(
            manageOfflineModeUseCase = ManageOfflineModeUseCase(offline, catalog),
            networkMonitor = network
        )
    }

    @Test
    fun initReadsPersistedPreferenceAndCachedCocktails() = runTest {
        val harness = Harness()
        harness.offline.offlineModeEnabled = true
        harness.offline.recentlyViewed = listOf(testCocktail("1"), testCocktail("2"))

        val vm = harness.viewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isOfflineModeEnabled)
        assertEquals("Enabled", vm.getOfflineModeStatus())
        assertEquals(listOf("1", "2"), vm.uiState.value.recentlyViewedCocktails.map { it.id })
        assertEquals(2, vm.getCachedCocktailCount())
        assertTrue(vm.hasRecentlyViewed)
    }

    @Test
    fun toggleOfflineModePersistsPreferenceAndUpdatesState() = runTest {
        val harness = Harness()
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isOfflineModeEnabled)

        vm.toggleOfflineMode()
        assertTrue(vm.uiState.value.isOfflineModeEnabled)
        assertTrue(harness.offline.offlineModeEnabled, "offline preference must be persisted")

        vm.toggleOfflineMode()
        assertFalse(vm.uiState.value.isOfflineModeEnabled)
        assertFalse(harness.offline.offlineModeEnabled)
    }

    @Test
    fun enablingOfflineModeSyncsCatalogIntoCache() = runTest {
        val harness = Harness()
        harness.catalog.all = listOf(testCocktail("1"), testCocktail("2"), testCocktail("3"))
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setOfflineMode(true)

        assertEquals(3, vm.uiState.value.cacheSize)
        assertEquals("Just now", vm.uiState.value.lastSyncTime)
    }

    @Test
    fun networkLossAutoEnablesOfflineMode() = runTest {
        val harness = Harness()
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isNetworkAvailable)

        harness.network.setOnline(false)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isNetworkAvailable)
        assertTrue(vm.uiState.value.isOfflineModeEnabled)
        assertTrue(harness.offline.offlineModeEnabled, "offline preference must be persisted")
        assertEquals("Disconnected", vm.getNetworkStatus())
        assertTrue(vm.isOfflineModeRecommended())
    }

    @Test
    fun syncWithoutNetworkSetsNetworkError() = runTest {
        val harness = Harness()
        val vm = harness.viewModel()
        advanceUntilIdle()
        harness.network.setOnline(false)
        advanceUntilIdle()
        vm.clearError()

        vm.syncCachedData()

        val error = assertNotNull(vm.error.value)
        assertEquals("No Network Connection", error.title)
        assertEquals(ErrorHandler.ErrorCategory.NETWORK, error.category)
    }

    @Test
    fun disablingOfflineModeWhileOnlineReloadsRecentlyViewed() = runTest {
        val harness = Harness()
        harness.offline.offlineModeEnabled = true
        val vm = harness.viewModel()
        advanceUntilIdle()
        harness.offline.recentlyViewed = listOf(testCocktail("9"))

        vm.setOfflineMode(false)

        assertFalse(vm.uiState.value.isOfflineModeEnabled)
        assertFalse(harness.offline.offlineModeEnabled)
        assertEquals(listOf("9"), vm.uiState.value.recentlyViewedCocktails.map { it.id })
    }
}
