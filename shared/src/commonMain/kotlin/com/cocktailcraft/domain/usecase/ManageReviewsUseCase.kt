package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.review_error_comment_required
import com.cocktailcraft.resources.review_error_comment_too_long
import com.cocktailcraft.resources.review_error_comment_too_short
import com.cocktailcraft.resources.review_error_rating_range
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
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

    /** The validation failure to show, or null when the review is valid. */
    fun validateReview(rating: Float, comment: String): StringDesc? {
        return when {
            rating < 1.0f || rating > 5.0f -> MR.strings.review_error_rating_range.desc()
            comment.isBlank() -> MR.strings.review_error_comment_required.desc()
            comment.length < MIN_COMMENT_LENGTH ->
                MR.strings.review_error_comment_too_short.format(MIN_COMMENT_LENGTH)
            comment.length > MAX_COMMENT_LENGTH ->
                MR.strings.review_error_comment_too_long.format(MAX_COMMENT_LENGTH)
            else -> null
        }
    }

    private companion object {
        const val MIN_COMMENT_LENGTH = 10
        const val MAX_COMMENT_LENGTH = 500
    }

    private fun getCurrentDate(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
    }
}
