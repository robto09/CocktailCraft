package com.cocktailcraft.data.cache

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Single source of truth for "is the app offline" (AR-7): either the user
 * forced offline mode, or the network is currently down.
 *
 * [CocktailCache] (expiry decision) and CocktailOfflineRepositoryImpl (the
 * public offline-mode API) both depend on this instead of independently
 * re-deriving the answer from raw Settings/NetworkMonitor reads. Deliberately
 * depends on nothing above Settings/AppConfig/NetworkMonitor so the DI graph
 * stays acyclic.
 */
internal class OfflineModePolicy(
    private val settings: Settings,
    private val appConfig: AppConfig,
    private val networkMonitor: NetworkMonitor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun isOfflineModeEnabled(): Boolean =
        withContext(ioDispatcher) { settings.getBoolean(appConfig.offlineModeEnabledKey, false) }

    suspend fun setOfflineMode(enabled: Boolean) {
        withContext(ioDispatcher) { settings.putBoolean(appConfig.offlineModeEnabledKey, enabled) }
    }

    /** True when the user forced offline mode or the network is currently down. */
    suspend fun isOffline(): Boolean =
        isOfflineModeEnabled() || !networkMonitor.isOnline.first()
}
