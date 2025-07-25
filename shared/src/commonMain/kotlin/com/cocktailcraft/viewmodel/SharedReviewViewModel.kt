package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Shared ViewModel for Review functionality.
 * Designed for full SKIE interoperability with iOS.
 * 
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 * 
 * Note: This implementation uses in-memory storage for reviews.
 * In a production app, this would be backed by a ReviewRepository.
 */
class SharedReviewViewModel : SharedViewModel() {
    
    // Review state - SKIE will convert these to Swift AsyncSequence
    private val _reviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    val reviews: StateFlow<Map<String, List<Review>>> = _reviews.asStateFlow()
    
    private val _currentCocktailReviews = MutableStateFlow<List<Review>>(emptyList())
    val currentCocktailReviews: StateFlow<List<Review>> = _currentCocktailReviews.asStateFlow()
    
    private val _averageRating = MutableStateFlow(0.0f)
    val averageRating: StateFlow<Float> = _averageRating.asStateFlow()
    
    private val _reviewCount = MutableStateFlow(0)
    val reviewCount: StateFlow<Int> = _reviewCount.asStateFlow()
    
    private val _currentCocktailId = MutableStateFlow<String?>(null)
    val currentCocktailId: StateFlow<String?> = _currentCocktailId.asStateFlow()
    
    // Computed properties
    val hasReviews: Boolean
        get() = _currentCocktailReviews.value.isNotEmpty()
    
    val isEmpty: Boolean
        get() = _currentCocktailReviews.value.isEmpty()
    
