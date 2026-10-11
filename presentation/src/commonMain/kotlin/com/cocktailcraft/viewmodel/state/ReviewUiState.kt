package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Review

/**
 * Consolidated UI state for the Review screen.
 */
data class ReviewUiState(
    val reviews: Map<String, List<Review>> = emptyMap(),
    val currentCocktailReviews: List<Review> = emptyList(),
    val averageRating: Float = 0.0f,
    val reviewCount: Int = 0,
    val currentCocktailId: String? = null,
    val isLoading: Boolean = false
)

