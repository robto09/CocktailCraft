package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult

/**
 * Connectivity state and cached-data access, extracted from the former
 * CocktailRepositoryImpl god class. The offline decision itself lives in
 * [OfflineModePolicy]; this repository is the public API over it.
 */
internal class CocktailOfflineRepositoryImpl(
    private val offlineModePolicy: OfflineModePolicy,
    private val cocktailCache: CocktailCache
) : CocktailOfflineRepository {

    override suspend fun isOffline(): Boolean = offlineModePolicy.isOffline()

    override suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> = runCatchingResult(
        "Failed to get recently viewed cocktails"
    ) {
        Result.Success(cocktailCache.getRecentlyViewedCocktails())
    }
}
