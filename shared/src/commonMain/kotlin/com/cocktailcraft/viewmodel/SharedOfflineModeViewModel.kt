package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.ManageOfflineModeUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.viewmodel.state.OfflineUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Offline Mode functionality.
 * Uses consolidated [OfflineUiState] for atomic state updates.
 */
class SharedOfflineModeViewModel : SharedViewModel() {

    private val manageOfflineModeUseCase: ManageOfflineModeUseCase by inject()
    private val networkMonitor: NetworkMonitor by inject()

    // Consolidated UI State
    private val _uiState = MutableStateFlow(OfflineUiState())
    val uiState: StateFlow<OfflineUiState> = _uiState.asStateFlow()

    // Derived StateFlows for backward compatibility
    val isOfflineModeEnabled: StateFlow<Boolean> = _uiState
        .map { it.isOfflineModeEnabled }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isNetworkAvailable: StateFlow<Boolean> = _uiState
        .map { it.isNetworkAvailable }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val recentlyViewedCocktails: StateFlow<List<Cocktail>> = _uiState
        .map { it.recentlyViewedCocktails }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val cacheSize: StateFlow<Int> = _uiState
        .map { it.cacheSize }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val lastSyncTime: StateFlow<String?> = _uiState
        .map { it.lastSyncTime }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val hasRecentlyViewed: Boolean
        get() = _uiState.value.recentlyViewedCocktails.isNotEmpty()
    val isOnlineAndOfflineModeDisabled: Boolean
        get() = _uiState.value.isNetworkAvailable && !_uiState.value.isOfflineModeEnabled

    init {
        initialize()
    }
    
    private fun initialize() {
        _uiState.update { it.copy(isOfflineModeEnabled = manageOfflineModeUseCase.isOfflineModeEnabled()) }

        viewModelScope.launch {
            networkMonitor.startMonitoring()
            networkMonitor.isOnline.collectLatest { isOnline ->
                _uiState.update { it.copy(isNetworkAvailable = isOnline) }
                if (!isOnline && !_uiState.value.isOfflineModeEnabled) {
                    setOfflineMode(true)
                }
                if (isOnline && _uiState.value.isOfflineModeEnabled) {
                    updateLastSyncTime()
                }
            }
        }

        viewModelScope.launch {
            loadRecentlyViewedCocktails()
            updateCacheSize()
        }
    }

    suspend fun toggleOfflineMode() {
        setOfflineMode(!_uiState.value.isOfflineModeEnabled)
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        try {
            _uiState.update { it.copy(isOfflineModeEnabled = enabled) }
            manageOfflineModeUseCase.setOfflineMode(enabled)

            if (enabled) {
                syncCachedData()
            } else if (_uiState.value.isNetworkAvailable) {
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
        if (!_uiState.value.isNetworkAvailable) {
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
            val cocktails = manageOfflineModeUseCase.syncCachedData().getOrDefault(emptyList())
            _uiState.update { it.copy(cacheSize = cocktails.size) }
            updateLastSyncTime()
            setLoading(false)
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
            
            _uiState.update { it.copy(recentlyViewedCocktails = emptyList(), cacheSize = 0, lastSyncTime = null) }
            
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
            val cocktails = manageOfflineModeUseCase.getRecentlyViewedCocktails().getOrDefault(emptyList())
            _uiState.update { it.copy(recentlyViewedCocktails = cocktails, cacheSize = cocktails.size) }
        } catch (e: Exception) {
            if (!_uiState.value.isOfflineModeEnabled) {
                handleException(e, "Failed to load recently viewed cocktails", showAsEvent = true)
            }
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
    /**
     * Get cached cocktail count.
     */
    fun getCachedCocktailCount(): Int = _uiState.value.cacheSize

    fun getNetworkStatus(): String =
        if (_uiState.value.isNetworkAvailable) "Connected" else "Disconnected"

    fun getOfflineModeStatus(): String =
        if (_uiState.value.isOfflineModeEnabled) "Enabled" else "Disabled"

    fun getRecentlyViewedByCategory(category: String): List<Cocktail> =
        _uiState.value.recentlyViewedCocktails.filter { it.category == category }

    fun getRecentlyViewedWithLimit(limit: Int): List<Cocktail> =
        _uiState.value.recentlyViewedCocktails.take(limit)

    fun isOfflineModeRecommended(): Boolean = !_uiState.value.isNetworkAvailable

    fun getCacheStatusSummary(): String {
        val count = _uiState.value.cacheSize
        val lastSync = _uiState.value.lastSyncTime ?: "Never"
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
        _uiState.update { it.copy(lastSyncTime = "Just now") }
    }

    private suspend fun updateCacheSize() {
        try {
            val cocktails = manageOfflineModeUseCase.getRecentlyViewedCocktails().getOrDefault(emptyList())
            _uiState.update { it.copy(cacheSize = cocktails.size) }
        } catch (e: Exception) {
            // Silent fail for cache size update
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        networkMonitor.stopMonitoring()
    }
}