package com.cocktailcraft.android.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.cocktailcraft.android.sync.BackgroundSyncWorker
import com.cocktailcraft.domain.service.BackgroundSyncService

/**
 * Koin-backed WorkerFactory (AN-10): workers get their dependencies through
 * the constructor instead of reaching into the global Koin context, so they
 * no longer depend on startup init ordering and are constructor-testable.
 *
 * Returning null falls back to WorkManager's default reflective factory —
 * that covers WidgetUpdateWorker, whose constructor takes no dependencies.
 */
class CocktailCraftWorkerFactory(
    private val backgroundSyncService: BackgroundSyncService
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        BackgroundSyncWorker::class.java.name ->
            BackgroundSyncWorker(appContext, workerParameters, backgroundSyncService)
        else -> null
    }
}
