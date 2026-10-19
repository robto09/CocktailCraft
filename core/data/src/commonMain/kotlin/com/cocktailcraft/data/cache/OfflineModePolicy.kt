package com.cocktailcraft.data.cache

import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.first

/**
 * Single source of truth for "is the app offline" (AR-7): the network is
 * currently down.
 *
 * [CocktailCache] (expiry decision) and CocktailOfflineRepositoryImpl (the
 * public offline API) both depend on this instead of independently re-deriving
 * the answer from raw NetworkMonitor reads. Deliberately depends on nothing
 * above NetworkMonitor so the DI graph stays acyclic.
 */
internal class OfflineModePolicy(private val networkMonitor: NetworkMonitor) {
    /** True when the network is currently down. */
    suspend fun isOffline(): Boolean = !networkMonitor.isOnline.first()
}
