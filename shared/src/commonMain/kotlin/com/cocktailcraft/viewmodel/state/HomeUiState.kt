package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters

/**
 * Consolidated UI state for the Home screen.
 * All state is updated atomically via copy(), eliminating intermediate inconsistent states.
 */
data class HomeUiState(
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
    val filterGlasses: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val currentPage: Int = 1,
    val hasMoreData: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isOfflineMode: Boolean = false,
    val isNetworkAvailable: Boolean = true
)

