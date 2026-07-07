package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Offline-mode preference and connectivity checks, extracted from the former
 * CocktailRepositoryImpl god class.
 */
internal class CocktailOfflineRepositoryImpl(
    private val settings: Settings,
    private val appConfig: AppConfig,
    private val networkMonitor: NetworkMonitor,
    private val cocktailCache: CocktailCache,
    private val remote: CocktailRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CocktailOfflineRepository {

    override fun setOfflineMode(enabled: Boolean) {
        settings.putBoolean(appConfig.offlineModeEnabledKey, enabled)
    }

    override fun isOfflineModeEnabled(): Boolean =
        settings.getBoolean(appConfig.offlineModeEnabledKey, false)

    /** True when the user forced offline mode or the network is down. */
    override suspend fun isOffline(): Boolean =
        withContext(ioDispatcher) { isOfflineModeEnabled() } || !networkMonitor.isOnline.first()

    override suspend fun checkApiConnectivity(): Result<Boolean> {
        return try {
            Result.Success(if (withContext(ioDispatcher) { isOfflineModeEnabled() }) false else remote.ping())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to check API connectivity")
        }
    }

    override suspend fun getRecentlyViewedCocktails(): Result<List<Cocktail>> {
        return try {
            Result.Success(cocktailCache.getRecentlyViewedCocktails())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get recently viewed cocktails")
        }
    }
}
