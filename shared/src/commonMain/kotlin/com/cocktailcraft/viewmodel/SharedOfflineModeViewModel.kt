package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Offline Mode functionality.
 * Designed for full SKIE interoperability with iOS.
 * 
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedOfflineModeViewModel : SharedViewModel() {
    
    private val repository: CocktailRepository by inject()
    private val networkMonitor: NetworkMonitor by inject()
    
    // Core state - SKIE will convert these to Swift AsyncSequence
    private val _isOfflineModeEnabled = MutableStateFlow(false)
    val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()
    
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    private val _recentlyViewedCocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val recentlyViewedCocktails: StateFlow<List<Cocktail>> = _recentlyViewedCocktails.asStateFlow()
    
    private val _cacheSize = MutableStateFlow(0)
    val cacheSize: StateFlow<Int> = _cacheSize.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()
    
    // Computed properties
    val hasRecentlyViewed: Boolean
        get() = _recentlyViewedCocktails.value.isNotEmpty()
    
    val isOnlineAndOfflineModeDisabled: Boolean
        get() = _isNetworkAvailable.value && !_isOfflineModeEnabled.value
    
    init {
        initialize()
    }
    
    private fun initialize() {
        // Initialize offline mode state
        _isOfflineModeEnabled.value = repository.isOfflineModeEnabled()
        
        // Monitor network connectivity
        viewModelScope.launch {
            networkMonitor.startMonitoring()
            networkMonitor.isOnline.collectLatest { isOnline ->
                _isNetworkAvailable.value = isOnline
                
                // Auto-enable offline mode when network is lost
                if (!isOnline && !_isOfflineModeEnabled.value) {
                    setOfflineMode(true)
                }
                
                // Update sync time when network is restored
                if (isOnline && _isOfflineModeEnabled.value) {
                    updateLastSyncTime()
                }
            }
        }
        
        // Load initial data
        viewModelScope.launch {
            loadRecentlyViewedCocktails()
            updateCacheSize()
        }
    }
    
    /**
     * Toggle offline mode.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleOfflineMode() {
        setOfflineMode(!_isOfflineModeEnabled.value)
    }
    
    /**
     * Set offline mode enabled/disabled.
     * SKIE will convert this to Swift async function.
     */
    suspend fun setOfflineMode(enabled: Boolean) {
        try {
            _isOfflineModeEnabled.value = enabled
            repository.setOfflineMode(enabled)
            
            if (enabled) {
                // Cache current data when enabling offline mode
                syncCachedData()
            } else if (_isNetworkAvailable.value) {
                // Refresh data when disabling offline mode and network is available
                loadRecentlyViewedCocktails()
            }
            
            updateLastSyncTime()
        } catch (e: Exception) {
            handleException(e, "Failed to update offline mode", showAsEvent = true)
        }
    }
    
    /**
     * Sync cached data with remote server.
     * SKIE will convert this to Swift async function.
     */
    suspend fun syncCachedData() {
        if (!_isNetworkAvailable.value) {
            setError(
                "No Network Connection",
                "Cannot sync data without network connection",
                ErrorHandler.ErrorCategory.NETWORK,
                showAsEvent = true
            )
            return
        }
        
        setLoading(true)
        try {
            // Load fresh data from repository
            repository.getCocktailsSortedByNewest()
                .catch { e ->
                    handleException(e, "Failed to sync cached data")
                }
                .collect { cocktails ->
                    // Update cache size based on available cocktails
                    _cacheSize.value = cocktails.size
                    updateLastSyncTime()
                    setLoading(false)
                }
        } catch (e: Exception) {
            handleException(e, "Failed to sync cached data")
            setLoading(false)
        }
    }
    
    /**
     * Clear cached data.
     * SKIE will convert this to Swift async function.
     */
    suspend fun clearCache() {
        try {
            setLoading(true)
            
            // Clear recently viewed cocktails
            _recentlyViewedCocktails.value = emptyList()
            _cacheSize.value = 0
            _lastSyncTime.value = null
            
            setLoading(false)
        } catch (e: Exception) {
            handleException(e, "Failed to clear cache", showAsEvent = true)
            setLoading(false)
        }
    }
    
    /**
     * Load recently viewed cocktails.
     * SKIE will convert this to Swift async function.
     */
    suspend fun loadRecentlyViewedCocktails() {
        try {
            repository.getRecentlyViewedCocktails()
                .catch { e ->
                    if (!_isOfflineModeEnabled.value) {
                        handleException(e, "Failed to load recently viewed cocktails")
                    }
                }
                .collect { cocktails ->
                    _recentlyViewedCocktails.value = cocktails
                    _cacheSize.value = cocktails.size
                }
        } catch (e: Exception) {
            if (!_isOfflineModeEnabled.value) {
                handleException(e, "Failed to load recently viewed cocktails", showAsEvent = true)
            }
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
    /**
     * Get cached cocktail count.
     */
    fun getCachedCocktailCount(): Int {
        return _cacheSize.value
    }
    
    /**
     * Get network status as string.
     */
    fun getNetworkStatus(): String {
        return if (_isNetworkAvailable.value) "Connected" else "Disconnected"
    }
    
    /**
     * Get offline mode status as string.
     */
    fun getOfflineModeStatus(): String {
        return if (_isOfflineModeEnabled.value) "Enabled" else "Disabled"
    }
    
    /**
     * Get recently viewed cocktails by category.
     */
    fun getRecentlyViewedByCategory(category: String): List<Cocktail> {
        return _recentlyViewedCocktails.value.filter { it.category == category }
    }
    
    /**
     * Get recently viewed cocktails with limit.
     */
    fun getRecentlyViewedWithLimit(limit: Int): List<Cocktail> {
        return _recentlyViewedCocktails.value.take(limit)
    }
    
    /**
     * Check if offline mode is recommended based on network status.
     */
    fun isOfflineModeRecommended(): Boolean {
        return !_isNetworkAvailable.value
    }
    
    /**
     * Get cache status summary.
     */
    fun getCacheStatusSummary(): String {
        val count = _cacheSize.value
        val lastSync = _lastSyncTime.value ?: "Never"
        return "Cached: $count cocktails, Last sync: $lastSync"
    }
    
    /**
     * Refresh offline mode data.
     */
    fun refresh() {
        viewModelScope.launch {
            loadRecentlyViewedCocktails()
            updateCacheSize()
        }
    }
    
    // MARK: - Private Helper Methods
    
    private fun updateLastSyncTime() {
        // Simple timestamp - using a basic counter for now
        _lastSyncTime.value = "Just now"
    }
    
    private suspend fun updateCacheSize() {
        try {
            repository.getRecentlyViewedCocktails()
                .catch { /* Silent fail */ }
                .collect { cocktails ->
                    _cacheSize.value = cocktails.size
                }
        } catch (e: Exception) {
            // Silent fail for cache size update
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}