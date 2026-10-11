package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.db.CocktailCraftDatabase
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
 */
internal class ReviewRepositoryImpl(
    private val localDatabase: LocalDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReviewRepository {

    private fun toReview(
        id: String,
        cocktailId: String,
        userName: String,
        rating: Double,
        comment: String,
        date: String
    ) = Review(
        id = id,
        cocktailId = cocktailId,
        userName = userName,
        rating = rating.toFloat(),
        comment = comment,
        date = date
    )

    private fun CocktailCraftDatabase.exists(reviewId: String): Boolean =
        cocktailReviewQueries.exists(reviewId).executeAsOne() > 0L

    override fun observeReviews(): Flow<List<Review>> =
        localDatabase.observe(ioDispatcher, { cocktailReviewQueries.selectAll(::toReview) }) { it }

    override suspend fun getReviews(): Result<List<Review>> = withContext(ioDispatcher) {
        runCatchingResult("Failed to load reviews") {
            Result.Success(localDatabase.get().cocktailReviewQueries.selectAll(::toReview).executeAsList())
        }
    }

    override suspend fun addReview(review: Review): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to add review") {
            localDatabase.get().cocktailReviewQueries.insert(
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
            val found = db.transactionWithResult {
                db.exists(reviewId).also { found ->
                    if (found) {
                        db.cocktailReviewQueries.update(
                            rating = rating.toDouble(),
                            comment = comment,
                            date = date,
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
            val removed = db.transactionWithResult {
                db.exists(reviewId).also { found ->
                    if (found) db.cocktailReviewQueries.deleteById(reviewId)
                }
            }
            Result.Success(removed)
        }
    }
}
