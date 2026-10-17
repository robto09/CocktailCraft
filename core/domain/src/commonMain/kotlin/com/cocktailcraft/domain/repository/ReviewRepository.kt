package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun observeReviews(): Flow<List<Review>>
    suspend fun getReviews(): Result<List<Review>>
    suspend fun addReview(review: Review): Result<Unit>

    /** Returns false when no review with [reviewId] exists. */
    suspend fun updateReview(reviewId: String, rating: Float, comment: String, date: String): Result<Boolean>

    /** Returns false when no review with [reviewId] exists. */
    suspend fun deleteReview(reviewId: String): Result<Boolean>
}
