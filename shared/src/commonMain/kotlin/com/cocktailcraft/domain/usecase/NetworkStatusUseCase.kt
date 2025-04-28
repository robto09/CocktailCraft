package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for managing network status and offline mode.
 * This use case handles the business logic of network operations,
 * including error handling and result transformation.
 */
class NetworkStatusUseCase(
    private val cocktailRepository: CocktailRepository,
    private val networkMonitor: NetworkMonitor
) {
    /**
     * Check if the API is reachable.
     * @return Flow of Result containing either a boolean or an error
     */
    suspend fun isApiReachable(): Flow<Result<Boolean>> {
        return cocktailRepository.checkApiConnectivity()
            .map { isReachable -> Result.Success(isReachable) as Result<Boolean> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
    }
    
    /**
     * Get the current network availability.
     * @return Flow of network availability status
     */
    fun getNetworkAvailability(): Flow<Boolean> {
        return networkMonitor.isOnline
    }
    
    /**
     * Check if offline mode is enabled.
     * @return Whether offline mode is enabled
     */
    fun isOfflineModeEnabled(): Boolean {
        return cocktailRepository.isOfflineModeEnabled()
    }
    
    /**
     * Set offline mode.
     * @param enabled Whether to enable offline mode
     */
    fun setOfflineMode(enabled: Boolean) {
        cocktailRepository.setOfflineMode(enabled)
    }
    
    /**
     * Start monitoring network connectivity.
     */
    fun startMonitoring() {
        networkMonitor.startMonitoring()
    }
    
    /**
     * Stop monitoring network connectivity.
     */
    fun stopMonitoring() {
        networkMonitor.stopMonitoring()
    }
}
