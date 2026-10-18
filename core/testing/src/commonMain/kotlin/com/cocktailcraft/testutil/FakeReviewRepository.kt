package com.cocktailcraft.testutil

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory [ReviewRepository]. */
class FakeReviewRepository : ReviewRepository {
    private val reviews = MutableStateFlow<List<Review>>(emptyList())

    /** Snapshot of the stored reviews. */
    val stored: List<Review> get() = reviews.value

    override fun observeReviews(): Flow<List<Review>> = reviews.asStateFlow()
    override suspend fun getReviews(): Result<List<Review>> = Result.Success(reviews.value)

    override suspend fun addReview(review: Review): Result<Unit> {
        reviews.value = reviews.value + review
        return Result.Success(Unit)
    }

    override suspend fun updateReview(reviewId: String, rating: Float, comment: String, date: String): Result<Boolean> {
        if (reviews.value.none { it.id == reviewId }) return Result.Success(false)
        reviews.value = reviews.value.map {
            if (it.id == reviewId) it.copy(rating = rating, comment = comment, date = date) else it
        }
        return Result.Success(true)
    }

    override suspend fun deleteReview(reviewId: String): Result<Boolean> {
        if (reviews.value.none { it.id == reviewId }) return Result.Success(false)
        reviews.value = reviews.value.filterNot { it.id == reviewId }
        return Result.Success(true)
    }
}
