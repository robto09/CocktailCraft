package com.cocktailcraft.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {
    private val TAG = "ReviewViewModel"
    
    private val _reviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    val reviews: StateFlow<Map<String, List<Review>>> = _reviews.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // In a real app, you would load reviews from a database or API
        loadMockReviews()
    }
    
    private fun loadMockReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real app, this would be a call to a repository
                // For now, we'll just use some mock data
                val mockReviews = createMockReviews()
                _reviews.value = mockReviews.groupBy { it.cocktailId }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getReviewsForCocktail(cocktailId: String): List<Review> {
        return _reviews.value[cocktailId] ?: emptyList()
    }
    
    fun addReview(review: Review) {
        viewModelScope.launch {
            try {
                val currentReviews = _reviews.value.toMutableMap()
                val cocktailReviews = currentReviews[review.cocktailId]?.toMutableList() ?: mutableListOf()
                cocktailReviews.add(review)
                currentReviews[review.cocktailId] = cocktailReviews
                _reviews.value = currentReviews
            } catch (e: Exception) {
                // Safely handle any exceptions during review creation
                _error.value = "Error adding review: ${e.message}"
                e.printStackTrace()
            }
        }
    }
    
    private fun createMockReviews(): List<Review> {
        // In a real app, this would come from a database
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
    
    fun getAverageRating(cocktailId: String): Float {
        val cocktailReviews = _reviews.value[cocktailId] ?: return 0f
        if (cocktailReviews.isEmpty()) return 0f
        return cocktailReviews.map { it.rating }.average().toFloat()
    }
    
    // A safer method to create and add a review
    fun createAndAddReview(cocktailId: String, userName: String, rating: Float, comment: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creating review for cocktail: $cocktailId, user: $userName")
                
                // Create review with safe parameters
                val review = Review(
                    cocktailId = cocktailId,
                    userName = userName.trim(),
                    rating = rating.coerceIn(0f, 5f),
                    comment = comment.trim()
                )
                
                Log.d(TAG, "Successfully created review object")
                
                // Add to reviews list
                val currentReviews = _reviews.value.toMutableMap()
                val cocktailReviews = currentReviews[cocktailId]?.toMutableList() ?: mutableListOf()
                cocktailReviews.add(review)
                currentReviews[cocktailId] = cocktailReviews
                _reviews.value = currentReviews
                
                Log.d(TAG, "Successfully added review to the list")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding review", e)
                _error.value = "Error adding review: ${e.message}"
                e.printStackTrace()
            }
        }
    }
} 