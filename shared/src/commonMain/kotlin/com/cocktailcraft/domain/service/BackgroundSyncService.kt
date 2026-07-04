package com.cocktailcraft.domain.service

import co.touchlab.kermit.Logger
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.util.NetworkMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Platform-agnostic background sync orchestration.
 *
 * There is exactly one definition of what "a sync" means, shared by both
 * platforms: the platform schedulers (BGTaskScheduler on iOS, WorkManager
 * on Android) only decide WHEN to call [performSync].
 */
class BackgroundSyncService(
    private val catalogRepository: CocktailCatalogRepository,
    private val networkMonitor: NetworkMonitor
) {
    private val log = Logger.withTag("BackgroundSyncService")

    /**
     * Refresh the offline cocktail cache.
     *
     * @param maxDurationMs hard time budget — iOS background tasks get
     *   roughly 30 seconds, so callers pass what they can afford.
     * @return true when the cache was refreshed, false when skipped
     *   (offline) or failed (error/timeout).
     */
    suspend fun performSync(maxDurationMs: Long = 25_000): Boolean {
        if (!networkMonitor.isOnline.value) {
            log.i { "Sync skipped: device is offline" }
            return false
        }
        return try {
            withTimeout(maxDurationMs) {
                val result = catalogRepository.getCocktailsSortedByNewest()
                val refreshed = result.isSuccess()
                log.i {
                    val count = result.getOrNull()?.size ?: 0
                    "Sync ${if (refreshed) "completed ($count cocktails cached)" else "failed"}"
                }
                refreshed
            }
        } catch (e: TimeoutCancellationException) {
            log.w { "Sync timed out after ${maxDurationMs}ms" }
            false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.w(e) { "Sync failed" }
            false
        }
    }
}
