package com.cocktailcraft.android.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cocktailcraft.domain.service.BackgroundSyncService
import java.util.concurrent.TimeUnit

/**
 * Periodic refresh of the offline cocktail cache. What "a sync" means
 * lives in the shared BackgroundSyncService (same definition iOS's
 * BGTaskScheduler path uses); this worker only decides when to run it.
 *
 * Constructed by [com.cocktailcraft.android.work.CocktailCraftWorkerFactory]
 * with its dependency injected — no global Koin lookup (AN-10).
 */
class BackgroundSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncService: BackgroundSyncService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // A skipped/failed sync just waits for the next period — the
        // network constraint already gates offline runs, so retry storms
        // buy nothing here.
        syncService.performSync(maxDurationMs = 60_000)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "background_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BackgroundSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
