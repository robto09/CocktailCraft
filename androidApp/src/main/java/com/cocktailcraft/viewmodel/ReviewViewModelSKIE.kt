package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedReviewViewModel using SKIE.
 * This replaces the old ReviewViewModel with a thin wrapper around the shared implementation.
 * 
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class ReviewViewModelSKIE : BaseViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedReviewViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val reviews: StateFlow<Map<String, List<Review>>> = sharedViewModel.reviews
    val currentCocktailReviews: StateFlow<List<Review>> = sharedViewModel.currentCocktailReviews
    val averageRating: StateFlow<Float> = sharedViewModel.averageRating
    val reviewCount: StateFlow<Int> = sharedViewModel.reviewCount
    val currentCocktailId: StateFlow<String?> = sharedViewModel.currentCocktailId
    
    // Expose loading and error from shared base class
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    val errorEventState = sharedViewModel.errorEvent
    
    // Computed properties
    val hasReviews: Boolean
        get() = sharedViewModel.hasReviews
    
    val isEmpty: Boolean
        get() = sharedViewModel.isEmpty
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Submit a review using SKIE async interop
     */
    fun submitReview(
        cocktailId: String,
        rating: Float,
        comment: String,
        userName: String
    ) {
        viewModelScope.launch {
            sharedViewModel.submitReview(cocktailId, rating, comment, userName)
        }
    }
    
    /**
     * Load reviews for cocktail using SKIE async interop
     */
    fun loadReviewsForCocktail(cocktailId: String) {
        viewModelScope.launch {
            sharedViewModel.loadReviewsForCocktail(cocktailId)
        }
    }
    
    /**
     * Load all reviews using SKIE async interop
     */
    fun loadAllReviews() {
        viewModelScope.launch {
            sharedViewModel.loadAllReviews()
        }
    }
    
    /**
     * Update review using SKIE async interop
     */
    fun updateReview(
        reviewId: String,
        rating: Float,
        comment: String
    ) {
        viewModelScope.launch {
            sharedViewModel.updateReview(reviewId, rating, comment)
        }
    }
    
    /**
     * Delete review using SKIE async interop
     */
    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            sharedViewModel.deleteReview(reviewId)
        }
    }
    
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Get average rating for a cocktail
     */
    fun getAverageRating(cocktailId: String): Float {
        return sharedViewModel.getAverageRating(cocktailId)
    }
    
    /**
     * Get review count for a cocktail
     */
    fun getReviewCount(cocktailId: String): Int {
        return sharedViewModel.getReviewCount(cocktailId)
    }
    
    /**
     * Get reviews for a specific cocktail
     */
    fun getReviewsForCocktail(cocktailId: String): List<Review> {
        return sharedViewModel.getReviewsForCocktail(cocktailId)
    }
    
    /**
     * Validate review input
     */
    fun validateReview(rating: Float, comment: String): Boolean {
        return sharedViewModel.validateReview(rating, comment)
    }
    
    /**
     * Get reviews sorted by rating (highest first)
     */
    fun getReviewsSortedByRating(cocktailId: String): List<Review> {
        return sharedViewModel.getReviewsSortedByRating(cocktailId)
    }
    
    /**
     * Get reviews sorted by date (newest first)
     */
    fun getReviewsSortedByDate(cocktailId: String): List<Review> {
        return sharedViewModel.getReviewsSortedByDate(cocktailId)
    }
    
    /**
     * Get rating distribution for a cocktail
     */
    fun getRatingDistribution(cocktailId: String): Map<Int, Int> {
        return sharedViewModel.getRatingDistribution(cocktailId)
    }
    
    /**
     * Get recent reviews across all cocktails
     */
    fun getRecentReviews(limit: Int = 10): List<Review> {
        return sharedViewModel.getRecentReviews(limit)
    }
    
    /**
     * Search reviews by comment content
     */
    fun searchReviews(query: String): List<Review> {
        return sharedViewModel.searchReviews(query)
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Refresh review data
     */
    fun refresh() {
        sharedViewModel.refresh()
    }
    
    /**
     * Clean up when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        sharedViewModel.onCleared()
    }
}