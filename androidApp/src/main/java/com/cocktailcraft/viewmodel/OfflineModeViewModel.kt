package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.usecase.NetworkStatusUseCase
import com.cocktailcraft.domain.usecase.OfflineModeUseCase
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * ViewModel for the offline mode screen.
 * Uses use cases instead of directly accessing repositories.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state for offline mode and network availability
 * - Handles user interactions like toggling offline mode
 * - Provides a clean API for the UI layer
 * - Implements the IOfflineModeViewModel interface for cross-platform compatibility
 */
class OfflineModeViewModel(
    private val offlineModeUseCase: OfflineModeUseCase,
    private val networkStatusUseCase: NetworkStatusUseCase
) : BaseViewModel(), IOfflineModeViewModel {

    // Cache - still injected since it's not part of the clean architecture boundary
    // In a future refactoring, this could be moved to a use case
    private val cocktailCache: CocktailCache by inject()

    // Offline mode state
    private val _isOfflineModeEnabled = MutableStateFlow(false)
    override val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()

    // Network state
    private val _isNetworkAvailable = MutableStateFlow(true)
    override val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    // Recently viewed cocktails
    private val _recentlyViewedCocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    override val recentlyViewedCocktails: StateFlow<List<Cocktail>> = _recentlyViewedCocktails.asStateFlow()

    init {
        // Initialize with current offline mode setting
        _isOfflineModeEnabled.value = offlineModeUseCase.isOfflineModeEnabled()

        // Monitor network connectivity
        viewModelScope.launch {
            networkStatusUseCase.startMonitoring()
            networkStatusUseCase.getNetworkAvailability().collectLatest { isOnline ->
                _isNetworkAvailable.value = isOnline
            }
        }

        // Load recently viewed cocktails
        loadRecentlyViewedCocktails()
    }

    /**
     * Toggle offline mode on/off.
     */
    override fun toggleOfflineMode() {
        val newValue = !_isOfflineModeEnabled.value
        _isOfflineModeEnabled.value = newValue
        viewModelScope.launch {
            offlineModeUseCase.setOfflineMode(newValue)
        }
    }

    /**
     * Set offline mode to a specific value.
     */
    override fun setOfflineMode(enabled: Boolean) {
        _isOfflineModeEnabled.value = enabled
        viewModelScope.launch {
            offlineModeUseCase.setOfflineMode(enabled)
        }
    }

    /**
     * Load recently viewed cocktails.
     */
    override fun loadRecentlyViewedCocktails() {
        viewModelScope.launch {
            handleResultFlow(
                flow = offlineModeUseCase.getRecentlyViewedCocktails(),
                onSuccess = { cocktails ->
                    _recentlyViewedCocktails.value = cocktails
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load recently viewed cocktails. Please try again.",
                recoveryAction = com.cocktailcraft.util.ErrorUtils.RecoveryAction("Retry") { loadRecentlyViewedCocktails() }
            )
        }
    }

    /**
     * Clear the cache of all cocktails.
     */
    override fun clearCache() {
        viewModelScope.launch {
            cocktailCache.clearCache()
            loadRecentlyViewedCocktails()
        }
    }

    /**
     * Get the number of cached cocktails.
     */
    override fun getCachedCocktailCount(): Int {
        return cocktailCache.getCachedCocktailCount()
    }

    override fun onCleared() {
        super.onCleared()
        networkStatusUseCase.stopMonitoring()
    }
}
