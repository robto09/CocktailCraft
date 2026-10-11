package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result

/**
 * Repository interface for connectivity state and cached-data access.
 * Part of the Interface Segregation split of CocktailRepository.
 */
interface CocktailOfflineRepository {
    suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>>

    /** True when the network is currently down. */
    suspend fun isOffline(): Boolean
}
