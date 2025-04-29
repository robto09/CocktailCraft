package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for managing reviews.
 * This use case handles the business logic of review management,
 * including error handling and result transformation.
 *
 * Note: In a real app, this would interact with a ReviewRepository.
 * For now, it manages reviews in memory.
 */
class ManageReviewsUseCase {
    // In-memory storage of reviews
    private val reviews = mutableMapOf<String, MutableList<Review>>()

    init {
        // Initialize with mock data
        val mockReviews = createMockReviews()
        mockReviews.forEach { review ->
            val cocktailReviews = reviews.getOrPut(review.cocktailId) { mutableListOf() }
            cocktailReviews.add(review)
        }
    }

    /**
     * Get all reviews.
     * @return Flow of Result containing either a map of cocktail IDs to lists of reviews or an error
     */
    fun getAllReviews(): Flow<Result<Map<String, List<Review>>>> = flow {
        try {
            emit(Result.Loading)
            emit(Result.Success(reviews.toMap()))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to get reviews"))
        }
    }

    /**
     * Get reviews for a specific cocktail.
     * @param cocktailId The ID of the cocktail
     * @return Flow of Result containing either a list of reviews or an error
     */
    fun getReviewsForCocktail(cocktailId: String): Flow<Result<List<Review>>> = flow {
        try {
            emit(Result.Loading)
            val cocktailReviews = reviews[cocktailId] ?: emptyList()
            emit(Result.Success(cocktailReviews))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to get reviews for cocktail"))
        }
    }

    /**
     * Add a review.
     * @param review The review to add
     * @return Flow of Result containing either a success message or an error
     */
    fun addReview(review: Review): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)
            val cocktailReviews = reviews.getOrPut(review.cocktailId) { mutableListOf() }
            cocktailReviews.add(review)
            emit(Result.Success("Review added successfully"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to add review"))
        }
    }

    /**
     * Get the average rating for a cocktail.
     * @param cocktailId The ID of the cocktail
     * @return Flow of Result containing either the average rating or an error
     */
    fun getAverageRating(cocktailId: String): Flow<Result<Float>> = flow {
        try {
            emit(Result.Loading)
            val cocktailReviews = reviews[cocktailId] ?: emptyList()
            val averageRating = if (cocktailReviews.isEmpty()) {
                0f
            } else {
                cocktailReviews.map { it.rating }.average().toFloat()
            }
            emit(Result.Success(averageRating))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to calculate average rating"))
        }
    }

    /**
     * Create a review.
     * @param cocktailId The ID of the cocktail
     * @param userName The name of the user
     * @param rating The rating (0-5)
     * @param comment The review comment
     * @return Flow of Result containing either the created review or an error
     */
    fun createReview(
        cocktailId: String,
        userName: String,
        rating: Float,
        comment: String
    ): Flow<Result<Review>> = flow {
        try {
            emit(Result.Loading)

            // Validate input
            if (cocktailId.isBlank()) {
                throw IllegalArgumentException("Cocktail ID cannot be empty")
            }
            if (userName.isBlank()) {
                throw IllegalArgumentException("User name cannot be empty")
            }
            if (rating < 0f || rating > 5f) {
                throw IllegalArgumentException("Rating must be between 0 and 5")
            }

            // Create review
            val review = Review(
                cocktailId = cocktailId,
                userName = userName.trim(),
                rating = rating.coerceIn(0f, 5f),
                comment = comment.trim()
            )

            // Add to storage
            val cocktailReviews = reviews.getOrPut(cocktailId) { mutableListOf() }
            cocktailReviews.add(review)

            emit(Result.Success(review))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to create review"))
        }
    }

    /**
     * Create mock reviews for testing.
     */
    private fun createMockReviews(): List<Review> {
        return listOf(
            Review(
                cocktailId = "11007", // Margarita
                userName = "John",
                rating = 4.5f,
                comment = "Great classic cocktail, perfect balance of sweet and sour."
            ),
            Review(
                cocktailId = "11007", // Margarita
                userName = "Sarah",
                rating = 5.0f,
                comment = "My favorite! Always refreshing."
            ),
            Review(
                cocktailId = "11001", // Old Fashioned
                userName = "Mike",
                rating = 4.0f,
                comment = "Strong and smooth, a perfect evening drink."
            )
        )
    }
}
