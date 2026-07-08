package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result

/**
 * Repository interface for offline mode and connectivity operations.
 * Part of the Interface Segregation split of CocktailRepository.
 */
interface CocktailOfflineRepository {
    suspend fun checkApiConnectivity(): Result<Boolean>
    suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>>
    suspend fun setOfflineMode(enabled: Boolean)
    suspend fun isOfflineModeEnabled(): Boolean

    /** True when the user forced offline mode or the network is currently down. */
    suspend fun isOffline(): Boolean
}

