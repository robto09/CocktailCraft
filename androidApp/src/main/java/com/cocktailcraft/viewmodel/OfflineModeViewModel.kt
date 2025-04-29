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
 */
class OfflineModeViewModel : BaseViewModel() {

    // Use cases
    private val offlineModeUseCase: OfflineModeUseCase by inject()
    private val networkStatusUseCase: NetworkStatusUseCase by inject()

    // Cache
    private val cocktailCache: CocktailCache by inject()

    // Offline mode state
    private val _isOfflineModeEnabled = MutableStateFlow(false)
    val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()

    // Network state
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    // Recently viewed cocktails
    private val _recentlyViewedCocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val recentlyViewedCocktails: StateFlow<List<Cocktail>> = _recentlyViewedCocktails.asStateFlow()

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
    fun toggleOfflineMode() {
        val newValue = !_isOfflineModeEnabled.value
        _isOfflineModeEnabled.value = newValue
        offlineModeUseCase.setOfflineMode(newValue)
    }

    /**
     * Set offline mode to a specific value.
     */
    fun setOfflineMode(enabled: Boolean) {
        _isOfflineModeEnabled.value = enabled
        offlineModeUseCase.setOfflineMode(enabled)
    }

    /**
     * Load recently viewed cocktails.
     */
    fun loadRecentlyViewedCocktails() {
        executeWithErrorHandling(
            operation = {
                offlineModeUseCase.getRecentlyViewedCocktails()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _recentlyViewedCocktails.value = result.data
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Failed to Load Recent Cocktails",
                                    message = result.message,
                                    category = com.cocktailcraft.util.ErrorUtils.ErrorCategory.DATA,
                                    recoveryAction = com.cocktailcraft.util.ErrorUtils.RecoveryAction("Retry") { loadRecentlyViewedCocktails() }
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to load recently viewed cocktails. Please try again.",
            recoveryAction = com.cocktailcraft.util.ErrorUtils.RecoveryAction("Retry") { loadRecentlyViewedCocktails() }
        )
    }

    /**
     * Clear the cache of all cocktails.
     */
    fun clearCache() {
        cocktailCache.clearCache()
        loadRecentlyViewedCocktails()
    }

    /**
     * Get the number of cached cocktails.
     */
    fun getCachedCocktailCount(): Int {
        return cocktailCache.getCachedCocktailCount()
    }

    override fun onCleared() {
        super.onCleared()
        networkStatusUseCase.stopMonitoring()
    }
}
