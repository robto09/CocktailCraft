package com.cocktailcraft.viewmodel

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.repository.ReviewRepositoryImpl
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.MainDispatcherTest
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SharedReviewViewModelTest : MainDispatcherTest() {

    private val json = Json { ignoreUnknownKeys = true }

    private fun TestScope.repository(settings: Settings) =
        ReviewRepositoryImpl(settings, json, AppConfigImpl(), StandardTestDispatcher(testScheduler))

    private fun TestScope.viewModel(settings: Settings) =
        SharedReviewViewModel(ManageReviewsUseCase(repository(settings)))

    @Test
    fun submittedReviewFlowsIntoStateAndPersists() = runTest {
        val settings = MapSettings()
        val vm = viewModel(settings)
        advanceUntilIdle()

        vm.loadReviewsForCocktail("c1")
        assertTrue(vm.submitReview("c1", 4.5f, "Great balance of sweet and sour.", "Robert"))
        advanceUntilIdle() // state arrives via the collected repository flow

        assertEquals(1, vm.uiState.value.reviewCount)
        assertEquals(4.5f, vm.uiState.value.averageRating)
        assertTrue(vm.hasReviews)

        // A fresh ViewModel over the same storage sees the review: it persisted
        val reloaded = viewModel(settings)
        advanceUntilIdle()
        assertEquals(1, reloaded.getReviewCount("c1"))
    }

    @Test
    fun invalidReviewIsRejectedWithValidationError() = runTest {
        val vm = viewModel(MapSettings())
        advanceUntilIdle()

        assertFalse(vm.submitReview("c1", 0.0f, "Rating out of range here.", "Robert"))
        assertNotNull(vm.error.value)
        vm.clearError()

        assertFalse(vm.submitReview("c1", 4.0f, "short", "Robert"))
        assertNotNull(vm.error.value)

        assertEquals(0, vm.getReviewCount("c1"))
    }

    @Test
    fun loadReviewsForCocktailDerivesPerCocktailSlices() = runTest {
        val settings = MapSettings()
        val repo = repository(settings)
        repo.addReview(Review(id = "r1", cocktailId = "c1", userName = "A", rating = 2.0f, comment = "Far too sweet for me.", date = "2026-07-01")).getOrThrow()
        repo.addReview(Review(id = "r2", cocktailId = "c1", userName = "B", rating = 4.0f, comment = "Really quite refreshing.", date = "2026-07-03")).getOrThrow()
        repo.addReview(Review(id = "r3", cocktailId = "c2", userName = "C", rating = 5.0f, comment = "The best drink in town.", date = "2026-07-02")).getOrThrow()

        val vm = viewModel(settings)
        advanceUntilIdle()

        vm.loadReviewsForCocktail("c1")
        assertEquals(2, vm.uiState.value.reviewCount)
        assertEquals(3.0f, vm.uiState.value.averageRating)
        assertEquals(listOf("r2", "r1"), vm.uiState.value.currentCocktailReviews.map { it.id }, "newest first")
        assertEquals(1, vm.getReviewCount("c2"))
    }

    @Test
    fun deleteReviewReportsMissingIdWithError() = runTest {
        val settings = MapSettings()
        val vm = viewModel(settings)
        advanceUntilIdle()

        assertFalse(vm.deleteReview("missing"))
        assertNotNull(vm.error.value)
    }

    @Test
    fun updateReviewChangesStateReactively() = runTest {
        val settings = MapSettings()
        val repo = repository(settings)
        repo.addReview(Review(id = "r1", cocktailId = "c1", userName = "A", rating = 2.0f, comment = "Far too sweet for me.", date = "2026-07-01")).getOrThrow()

        val vm = viewModel(settings)
        advanceUntilIdle()
        vm.loadReviewsForCocktail("c1")

        assertTrue(vm.updateReview("r1", 5.0f, "It grew on me after all."))
        advanceUntilIdle()

        assertEquals(5.0f, vm.uiState.value.averageRating)
        assertEquals("It grew on me after all.", vm.uiState.value.currentCocktailReviews.single().comment)
    }
}
