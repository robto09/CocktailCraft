package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.Cocktail

/**
 * Consolidated UI state for the Offline Mode screen.
 */
data class OfflineUiState(
    val isOfflineModeEnabled: Boolean = false,
    val isNetworkAvailable: Boolean = true,
    val recentlyViewedCocktails: List<Cocktail> = emptyList(),
    val cacheSize: Int = 0,
    val lastSyncTime: String? = null,
    val isLoading: Boolean = false
)

