package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_invalid_user_title
import com.cocktailcraft.resources.error_review_user_required
import com.cocktailcraft.resources.review_error_comment_required
import com.cocktailcraft.resources.review_error_comment_too_long
import com.cocktailcraft.resources.review_error_comment_too_short
import com.cocktailcraft.resources.review_error_rating_range
import com.cocktailcraft.testutil.DataLayerTest
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.Koin

@OptIn(ExperimentalCoroutinesApi::class)
class SharedReviewViewModelTest : DataLayerTest() {

    /**
     * A fresh in-memory database behind the real ReviewRepositoryImpl
     * (internal to :core:data, so resolved through dataModule); io work runs
     * on the test scheduler. Repositories and ViewModels built from the same
     * [Koin] share that storage.
     */
    private fun TestScope.createTestDatabase(): Koin = dataLayer(StandardTestDispatcher(testScheduler))

    private fun repository(db: Koin): ReviewRepository = db.get()

    private fun viewModel(db: Koin) =
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

        assertEquals(MR.strings.error_invalid_user_title.desc(), vm.error.value?.title)
        assertEquals(MR.strings.error_review_user_required.desc(), vm.error.value?.message)
        assertEquals(0, vm.reviewCountFor("c1"))
    }

    @Test
    fun eachValidationRuleReportsItsOwnMessage() = runTest {
        // The iOS review sheet shows these messages verbatim, so pin them
        // (their English copy is pinned in SharedStringsTest).
        val vm = viewModel(createTestDatabase())
        advanceUntilIdle()

        fun rejection(rating: Float, comment: String): StringDesc? {
            assertFalse(vm.validateReview(rating, comment))
            return vm.error.value?.message.also { vm.clearError() }
        }

        assertEquals(MR.strings.review_error_rating_range.desc(), rejection(0.0f, "Long enough comment."))
        assertEquals(MR.strings.review_error_rating_range.desc(), rejection(5.5f, "Long enough comment."))
        assertEquals(MR.strings.review_error_comment_required.desc(), rejection(3.0f, "   "))
        assertEquals(MR.strings.review_error_comment_too_short.format(10), rejection(3.0f, "Too short"))
        assertEquals(MR.strings.review_error_comment_too_long.format(500), rejection(3.0f, "a".repeat(501)))

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
