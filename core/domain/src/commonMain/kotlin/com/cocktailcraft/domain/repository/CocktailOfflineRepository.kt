package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result
import kotlin.native.HiddenFromObjC

/**
 * Repository interface for connectivity state and cached-data access.
 * Part of the Interface Segregation split of CocktailRepository.
 */
@HiddenFromObjC // Kotlin-side port, not Swift API (AR-12): Swift reaches data through the ViewModels
interface CocktailOfflineRepository {
    suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>>

    /** True when the network is currently down. */
    suspend fun isOffline(): Boolean
}
