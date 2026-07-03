package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Review
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class ManageReviewsUseCase {

    suspend fun submitReview(
        cocktailId: String,
        rating: Float,
        comment: String,
        userName: String,
        existingReviews: Map<String, List<Review>>
    ): Map<String, List<Review>> {
        val review = Review(
            cocktailId = cocktailId,
            userName = userName.trim(),
            rating = rating,
            comment = comment.trim()
        )
        val updatedReviews = existingReviews.toMutableMap()
        val cocktailReviews = updatedReviews[cocktailId]?.toMutableList() ?: mutableListOf()
        cocktailReviews.add(review)
        updatedReviews[cocktailId] = cocktailReviews
        return updatedReviews
    }

    suspend fun updateReview(
        reviewId: String,
        rating: Float,
        comment: String,
        existingReviews: Map<String, List<Review>>
    ): Pair<Map<String, List<Review>>, Boolean> {
        val updatedReviews = existingReviews.toMutableMap()
        var found = false
        for ((cocktailId, reviewList) in updatedReviews) {
            val updatedList = reviewList.map { review ->
                if (review.id == reviewId) {
                    found = true
                    review.copy(rating = rating, comment = comment.trim(), date = getCurrentDate())
                } else {
                    review
                }
            }
            updatedReviews[cocktailId] = updatedList
        }
        return Pair(updatedReviews, found)
    }

    suspend fun deleteReview(
        reviewId: String,
        existingReviews: Map<String, List<Review>>
    ): Triple<Map<String, List<Review>>, Boolean, String?> {
        val updatedReviews = existingReviews.toMutableMap()
        var found = false
        var affectedCocktailId: String? = null
        for ((cocktailId, reviewList) in updatedReviews) {
            val filteredList = reviewList.filter { review ->
                if (review.id == reviewId) {
                    found = true
                    affectedCocktailId = cocktailId
                    false
                } else {
                    true
                }
            }
            updatedReviews[cocktailId] = filteredList
        }
        return Triple(updatedReviews, found, affectedCocktailId)
    }

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
        return "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
    }
}

