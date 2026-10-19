package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_delete_review_failed
import com.cocktailcraft.resources.error_invalid_user_title
import com.cocktailcraft.resources.error_review_delete_not_found
import com.cocktailcraft.resources.error_review_not_found_title
import com.cocktailcraft.resources.error_review_update_not_found
import com.cocktailcraft.resources.error_review_user_required
import com.cocktailcraft.resources.error_submit_review_failed
import com.cocktailcraft.resources.error_update_review_failed
import com.cocktailcraft.resources.error_validation_title
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingNonCancellation
import com.cocktailcraft.viewmodel.state.ReviewUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Review functionality.
 * Collects the persisted review store reactively, so submitted reviews
 * survive restarts and every state change flows through [uiState] — the only
 * public state surface (per-cocktail counts/averages are derived into it).
 *
 * It holds every review on the device, whoever wrote it; only the
 * [Review.isMine] ones can be updated or deleted (any other id reports
 * "not found"), so screens offer edit/delete on those alone.
 */
class SharedReviewViewModel internal constructor(private val manageReviewsUseCase: ManageReviewsUseCase) :
    SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            manageReviewsUseCase.observeReviews().collect { allReviews ->
                _uiState.update { state ->
                    state.withReviews(allReviews.groupBy { it.cocktailId })
                }
            }
        }
    }

    /**
     * Show the reviews of [cocktailId]: selects its slice of the observed
     * review store (nothing to fetch).
     */
    fun loadReviewsForCocktail(cocktailId: String) {
        _uiState.update { state ->
            state.copy(currentCocktailId = cocktailId).withReviews(state.reviews)
        }
    }

    /**
     * Submit a new review for a cocktail. Returns whether it was saved, so
     * the sheet can close; a rejection or failure is reported on [error].
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun submitReview(cocktailId: String, rating: Float, comment: String, userName: String): Boolean =
        runInViewModelScope { submit(cocktailId, rating, comment, userName) }

    private suspend fun submit(cocktailId: String, rating: Float, comment: String, userName: String): Boolean {
        clearError()
        if (!validateReview(rating, comment) || !validateUserName(userName)) return false

        return runCatchingNonCancellation(onFailure = {
            handleException(it, MR.strings.error_submit_review_failed)
            false
        }) {
            manageReviewsUseCase.submitReview(cocktailId, rating, comment, userName).getOrThrow()
            true
        }
    }

    /**
     * Update an existing review. Returns whether it was saved.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun updateReview(reviewId: String, rating: Float, comment: String): Boolean = runInViewModelScope {
        clearError()
        if (!validateReview(rating, comment)) {
            return@runInViewModelScope false
        }

        runCatchingNonCancellation(onFailure = {
            handleException(it, MR.strings.error_update_review_failed)
            false
        }) {
            val found = manageReviewsUseCase.updateReview(reviewId, rating, comment).getOrThrow()
            if (!found) {
                setError(
                    MR.strings.error_review_not_found_title,
                    MR.strings.error_review_update_not_found,
                    ErrorHandler.ErrorCategory.DATA
                )
            }
            found
        }
    }

    /**
     * Delete a review. Returns whether it was deleted.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun deleteReview(reviewId: String): Boolean = runInViewModelScope {
        clearError()
        runCatchingNonCancellation(onFailure = {
            handleException(it, MR.strings.error_delete_review_failed)
            false
        }) {
            val found = manageReviewsUseCase.deleteReview(reviewId).getOrThrow()
            if (!found) {
                setError(
                    MR.strings.error_review_not_found_title,
                    MR.strings.error_review_delete_not_found,
                    ErrorHandler.ErrorCategory.DATA
                )
            }
            found
        }
    }

    // MARK: - Synchronous Helper Methods

    /**
     * Validate review input.
     */
    fun validateReview(rating: Float, comment: String): Boolean {
        val errorMessage = manageReviewsUseCase.validateReview(rating, comment)
        if (errorMessage != null) {
            setError(
                MR.strings.error_validation_title,
                errorMessage,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        return true
    }

    private fun validateUserName(userName: String): Boolean {
        if (userName.isBlank()) {
            setError(
                MR.strings.error_invalid_user_title,
                MR.strings.error_review_user_required,
                ErrorHandler.ErrorCategory.DATA
            )
        }
        return userName.isNotBlank()
    }

    /** Re-derives the per-cocktail slices from the grouped store. */
    private fun ReviewUiState.withReviews(grouped: Map<String, List<Review>>): ReviewUiState {
        val currentReviews = currentCocktailId
            ?.let { grouped[it].orEmpty() }
            .orEmpty()
            .sortedByDescending { it.date }
        return copy(
            reviews = grouped,
            currentCocktailReviews = currentReviews,
            reviewCount = currentReviews.size,
            averageRating = manageReviewsUseCase.computeAverageRating(currentReviews),
            isLoading = false
        )
    }
}
