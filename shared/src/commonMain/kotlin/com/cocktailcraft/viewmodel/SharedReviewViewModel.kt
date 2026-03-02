package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.domain.usecase.ManageReviewsUseCase
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.ReviewUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Review functionality.
 * Uses consolidated [ReviewUiState] for atomic state updates.
 */
class SharedReviewViewModel : SharedViewModel() {

    private val manageReviewsUseCase: ManageReviewsUseCase by inject()

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    // Derived StateFlows for backward compatibility
    val reviews: StateFlow<Map<String, List<Review>>> = _uiState
        .map { it.reviews }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val currentCocktailReviews: StateFlow<List<Review>> = _uiState
        .map { it.currentCocktailReviews }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val averageRating: StateFlow<Float> = _uiState
        .map { it.averageRating }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0f)
    val reviewCount: StateFlow<Int> = _uiState
        .map { it.reviewCount }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val currentCocktailId: StateFlow<String?> = _uiState
        .map { it.currentCocktailId }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val hasReviews: Boolean
        get() = _uiState.value.currentCocktailReviews.isNotEmpty()
    val isEmpty: Boolean
        get() = _uiState.value.currentCocktailReviews.isEmpty()
    
    /**
     * Load reviews for a specific cocktail.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadReviewsForCocktail(cocktailId: String) {
        try {
            _uiState.update { it.copy(isLoading = true) }
            setLoading(true)

            val cocktailReviews = _uiState.value.reviews[cocktailId] ?: emptyList()
            _uiState.update { it.copy(
                currentCocktailId = cocktailId,
                currentCocktailReviews = cocktailReviews.sortedByDescending { r -> r.date },
                reviewCount = cocktailReviews.size,
                averageRating = manageReviewsUseCase.computeAverageRating(cocktailReviews),
                isLoading = false
            ) }
            setLoading(false)
        } catch (e: Exception) {
            handleException(e, "Failed to load reviews")
            _uiState.update { it.copy(isLoading = false) }
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

            val updated = manageReviewsUseCase.submitReview(cocktailId, rating, comment, userName, _uiState.value.reviews)
            _uiState.update { it.copy(reviews = updated) }

            if (_uiState.value.currentCocktailId == cocktailId) {
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

            val (updatedReviews, found) = manageReviewsUseCase.updateReview(reviewId, rating, comment, _uiState.value.reviews)

            if (!found) {
                setError(
                    "Review Not Found",
                    "The review you're trying to update was not found",
                    ErrorHandler.ErrorCategory.DATA,
                    showAsEvent = true
                )
                setLoading(false)
                return false
            }

            _uiState.update { it.copy(reviews = updatedReviews) }

            _uiState.value.currentCocktailId?.let { cocktailId ->
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

            val (updatedReviews, found, affectedCocktailId) = manageReviewsUseCase.deleteReview(reviewId, _uiState.value.reviews)

            if (!found) {
                setError(
                    "Review Not Found",
                    "The review you're trying to delete was not found",
                    ErrorHandler.ErrorCategory.DATA,
                    showAsEvent = true
                )
                setLoading(false)
                return false
            }

            _uiState.update { it.copy(reviews = updatedReviews) }

            affectedCocktailId?.let { cocktailId ->
                if (_uiState.value.currentCocktailId == cocktailId) {
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
            val allReviews = _uiState.value.reviews.values.flatten()
            
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
        val cocktailReviews = _uiState.value.reviews[cocktailId] ?: return 0.0f
        return manageReviewsUseCase.computeAverageRating(cocktailReviews)
    }
    
    /**
     * Get review count for a cocktail.
     */
    fun getReviewCount(cocktailId: String): Int =
        _uiState.value.reviews[cocktailId]?.size ?: 0

    fun getReviewsForCocktail(cocktailId: String): List<Review> =
        _uiState.value.reviews[cocktailId]?.sortedByDescending { it.date } ?: emptyList()
    
    /**
     * Validate review input.
     */
    fun validateReview(rating: Float, comment: String): Boolean {
        val errorMessage = manageReviewsUseCase.validateReview(rating, comment)
        if (errorMessage != null) {
            setError(
                "Validation Error",
                errorMessage,
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
        return manageReviewsUseCase.getRatingDistribution(getReviewsForCocktail(cocktailId))
    }
    
    /**
     * Get recent reviews across all cocktails.
     */
    fun getRecentReviews(limit: Int = 10): List<Review> {
        return _uiState.value.reviews.values
            .flatten()
            .sortedByDescending { it.date }
            .take(limit)
    }
    
    /**
     * Search reviews by comment content.
     */
    fun searchReviews(query: String): List<Review> {
        if (query.isBlank()) return emptyList()
        
        return _uiState.value.reviews.values
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
            _uiState.value.currentCocktailId?.let { cocktailId ->
                loadReviewsForCocktail(cocktailId)
            }
        }
    }
    
}