package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.model.ReviewValidationError
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.native.HiddenFromObjC
import kotlin.time.Clock

@HiddenFromObjC // Public for :presentation; not Swift API
class ManageReviewsUseCase(private val repository: ReviewRepository) {

    fun observeReviews(): Flow<List<Review>> = repository.observeReviews()

    suspend fun submitReview(
        cocktailId: String,
        rating: Float,
        comment: String,
        userName: String
    ): Result<Review> {
        val review = Review(
            cocktailId = cocktailId,
            userName = userName.trim(),
            rating = rating,
            comment = comment.trim()
        )
        return repository.addReview(review).map { review }
    }

    /** Returns false when the current user has no review with [reviewId]. */
    suspend fun updateReview(reviewId: String, rating: Float, comment: String): Result<Boolean> =
        repository.updateReview(reviewId, rating, comment.trim(), getCurrentDate())

    /** Returns false when the current user has no review with [reviewId]. */
    suspend fun deleteReview(reviewId: String): Result<Boolean> =
        repository.deleteReview(reviewId)

    fun computeAverageRating(reviews: List<Review>): Float = if (reviews.isNotEmpty()) {
        reviews.map {
            it.rating
        }.average().toFloat()
    } else {
        0.0f
    }

    /**
     * The rule the review breaks, or null when it is valid. The
     * presentation layer words it.
     */
    fun validateReview(rating: Float, comment: String): ReviewValidationError? = when {
        rating < MIN_RATING || rating > MAX_RATING -> ReviewValidationError.RATING_OUT_OF_RANGE
        comment.isBlank() -> ReviewValidationError.COMMENT_REQUIRED
        comment.length < MIN_COMMENT_LENGTH -> ReviewValidationError.COMMENT_TOO_SHORT
        comment.length > MAX_COMMENT_LENGTH -> ReviewValidationError.COMMENT_TOO_LONG
        else -> null
    }

    companion object {
        const val MIN_RATING = 1.0f
        const val MAX_RATING = 5.0f
        const val MIN_COMMENT_LENGTH = 10
        const val MAX_COMMENT_LENGTH = 500
    }

    private fun getCurrentDate(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
    }
}
