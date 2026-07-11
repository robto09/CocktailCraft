package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OfflineModeViewModel : ViewModel(), KoinComponent {

    private val cocktailRepository: CocktailRepository by inject()
    private val networkMonitor: NetworkMonitor by inject()
    private val cocktailCache: CocktailCache by inject()

    private val _isOfflineModeEnabled = MutableStateFlow(false)
    val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val _recentlyViewedCocktails = MutableStateFlow<List<com.cocktailcraft.domain.model.Cocktail>>(emptyList())
    val recentlyViewedCocktails: StateFlow<List<com.cocktailcraft.domain.model.Cocktail>> = _recentlyViewedCocktails.asStateFlow()

    init {
        // Initialize with current offline mode setting
        _isOfflineModeEnabled.value = cocktailRepository.isOfflineModeEnabled()

        // Monitor network connectivity
        viewModelScope.launch {
            networkMonitor.startMonitoring()
            networkMonitor.isOnline.collectLatest { isOnline ->
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
        cocktailRepository.setOfflineMode(newValue)
    }

    /**
     * Set offline mode to a specific value.
     */
    fun setOfflineMode(enabled: Boolean) {
        _isOfflineModeEnabled.value = enabled
        cocktailRepository.setOfflineMode(enabled)
    }

    /**
     * Load recently viewed cocktails.
     */
    fun loadRecentlyViewedCocktails() {
        viewModelScope.launch {
            cocktailRepository.getRecentlyViewedCocktails().collectLatest { cocktails ->
                _recentlyViewedCocktails.value = cocktails
            }
        }
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
        networkMonitor.stopMonitoring()
    }
}
