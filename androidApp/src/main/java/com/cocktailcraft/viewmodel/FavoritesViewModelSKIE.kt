package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedFavoritesViewModel using SKIE.
 * This replaces the old FavoritesViewModel with a thin wrapper around the shared implementation.
 * 
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class FavoritesViewModelSKIE : BaseViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedFavoritesViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val favorites: StateFlow<List<Cocktail>> = sharedViewModel.favorites
    val favoriteCount: StateFlow<Int> = sharedViewModel.favoriteCount
    
    // Expose loading and error from shared base class
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    val errorEventState = sharedViewModel.errorEvent
    
    // Computed properties
    val isEmpty: Boolean
        get() = sharedViewModel.isEmpty
    
    val hasItems: Boolean
        get() = sharedViewModel.hasItems
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Toggle favorite status for a cocktail using SKIE async interop
     */
    fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            sharedViewModel.toggleFavorite(cocktail)
        }
    }
    
    /**
     * Clear all favorites using SKIE async interop
     */
    fun clearAllFavorites() {
        viewModelScope.launch {
            sharedViewModel.clearAllFavorites()
        }
    }
    
    /**
     * Load favorites using SKIE async interop
     */
    fun loadFavorites() {
        viewModelScope.launch {
            sharedViewModel.loadFavorites()
        }
    }
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Check if a cocktail is favorite
     */
    fun isFavorite(cocktailId: String): Boolean {
        return sharedViewModel.isFavorite(cocktailId)
    }
    
    /**
     * Get favorites by category
     */
    fun getFavoritesByCategory(category: String): List<Cocktail> {
        return sharedViewModel.getFavoritesByCategory(category)
    }
    
    /**
     * Get favorite categories
     */
    fun getFavoriteCategories(): List<String> {
        return sharedViewModel.getFavoriteCategories()
    }
    
    /**
     * Search favorites by name
     */
    fun searchFavorites(query: String): List<Cocktail> {
        return sharedViewModel.searchFavorites(query)
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Refresh favorites data
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