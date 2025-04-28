package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Use case for handling offline mode operations.
 * This moves the offline mode business logic from the repository to the domain layer.
 */
class OfflineModeUseCase(
    private val cocktailRepository: CocktailRepository,
    private val networkMonitor: NetworkMonitor
) {
    /**
     * Check if the device is currently offline
     * @return True if offline, false otherwise
     */
    suspend fun isOffline(): Boolean {
        return cocktailRepository.isOfflineModeEnabled() || !networkMonitor.isOnline.first()
    }

    /**
     * Set offline mode to a specific value
     * @param enabled Whether offline mode should be enabled
     */
    fun setOfflineMode(enabled: Boolean) {
        cocktailRepository.setOfflineMode(enabled)
    }

    /**
     * Check if offline mode is enabled
     * @return True if offline mode is enabled, false otherwise
     */
    fun isOfflineModeEnabled(): Boolean {
        return cocktailRepository.isOfflineModeEnabled()
    }

    /**
     * Get recently viewed cocktails (useful in offline mode)
     * @return Flow of Result containing either a list of cocktails or an error
     */
    suspend fun getRecentlyViewedCocktails(): Flow<Result<List<Cocktail>>> {
        return cocktailRepository.getRecentlyViewedCocktails()
            .map { cocktails -> Result.Success(cocktails) as Result<List<Cocktail>> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Failed to get recently viewed cocktails"))
            }
    }

    /**
     * Check API connectivity
     * @return Flow of Result containing either a boolean indicating connectivity or an error
     */
    suspend fun checkApiConnectivity(): Flow<Result<Boolean>> {
        return cocktailRepository.checkApiConnectivity()
            .map { isConnected -> Result.Success(isConnected) as Result<Boolean> }
            .catch { e -> 
                emit(Result.Error(e.message ?: "Failed to check API connectivity"))
            }
    }
}