    /**
     * Load reviews for a specific cocktail.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadReviewsForCocktail(cocktailId: String) {
        try {
            setLoading(true)
            _currentCocktailId.value = cocktailId
            
            val cocktailReviews = _reviews.value[cocktailId] ?: emptyList()
            _currentCocktailReviews.value = cocktailReviews.sortedByDescending { it.date }
            _reviewCount.value = cocktailReviews.size
            
            // Calculate average rating
            if (cocktailReviews.isNotEmpty()) {
                _averageRating.value = cocktailReviews.map { it.rating }.average().toFloat()
            } else {
                _averageRating.value = 0.0f
            }
            
            setLoading(false)
        } catch (e: Exception) {
            handleException(e, "Failed to load reviews")
            setLoading(false)
        }
    }
    
    /**
     * Submit a new review for a cocktail.
     * SKIE will convert this to Swift async function.
     */
    suspend fun submitReview(cocktailId: String, rating: Float, comment: String, userName: String): Boolean {
        if (!validateReview(rating, comment)) {
            return false
        }
        
        if (userName.isBlank()) {
            setError(
                "Invalid User",
                "User name is required to submit a review",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        try {
            setLoading(true)
            
            val review = Review(
                cocktailId = cocktailId,
                userName = userName.trim(),
                rating = rating,
                comment = comment.trim()
            )
            
            // Add review to the map
            val currentReviews = _reviews.value.toMutableMap()
            val cocktailReviews = currentReviews[cocktailId]?.toMutableList() ?: mutableListOf()
            cocktailReviews.add(review)
            currentReviews[cocktailId] = cocktailReviews
            _reviews.value = currentReviews
            
            // Refresh current cocktail reviews if it matches
            if (_currentCocktailId.value == cocktailId) {
                loadReviewsForCocktail(cocktailId)
            }
            
            setLoading(false)
            return true
        } catch (e: Exception) {
            handleException(e, "Failed to submit review", showAsEvent = true)
            setLoading(false)
            return false
        }
    }
    
    /**
     * Update an existing review.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateReview(reviewId: String, rating: Float, comment: String): Boolean {
        if (!validateReview(rating, comment)) {
            return false
        }
        
        try {
            setLoading(true)
            
            val currentReviews = _reviews.value.toMutableMap()
            var reviewFound = false
            
            // Find and update the review
            for ((cocktailId, reviewList) in currentReviews) {
                val updatedList = reviewList.map { review ->
                    if (review.id == reviewId) {
                        reviewFound = true
                        review.copy(
                            rating = rating,
                            comment = comment.trim(),
                            date = getCurrentDate()
                        )
                    } else {
                        review
                    }
                }
                currentReviews[cocktailId] = updatedList
            }
            
            if (!reviewFound) {
                setError(
                    "Review Not Found",
                    "The review you're trying to update was not found",
                    ErrorHandler.ErrorCategory.DATA,
                    showAsEvent = true
                )
                setLoading(false)
                return false
            }
            
            _reviews.value = currentReviews
            
            // Refresh current cocktail reviews if needed
            _currentCocktailId.value?.let { cocktailId ->
                loadReviewsForCocktail(cocktailId)
            }
            
            setLoading(false)
            return true
        } catch (e: Exception) {
            handleException(e, "Failed to update review", showAsEvent = true)
            setLoading(false)
            return false
        }
    }
    
    /**
     * Delete a review.
     * SKIE will convert this to Swift async function.
     */
    suspend fun deleteReview(reviewId: String): Boolean {
        try {
            setLoading(true)
            
            val currentReviews = _reviews.value.toMutableMap()
            var reviewFound = false
            var affectedCocktailId: String? = null
            
            // Find and remove the review
            for ((cocktailId, reviewList) in currentReviews) {
                val filteredList = reviewList.filter { review ->
                    if (review.id == reviewId) {
                        reviewFound = true
                        affectedCocktailId = cocktailId
                        false
                    } else {
                        true
                    }
                }
                currentReviews[cocktailId] = filteredList
            }
            
            if (!reviewFound) {
                setError(
                    "Review Not Found",
                    "The review you're trying to delete was not found",
                    ErrorHandler.ErrorCategory.DATA,
                    showAsEvent = true
                )
                setLoading(false)
                return false
            }
            
            _reviews.value = currentReviews
            
            // Refresh current cocktail reviews if needed
            affectedCocktailId?.let { cocktailId ->
                if (_currentCocktailId.value == cocktailId) {
                    loadReviewsForCocktail(cocktailId)
                }
            }
            
            setLoading(false)
            return true
        } catch (e: Exception) {
            handleException(e, "Failed to delete review", showAsEvent = true)
            setLoading(false)
            return false
        }
    }
    
    /**
     * Load all reviews.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadAllReviews() {
        try {
            setLoading(true)
            
            // In a real implementation, this would load from a repository
            // For now, we just update the current state
            val allReviews = _reviews.value.values.flatten()
            
            setLoading(false)
        } catch (e: Exception) {
            handleException(e, "Failed to load all reviews")
            setLoading(false)
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
    /**
     * Get average rating for a cocktail.
     */
    fun getAverageRating(cocktailId: String): Float {
        val cocktailReviews = _reviews.value[cocktailId] ?: return 0.0f
        return if (cocktailReviews.isNotEmpty()) {
            cocktailReviews.map { it.rating }.average().toFloat()
        } else {
            0.0f
        }
    }
    
    /**
     * Get review count for a cocktail.
     */
    fun getReviewCount(cocktailId: String): Int {
        return _reviews.value[cocktailId]?.size ?: 0
    }
    
    /**
     * Get reviews for a specific cocktail.
     */
    fun getReviewsForCocktail(cocktailId: String): List<Review> {
        return _reviews.value[cocktailId]?.sortedByDescending { it.date } ?: emptyList()
    }
    
    /**
     * Validate review input.
     */
    fun validateReview(rating: Float, comment: String): Boolean {
        if (rating < 1.0f || rating > 5.0f) {
            setError(
                "Invalid Rating",
                "Rating must be between 1 and 5 stars",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        if (comment.isBlank()) {
            setError(
                "Invalid Comment",
                "Please provide a comment for your review",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        if (comment.length < 10) {
            setError(
                "Comment Too Short",
                "Review comment must be at least 10 characters long",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        if (comment.length > 500) {
            setError(
                "Comment Too Long",
                "Review comment must be less than 500 characters",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        return true
    }
    
    /**
     * Get reviews sorted by rating (highest first).
     */
    fun getReviewsSortedByRating(cocktailId: String): List<Review> {
        return getReviewsForCocktail(cocktailId).sortedByDescending { it.rating }
    }
    
    /**
     * Get reviews sorted by date (newest first).
     */
    fun getReviewsSortedByDate(cocktailId: String): List<Review> {
        return getReviewsForCocktail(cocktailId).sortedByDescending { it.date }
    }
    
    /**
     * Get rating distribution for a cocktail.
     */
    fun getRatingDistribution(cocktailId: String): Map<Int, Int> {
        val reviews = getReviewsForCocktail(cocktailId)
        val distribution = mutableMapOf<Int, Int>()
        
        for (i in 1..5) {
            distribution[i] = 0
        }
        
        reviews.forEach { review ->
            val ratingInt = review.rating.toInt()
            distribution[ratingInt] = distribution[ratingInt]!! + 1
        }
        
        return distribution
    }
    
    /**
     * Get recent reviews across all cocktails.
     */
    fun getRecentReviews(limit: Int = 10): List<Review> {
        return _reviews.value.values
            .flatten()
            .sortedByDescending { it.date }
            .take(limit)
    }
    
    /**
     * Search reviews by comment content.
     */
    fun searchReviews(query: String): List<Review> {
        if (query.isBlank()) return emptyList()
        
        return _reviews.value.values
            .flatten()
            .filter { review ->
                review.comment.contains(query, ignoreCase = true) ||
                review.userName.contains(query, ignoreCase = true)
            }
            .sortedByDescending { it.date }
    }
    
    /**
     * Refresh review data.
     */
    fun refresh() {
        viewModelScope.launch {
            _currentCocktailId.value?.let { cocktailId ->
                loadReviewsForCocktail(cocktailId)
            }
        }
    }
    
    // MARK: - Private Helper Methods
    
    private fun getCurrentDate(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
    }
}