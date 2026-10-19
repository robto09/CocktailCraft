package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeReviewRepository
import com.cocktailcraft.testutil.MainDispatcherTest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * ViewModel actions run on viewModelScope (SharedViewModel's class notes):
 * the caller going away never aborts the work, and coroutine cancellation
 * is never reported as a failure.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelActionScopeTest : MainDispatcherTest() {

    /** [FakeReviewRepository] whose writes take a second, or throw [throwOnAdd]. */
    private class SlowReviewRepository(private val delegate: FakeReviewRepository = FakeReviewRepository()) :
        ReviewRepository by delegate {
        var throwOnAdd: Exception? = null
        val stored: List<Review> get() = delegate.stored

        override suspend fun addReview(review: Review): Result<Unit> {
            delay(WRITE_MS)
            throwOnAdd?.let { throw it }
            return delegate.addReview(review)
        }
    }

    @Test
    fun cancellingTheCallerDoesNotAbortTheSubmittedReview() = runTest {
        val repository = SlowReviewRepository()
        val vm = SharedReviewViewModel(ManageReviewsUseCase(repository))
        advanceUntilIdle()

        // e.g. the sheet's Swift Task, cancelled as the sheet is swiped away
        val caller = launch { vm.submitReview("1", 4f, VALID_COMMENT, "Ada") }
        advanceTimeBy(WRITE_MS / 2)
        caller.cancel()
        advanceUntilIdle()

        assertEquals(1, repository.stored.size, "the write must finish on viewModelScope")
        assertNull(vm.error.value)
    }

    @Test
    fun cancellationInsideAnActionPropagatesInsteadOfReportingAFailure() = runTest {
        val repository = SlowReviewRepository().apply { throwOnAdd = CancellationException("scope gone") }
        val vm = SharedReviewViewModel(ManageReviewsUseCase(repository))
        advanceUntilIdle()

        // Used to be caught as a failure: false, plus an error alert
        assertFailsWith<CancellationException> {
            vm.submitReview("1", 4f, VALID_COMMENT, "Ada")
        }
        assertNull(vm.error.value, "cancellation is teardown, not a failure to report")
    }

    private companion object {
        const val WRITE_MS = 1_000L
        const val VALID_COMMENT = "Balanced, bright and not too sweet."
    }
}
