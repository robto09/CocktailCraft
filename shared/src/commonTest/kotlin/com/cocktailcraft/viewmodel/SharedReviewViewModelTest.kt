package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.repository.ReviewRepositoryImpl
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.createTestLocalDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SharedReviewViewModelTest : MainDispatcherTest() {

    private fun TestScope.repository(db: CocktailCraftDatabase): ReviewRepositoryImpl {
        val io = StandardTestDispatcher(testScheduler)
        return ReviewRepositoryImpl(createTestLocalDatabase(db, ioDispatcher = io), io)
    }

    private fun TestScope.viewModel(db: CocktailCraftDatabase) =
        SharedReviewViewModel(ManageReviewsUseCase(repository(db)))

    @Test
    fun submittedReviewFlowsIntoStateAndPersists() = runTest {
        val db = createTestDatabase()
        val vm = viewModel(db)
        advanceUntilIdle()

        vm.loadReviewsForCocktail("c1")
        assertTrue(vm.submitReview("c1", 4.5f, "Great balance of sweet and sour.", "Robert"))
        advanceUntilIdle() // state arrives via the collected repository flow

        assertEquals(1, vm.uiState.value.reviewCount)
        assertEquals(4.5f, vm.uiState.value.averageRating)

        // A fresh ViewModel over the same storage sees the review: it persisted
        val reloaded = viewModel(db)
        advanceUntilIdle()
        assertEquals(1, reloaded.reviewCountFor("c1"))
    }

    @Test
    fun reviewObserverEmitsWhenRepositoryChanges() = runTest {
        // SH-12 (Turbine): the observed reviews flow must push repository
        // changes into uiState without a manual reload.
        val db = createTestDatabase()
        val repo = repository(db)
        val vm = SharedReviewViewModel(ManageReviewsUseCase(repo))
        advanceUntilIdle()
        vm.loadReviewsForCocktail("c1")
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(0, awaitItem().reviewCount)
            repo.addReview(
                Review(id = "r9", cocktailId = "c1", userName = "A", rating = 4.0f,
                    comment = "Really quite refreshing.", date = "2026-07-05")
            ).getOrThrow()
            var state = awaitItem()
            while (state.reviewCount == 0) state = awaitItem()
            assertEquals(1, state.reviewCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invalidReviewIsRejectedWithValidationError() = runTest {
        val vm = viewModel(createTestDatabase())
        advanceUntilIdle()

        assertFalse(vm.submitReview("c1", 0.0f, "Rating out of range here.", "Robert"))
        assertNotNull(vm.error.value)
        vm.clearError()

        assertFalse(vm.submitReview("c1", 4.0f, "short", "Robert"))
        assertNotNull(vm.error.value)

        assertEquals(0, vm.reviewCountFor("c1"))
    }

    @Test
    fun loadReviewsForCocktailDerivesPerCocktailSlices() = runTest {
        val db = createTestDatabase()
        val repo = repository(db)
        repo.addReview(Review(id = "r1", cocktailId = "c1", userName = "A", rating = 2.0f, comment = "Far too sweet for me.", date = "2026-07-01")).getOrThrow()
        repo.addReview(Review(id = "r2", cocktailId = "c1", userName = "B", rating = 4.0f, comment = "Really quite refreshing.", date = "2026-07-03")).getOrThrow()
        repo.addReview(Review(id = "r3", cocktailId = "c2", userName = "C", rating = 5.0f, comment = "The best drink in town.", date = "2026-07-02")).getOrThrow()

        val vm = viewModel(db)
        advanceUntilIdle()

        vm.loadReviewsForCocktail("c1")
        assertEquals(2, vm.uiState.value.reviewCount)
        assertEquals(3.0f, vm.uiState.value.averageRating)
        assertEquals(listOf("r2", "r1"), vm.uiState.value.currentCocktailReviews.map { it.id }, "newest first")
        assertEquals(1, vm.reviewCountFor("c2"))
    }

    @Test
    fun deleteReviewReportsMissingIdWithError() = runTest {
        val db = createTestDatabase()
        val vm = viewModel(db)
        advanceUntilIdle()

        assertFalse(vm.deleteReview("missing"))
        assertNotNull(vm.error.value)
    }

    @Test
    fun updateReviewChangesStateReactively() = runTest {
        val db = createTestDatabase()
        val repo = repository(db)
        repo.addReview(Review(id = "r1", cocktailId = "c1", userName = "A", rating = 2.0f, comment = "Far too sweet for me.", date = "2026-07-01")).getOrThrow()

        val vm = viewModel(db)
        advanceUntilIdle()
        vm.loadReviewsForCocktail("c1")

        assertTrue(vm.updateReview("r1", 5.0f, "It grew on me after all."))
        advanceUntilIdle()

        assertEquals(5.0f, vm.uiState.value.averageRating)
        assertEquals("It grew on me after all.", vm.uiState.value.currentCocktailReviews.single().comment)
    }

    @Test
    fun blankUserNameIsRejectedAndNothingIsStored() = runTest {
        // Neither platform requires sign-in; the name typed into the review
        // form is the only identity, so a blank one must be refused here.
        val vm = viewModel(createTestDatabase())
        advanceUntilIdle()

        assertFalse(vm.submitReview("c1", 4.0f, "Great balance of sweet and sour.", "   "))
        advanceUntilIdle()

        assertEquals("Invalid User", vm.error.value?.title)
        assertEquals("User name is required to submit a review", vm.error.value?.message)
        assertEquals(0, vm.reviewCountFor("c1"))
    }

    @Test
    fun eachValidationRuleReportsItsOwnMessage() = runTest {
        // The iOS review sheet shows these messages verbatim, so pin them.
        val vm = viewModel(createTestDatabase())
        advanceUntilIdle()

        fun rejection(rating: Float, comment: String): String? {
            assertFalse(vm.validateReview(rating, comment))
            return vm.error.value?.message.also { vm.clearError() }
        }

        assertEquals("Rating must be between 1 and 5 stars", rejection(0.0f, "Long enough comment."))
        assertEquals("Rating must be between 1 and 5 stars", rejection(5.5f, "Long enough comment."))
        assertEquals("Please provide a comment for your review", rejection(3.0f, "   "))
        assertEquals("Review comment must be at least 10 characters long", rejection(3.0f, "Too short"))
        assertEquals("Review comment must be less than 500 characters", rejection(3.0f, "a".repeat(501)))

        assertTrue(vm.validateReview(1.0f, "a".repeat(10)), "10 characters is the lower bound")
        assertTrue(vm.validateReview(5.0f, "a".repeat(500)), "500 characters is the upper bound")
        assertEquals(null, vm.error.value)
    }

    @Test
    fun submitTrimsNameAndComment() = runTest {
        val vm = viewModel(createTestDatabase())
        advanceUntilIdle()
        vm.loadReviewsForCocktail("c1")

        assertTrue(vm.submitReview("c1", 5.0f, "  Great balance of sweet and sour.  ", "  Robert  "))
        advanceUntilIdle()

        val review = vm.uiState.value.currentCocktailReviews.single()
        assertEquals("Robert", review.userName)
        assertEquals("Great balance of sweet and sour.", review.comment)
        assertEquals("c1", review.cocktailId)
    }

    @Test
    fun selectingUnreviewedCocktailShowsEmptyState() = runTest {
        val db = createTestDatabase()
        val repo = repository(db)
        repo.addReview(Review(id = "r1", cocktailId = "c1", userName = "A", rating = 4.0f, comment = "Really quite refreshing.", date = "2026-07-01")).getOrThrow()

        val vm = viewModel(db)
        advanceUntilIdle()
        vm.loadReviewsForCocktail("c1")
        assertEquals(1, vm.uiState.value.reviewCount)

        vm.loadReviewsForCocktail("c2")

        val state = vm.uiState.value
        assertEquals("c2", state.currentCocktailId)
        assertTrue(state.currentCocktailReviews.isEmpty())
        assertEquals(0, state.reviewCount)
        assertEquals(0.0f, state.averageRating)
    }

    /** Stored reviews for [cocktailId] in the grouped uiState store. */
    private fun SharedReviewViewModel.reviewCountFor(cocktailId: String): Int =
        uiState.value.reviews[cocktailId]?.size ?: 0
}
