package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlin.native.HiddenFromObjC

/**
 * Every review on the device, whoever wrote it; [Review.isMine] marks the
 * current user's (or the guest's) own, the only ones they may change.
 */
@HiddenFromObjC // Kotlin-side port, not Swift API: Swift reaches data through the ViewModels
interface ReviewRepository {
    fun observeReviews(): Flow<List<Review>>
    suspend fun getReviews(): Result<List<Review>>
    suspend fun addReview(review: Review): Result<Unit>

    /** Returns false when the current user has no review with [reviewId]. */
    suspend fun updateReview(reviewId: String, rating: Float, comment: String, date: String): Result<Boolean>

    /** Returns false when the current user has no review with [reviewId]. */
    suspend fun deleteReview(reviewId: String): Result<Boolean>
}
