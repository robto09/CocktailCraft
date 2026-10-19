package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
import com.cocktailcraft.data.cache.db.cocktailReview.SelectAll
import com.cocktailcraft.data.local.LocalDatabase
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.repository.ReviewRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Reviews in the cocktail_review table (see CocktailReview.sq).
 * [observeReviews] re-emits after every write.
 *
 * Everyone on the device (every user, and the guest) reads every review;
 * only a review's owner edits or deletes it, and [Review.isMine] marks the
 * current owner's (see UserDataOwnership.kt).
 */
internal class ReviewRepositoryImpl(
    private val localDatabase: LocalDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReviewRepository {

    private fun SelectAll.toReview() = Review(
        id = id,
        cocktailId = cocktail_id,
        userName = user_name,
        rating = rating.toFloat(),
        comment = comment,
        date = date,
        isMine = is_mine
    )

    private fun CocktailCraftDatabase.exists(ownerId: String, reviewId: String): Boolean =
        cocktailReviewQueries.exists(ownerId, reviewId).executeAsOne() > 0L

    override fun observeReviews(): Flow<List<Review>> =
        localDatabase.observe(ioDispatcher, { ownerId -> cocktailReviewQueries.selectAll(ownerId) }) { rows ->
            rows.map { it.toReview() }
        }

    override suspend fun getReviews(): Result<List<Review>> = withContext(ioDispatcher) {
        runCatchingResult("Failed to load reviews") {
            val ownerId = localDatabase.ownerId()
            val rows = localDatabase.get().cocktailReviewQueries.selectAll(ownerId).executeAsList()
            Result.Success(rows.map { it.toReview() })
        }
    }

    override suspend fun addReview(review: Review): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to add review") {
            localDatabase.get().cocktailReviewQueries.insert(
                ownerId = localDatabase.ownerId(),
                id = review.id,
                cocktailId = review.cocktailId,
                userName = review.userName,
                rating = review.rating.toDouble(),
                comment = review.comment,
                date = review.date
            )
            Result.Success(Unit)
        }
    }

    override suspend fun updateReview(
        reviewId: String,
        rating: Float,
        comment: String,
        date: String
    ): Result<Boolean> = withContext(ioDispatcher) {
        runCatchingResult("Failed to update review") {
            val db = localDatabase.get()
            val ownerId = localDatabase.ownerId()
            val found = db.transactionWithResult {
                db.exists(ownerId, reviewId).also { found ->
                    if (found) {
                        db.cocktailReviewQueries.update(
                            rating = rating.toDouble(),
                            comment = comment,
                            date = date,
                            ownerId = ownerId,
                            id = reviewId
                        )
                    }
                }
            }
            Result.Success(found)
        }
    }

    override suspend fun deleteReview(reviewId: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatchingResult("Failed to delete review") {
            val db = localDatabase.get()
            val ownerId = localDatabase.ownerId()
            val removed = db.transactionWithResult {
                db.exists(ownerId, reviewId).also { found ->
                    if (found) db.cocktailReviewQueries.deleteById(ownerId, reviewId)
                }
            }
            Result.Success(removed)
        }
    }
}
