package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

internal class ManageReviewsUseCase(
    private val repository: ReviewRepository
) {

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

    /** Returns false when no review with [reviewId] exists. */
    suspend fun updateReview(reviewId: String, rating: Float, comment: String): Result<Boolean> =
        repository.updateReview(reviewId, rating, comment.trim(), getCurrentDate())

    /** Returns false when no review with [reviewId] exists. */
    suspend fun deleteReview(reviewId: String): Result<Boolean> =
        repository.deleteReview(reviewId)

    fun computeAverageRating(reviews: List<Review>): Float {
        return if (reviews.isNotEmpty()) reviews.map { it.rating }.average().toFloat() else 0.0f
    }

    fun getRatingDistribution(reviews: List<Review>): Map<Int, Int> {
        val distribution = mutableMapOf<Int, Int>()
        for (i in 1..5) { distribution[i] = 0 }
        reviews.forEach { review ->
            val ratingInt = review.rating.toInt()
            distribution[ratingInt] = distribution[ratingInt]!! + 1
        }
        return distribution
    }

    fun validateReview(rating: Float, comment: String): String? {
        return when {
            rating < 1.0f || rating > 5.0f -> "Rating must be between 1 and 5 stars"
            comment.isBlank() -> "Please provide a comment for your review"
            comment.length < 10 -> "Review comment must be at least 10 characters long"
            comment.length > 500 -> "Review comment must be less than 500 characters"
            else -> null
        }
    }

    private fun getCurrentDate(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
    }
}
