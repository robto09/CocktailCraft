package com.cocktailcraft.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedHomeViewModel using SKIE.
 * This replaces the old HomeViewModel with a thin wrapper around the shared implementation.
 * 
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class HomeViewModelSKIE : ViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedHomeViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val cocktails: StateFlow<List<Cocktail>> = sharedViewModel.cocktails
    val favorites: StateFlow<List<Cocktail>> = sharedViewModel.favorites
    val isOfflineMode: StateFlow<Boolean> = sharedViewModel.isOfflineMode
    val isNetworkAvailable: StateFlow<Boolean> = sharedViewModel.isNetworkAvailable
    val searchQuery: StateFlow<String> = sharedViewModel.searchQuery
    val isSearchActive: StateFlow<Boolean> = sharedViewModel.isSearchActive
    val searchFilters: StateFlow<SearchFilters> = sharedViewModel.searchFilters
    val hasMoreData: StateFlow<Boolean> = sharedViewModel.hasMoreData
    val isLoadingMore: StateFlow<Boolean> = sharedViewModel.isLoadingMore
    val selectedCategory: StateFlow<String?> = sharedViewModel.selectedCategory
    
    // Expose loading and error from shared base class
    val isLoading: StateFlow<Boolean> = sharedViewModel.isLoading
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    val errorEventState = sharedViewModel.errorEvent
    val errorString: StateFlow<String> = sharedViewModel.errorString
    val isAdvancedSearchActive: StateFlow<Boolean> = sharedViewModel.isAdvancedSearchActive
    
    // Catalog repository for filter options (Android-specific; shared ViewModels use use cases)
    val catalogRepository: CocktailCatalogRepository by inject()
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Load cocktails using SKIE async interop
     */
    fun loadCocktails() {
        viewModelScope.launch {
            sharedViewModel.loadCocktails()
        }
    }
    
    /**
     * Load cocktails by category using SKIE async interop
     */
    fun loadCocktailsByCategory(category: String?) {
        viewModelScope.launch {
            sharedViewModel.loadCocktailsByCategory(category)
        }
    }
    
    /**
     * Search cocktails with debouncing using SKIE async interop
     */
    fun searchCocktails(query: String) {
        viewModelScope.launch {
            sharedViewModel.searchCocktails(query)
        }
    }
    
    /**
     * Load more cocktails for pagination using SKIE async interop
     */
    fun loadMoreCocktails() {
        viewModelScope.launch {
            sharedViewModel.loadMoreCocktails()
        }
    }
    
    /**
     * Update search filters using SKIE async interop
     */
    fun updateSearchFilters(filters: SearchFilters) {
        viewModelScope.launch {
            sharedViewModel.applyFilters(filters.category, filters.ingredient)
        }
    }
    
    /**
     * Toggle favorite status using SKIE async interop
     */
    fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            sharedViewModel.toggleFavorite(cocktail)
        }
    }
    
    /**
     * Sort cocktails by price using SKIE async interop
     */
    fun sortByPrice(ascending: Boolean) {
        viewModelScope.launch {
            sharedViewModel.sortByPrice(ascending)
        }
    }
    
    /**
     * Sort cocktails by rating using SKIE async interop
     */
    fun sortByRating() {
        viewModelScope.launch {
            sharedViewModel.sortByRating()
        }
    }
    
    /**
     * Sort cocktails by popularity using SKIE async interop
     */
    fun sortByPopularity() {
        viewModelScope.launch {
            sharedViewModel.sortByPopularity()
        }
    }
    
    /**
     * Get cocktail by ID using SKIE async interop
     */
    fun getCocktailById(id: String, callback: (Cocktail?) -> Unit) {
        viewModelScope.launch {
            val cocktail = sharedViewModel.getCocktailById(id)
            callback(cocktail)
        }
    }
    
    /**
     * Get cocktail by ID as a suspend function returning a StateFlow
     */
    fun getCocktailByIdAsFlow(id: String): StateFlow<Cocktail?> {
        val state = MutableStateFlow<Cocktail?>(null)
        viewModelScope.launch {
            state.value = sharedViewModel.getCocktailById(id)
        }
        return state.asStateFlow()
    }
    
    /**
     * Force refresh cocktail details
     */
    fun forceRefreshCocktailDetails(id: String) {
        viewModelScope.launch {
            sharedViewModel.refreshCocktailDetails(id)
        }
    }
    
    /**
     * Toggle search mode
     */
    fun toggleSearchMode(active: Boolean) {
        sharedViewModel.toggleSearchMode(active)
    }
    
    /**
     * Toggle advanced search mode
     */
    fun toggleAdvancedSearchMode(active: Boolean) {
        sharedViewModel.toggleAdvancedSearchMode(active)
    }
    
    /**
     * Clear search filters
     */
    fun clearSearchFilters() {
        sharedViewModel.clearSearchFilters()
    }
    
    /**
     * Clear legacy error
     */
    fun clearLegacyError() {
        sharedViewModel.clearError()
    }
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Check if a cocktail is favorite
     */
    fun isFavorite(cocktailId: String): Boolean {
        return sharedViewModel.isFavorite(cocktailId)
    }
    
    /**
     * Get available categories
     */
    fun getCategories(): List<String> {
        return sharedViewModel.getCategories()
    }
    
    /**
     * Get cocktails by category with limit
     */
    fun getCocktailsByCategory(category: String, limit: Int = 3): List<Cocktail> {
        return sharedViewModel.getCocktailsByCategory(category, limit)
    }
    
    /**
     * Set offline mode
     */
    fun setOfflineMode(enabled: Boolean) {
        sharedViewModel.setOfflineMode(enabled)
    }
    
    /**
     * Clear search and filters
     */
    fun clearSearch() {
        sharedViewModel.clearSearch()
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Retry last operation
     */
    fun retry() {
        sharedViewModel.retry()
    }
    
    /**
     * Clean up when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        sharedViewModel.onCleared()
    }
}