package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult

/**
 * Offline-mode preference and connectivity checks, extracted from the former
 * CocktailRepositoryImpl god class. The offline decision itself lives in
 * [OfflineModePolicy] (AR-7); this repository is the public API over it.
 */
internal class CocktailOfflineRepositoryImpl(
    private val offlineModePolicy: OfflineModePolicy,
    private val cocktailCache: CocktailCache,
    private val cacheManager: CocktailCacheManager,
    private val remote: CocktailRemoteDataSource
) : CocktailOfflineRepository {

    override suspend fun setOfflineMode(enabled: Boolean) =
        offlineModePolicy.setOfflineMode(enabled)

    override suspend fun isOfflineModeEnabled(): Boolean =
        offlineModePolicy.isOfflineModeEnabled()

    /** True when the user forced offline mode or the network is down. */
    override suspend fun isOffline(): Boolean = offlineModePolicy.isOffline()

    override suspend fun checkApiConnectivity(): Result<Boolean> {
        return runCatchingResult("Failed to check API connectivity") {
            Result.Success(if (isOfflineModeEnabled()) false else remote.ping())
        }
    }

    override suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> {
        return runCatchingResult("Failed to get recently viewed cocktails") {
            Result.Success(cocktailCache.getRecentlyViewedCocktails())
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return runCatchingResult("Failed to clear cache") {
            // Both layers must be purged together (SH-3/SH-11): the persistent
            // Settings-backed cache and the volatile in-memory one — otherwise
            // the cleared data reappears on the next refresh.
            cocktailCache.clearCache()
            cacheManager.clearDataCaches()
            Result.Success(Unit)
        }
    }
}
