package com.cocktailcraft.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * ViewModel for managing reviews.
 * Uses use cases instead of directly accessing repositories.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state for reviews
 * - Handles user interactions like adding reviews
 * - Provides a clean API for the UI layer
 * - Implements the IReviewViewModel interface for cross-platform compatibility
 */
class ReviewViewModel(
    private val manageReviewsUseCase: ManageReviewsUseCase
) : BaseViewModel(), IReviewViewModel {
    private val TAG = "ReviewViewModel"

    // Reviews state
    private val _reviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    override val reviews: StateFlow<Map<String, List<Review>>> = _reviews.asStateFlow()

    init {
        loadAllReviews()
    }

    /**
     * Load all reviews.
     */
    private fun loadAllReviews() {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageReviewsUseCase.getAllReviews(),
                onSuccess = { reviewsMap ->
                    _reviews.value = reviewsMap
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load reviews. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadAllReviews() }
            )
        }
    }

    /**
     * Get reviews for a specific cocktail.
     */
    override fun getReviewsForCocktail(cocktailId: String): List<Review> {
        return _reviews.value[cocktailId] ?: emptyList()
    }

    /**
     * Add a review.
     */
    override fun addReview(review: Review) {
        viewModelScope.launch {
            handleResultFlow(
                flow = manageReviewsUseCase.addReview(review),
                onSuccess = { _ ->
                    loadAllReviews() // Refresh reviews after adding
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to add review. Please try again."
            )
        }
    }

    /**
     * Get the average rating for a cocktail.
     */
    override fun getAverageRating(cocktailId: String): Float {
        val cocktailReviews = _reviews.value[cocktailId] ?: return 0f
        if (cocktailReviews.isEmpty()) return 0f
        return cocktailReviews.map { it.rating }.average().toFloat()
    }

    /**
     * Create and add a review.
     */
    override fun createAndAddReview(cocktailId: String, userName: String, rating: Float, comment: String) {
        Log.d(TAG, "Creating review for cocktail: $cocktailId, user: $userName")

        viewModelScope.launch {
            handleResultFlow(
                flow = manageReviewsUseCase.createReview(
                    cocktailId = cocktailId,
                    userName = userName,
                    rating = rating,
                    comment = comment
                ),
                onSuccess = { _ ->
                    Log.d(TAG, "Successfully created review object")
                    loadAllReviews() // Refresh reviews after adding
                    Log.d(TAG, "Successfully added review to the list")
                },
                onError = { error ->
                    Log.e(TAG, "Error creating review: ${error.message}")
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to create review. Please try again."
            )
        }
    }
}