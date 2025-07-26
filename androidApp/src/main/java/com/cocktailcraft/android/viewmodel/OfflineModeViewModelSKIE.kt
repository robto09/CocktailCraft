package com.cocktailcraft.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.viewmodel.SharedOfflineModeViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedOfflineModeViewModel using SKIE.
 * This replaces the old OfflineModeViewModel with a thin wrapper around the shared implementation.
 * 
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class OfflineModeViewModelSKIE : ViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedOfflineModeViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val isOfflineModeEnabled: StateFlow<Boolean> = sharedViewModel.isOfflineModeEnabled
    val isNetworkAvailable: StateFlow<Boolean> = sharedViewModel.isNetworkAvailable
    val recentlyViewedCocktails: StateFlow<List<Cocktail>> = sharedViewModel.recentlyViewedCocktails
    val cacheSize: StateFlow<Int> = sharedViewModel.cacheSize
    val lastSyncTime: StateFlow<String?> = sharedViewModel.lastSyncTime
    
    // Expose loading and error from shared base class
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    val errorEventState = sharedViewModel.errorEvent
    
    // Computed properties
    val hasRecentlyViewed: Boolean
        get() = sharedViewModel.hasRecentlyViewed
    
    val isOnlineAndOfflineModeDisabled: Boolean
        get() = sharedViewModel.isOnlineAndOfflineModeDisabled
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Toggle offline mode using SKIE async interop
     */
    fun toggleOfflineMode() {
        viewModelScope.launch {
            sharedViewModel.toggleOfflineMode()
        }
    }
    
    /**
     * Set offline mode using SKIE async interop
     */
    fun setOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            sharedViewModel.setOfflineMode(enabled)
        }
    }
    
    /**
     * Sync cached data with server using SKIE async interop
     */
    fun syncCachedData() {
        viewModelScope.launch {
            sharedViewModel.syncCachedData()
        }
    }
    
    /**
     * Clear all cached data using SKIE async interop
     */
    fun clearCache() {
        viewModelScope.launch {
            sharedViewModel.clearCache()
        }
    }
    
    /**
     * Load recently viewed cocktails using SKIE async interop
     */
    fun loadRecentlyViewedCocktails() {
        viewModelScope.launch {
            sharedViewModel.loadRecentlyViewedCocktails()
        }
    }
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Get cached cocktail count
     */
    fun getCachedCocktailCount(): Int {
        return sharedViewModel.getCachedCocktailCount()
    }
    
    /**
     * Get network status as string
     */
    fun getNetworkStatus(): String {
        return sharedViewModel.getNetworkStatus()
    }
    
    /**
     * Get offline mode status as string
     */
    fun getOfflineModeStatus(): String {
        return sharedViewModel.getOfflineModeStatus()
    }
    
    /**
     * Get recently viewed cocktails by category
     */
    fun getRecentlyViewedByCategory(category: String): List<Cocktail> {
        return sharedViewModel.getRecentlyViewedByCategory(category)
    }
    
    /**
     * Get recently viewed cocktails with limit
     */
    fun getRecentlyViewedWithLimit(limit: Int): List<Cocktail> {
        return sharedViewModel.getRecentlyViewedWithLimit(limit)
    }
    
    /**
     * Check if offline mode is recommended
     */
    fun isOfflineModeRecommended(): Boolean {
        return sharedViewModel.isOfflineModeRecommended()
    }
    
    /**
     * Get cache status summary
     */
    fun getCacheStatusSummary(): String {
        return sharedViewModel.getCacheStatusSummary()
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Refresh offline mode data
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