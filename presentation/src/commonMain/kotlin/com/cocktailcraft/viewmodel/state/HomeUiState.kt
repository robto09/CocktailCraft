package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailRatingSummary
import com.cocktailcraft.domain.model.SearchFilters

/**
 * Consolidated UI state for the Home screen.
 * All state is updated atomically via copy(), eliminating intermediate inconsistent states.
 */
data class HomeUiState(
    // Already ordered by [sortOption]
    val cocktails: List<Cocktail> = emptyList(),
    val favorites: List<Cocktail> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isAdvancedSearchActive: Boolean = false,
    val searchFilters: SearchFilters = SearchFilters(),
    // Option lists backing the advanced-search filter UI (loaded from list.php).
    val filterCategories: List<String> = emptyList(),
    val filterIngredients: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val currentPage: Int = 1,
    val hasMoreData: Boolean = true,
    val isNetworkAvailable: Boolean = true,
    // Kept across category changes, searches and catalog refreshes
    val sortOption: HomeSortOption = HomeSortOption.DEFAULT,
    // Most recently viewed first, at most SharedHomeViewModel.RECENTLY_VIEWED_LIMIT
    val recentlyViewed: List<Cocktail> = emptyList(),
    // Review averages by cocktail id, over every review on the device
    val ratingSummaries: Map<String, CocktailRatingSummary> = emptyMap()
) {
    /** The "Recently viewed" row shows when there is something in it and no search query. */
    val showRecentlyViewed: Boolean
        get() = recentlyViewed.isNotEmpty() && searchQuery.isBlank()
}
