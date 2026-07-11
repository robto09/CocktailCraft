package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.ReviewUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Review functionality.
 * Collects the persisted review store reactively, so submitted reviews
 * survive restarts and every state change flows through [uiState].
 */
class SharedReviewViewModel internal constructor(
    private val manageReviewsUseCase: ManageReviewsUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    val hasReviews: Boolean
        get() = _uiState.value.currentCocktailReviews.isNotEmpty()
    val isEmpty: Boolean
        get() = _uiState.value.currentCocktailReviews.isEmpty()

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
     * Load reviews for a specific cocktail.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadReviewsForCocktail(cocktailId: String) {
        _uiState.update { state ->
            state.copy(currentCocktailId = cocktailId).withReviews(state.reviews)
        }
    }

    /**
     * Submit a new review for a cocktail.
     * SKIE will convert this to Swift async function.
     */
    suspend fun submitReview(cocktailId: String, rating: Float, comment: String, userName: String): Boolean {
        if (!validateReview(rating, comment)) {
            return false
        }

        if (userName.isBlank()) {
            setError(
                "Invalid User",
                "User name is required to submit a review",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        return try {
            manageReviewsUseCase.submitReview(cocktailId, rating, comment, userName).getOrThrow()
            true
        } catch (e: Exception) {
            handleException(e, "Failed to submit review")
            false
        }
    }

    /**
     * Update an existing review.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateReview(reviewId: String, rating: Float, comment: String): Boolean {
        if (!validateReview(rating, comment)) {
            return false
        }

        return try {
            val found = manageReviewsUseCase.updateReview(reviewId, rating, comment).getOrThrow()
            if (!found) {
                setError(
                    "Review Not Found",
                    "The review you're trying to update was not found",
                    ErrorHandler.ErrorCategory.DATA
                )
            }
            found
        } catch (e: Exception) {
            handleException(e, "Failed to update review")
            false
        }
    }

    /**
     * Delete a review.
     * SKIE will convert this to Swift async function.
     */
    suspend fun deleteReview(reviewId: String): Boolean {
        return try {
            val found = manageReviewsUseCase.deleteReview(reviewId).getOrThrow()
            if (!found) {
                setError(
                    "Review Not Found",
                    "The review you're trying to delete was not found",
                    ErrorHandler.ErrorCategory.DATA
                )
            }
            found
        } catch (e: Exception) {
            handleException(e, "Failed to delete review")
            false
        }
    }

    // MARK: - Synchronous Helper Methods

    /**
     * Get average rating for a cocktail.
     */
    fun getAverageRating(cocktailId: String): Float {
        val cocktailReviews = _uiState.value.reviews[cocktailId] ?: return 0.0f
        return manageReviewsUseCase.computeAverageRating(cocktailReviews)
    }

    /**
     * Get review count for a cocktail.
     */
    fun getReviewCount(cocktailId: String): Int =
        _uiState.value.reviews[cocktailId]?.size ?: 0

    fun getReviewsForCocktail(cocktailId: String): List<Review> =
        _uiState.value.reviews[cocktailId]?.sortedByDescending { it.date } ?: emptyList()

    /**
     * Validate review input.
     */
    fun validateReview(rating: Float, comment: String): Boolean {
        val errorMessage = manageReviewsUseCase.validateReview(rating, comment)
        if (errorMessage != null) {
            setError(
                "Validation Error",
                errorMessage,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        return true
    }

    /**
     * Get rating distribution for a cocktail.
     */
    fun getRatingDistribution(cocktailId: String): Map<Int, Int> {
        return manageReviewsUseCase.getRatingDistribution(getReviewsForCocktail(cocktailId))
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
