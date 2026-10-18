package com.cocktailcraft.android.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * Refreshes the home-screen widgets: periodically while any widget is placed
 * (scheduled from the receivers' onEnabled, cancelled from onDisabled once
 * none remain), and once on demand — when a widget is placed or first
 * rendered with no content, and when the app asks via
 * [requestFavoritesRefresh].
 *
 * All widget data writes go through [refreshRandomCocktailWidgets] and
 * [refreshFavoritesWidgets], which the widgets' own refresh buttons use too.
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val dataProvider: WidgetDataProvider by inject()

    override suspend fun doWork(): Result {
        val context = applicationContext
        // A periodic request enqueued by an older version (hourly, no
        // constraints, scheduled on every launch even with no widgets) has
        // no TAG: bring it to the current schedule — UPDATE keeps the unique
        // name, so later runs are the new request — or drop it entirely.
        if (TAG !in tags) {
            if (!hasPlacedWidgets(context)) {
                cancelAll(context)
                return Result.success()
            }
            schedulePeriodicRefresh(context)
        }

        val target = inputData.getString(KEY_TARGET) ?: TARGET_ALL
        // Each widget type refreshes even if the other fails. Cancellation
        // (WorkManager stopping the work) propagates out of either.
        val randomOk = target == TARGET_FAVORITES ||
            attempt("random cocktail") { refreshRandomCocktailWidgets(context, dataProvider) }
        val favoritesOk = target == TARGET_RANDOM ||
            attempt("favorites") { refreshFavoritesWidgets(context, dataProvider) }

        return when {
            randomOk && favoritesOk -> Result.success()
            runAttemptCount < MAX_RETRIES -> Result.retry()
            // Periodic work simply waits for its next period
            else -> Result.failure()
        }
    }

    private suspend fun attempt(widget: String, refresh: suspend () -> Boolean): Boolean =
        try {
            refresh()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Refreshing the $widget widgets failed", e)
            false
        }

    companion object {
        private const val LOG_TAG = "WidgetUpdateWorker"

        // Marks requests made by this version (see the migration in doWork)
        private const val TAG = "cocktailcraft_widget_refresh"
        // Same name as the old hourly request, so UPDATE/cancel cover it
        private const val PERIODIC_WORK_NAME = "widget_update_work"
        private const val ONE_TIME_WORK_PREFIX = "widget_refresh_now_"

        private const val KEY_TARGET = "target"
        private const val TARGET_ALL = "all"
        private const val TARGET_RANDOM = "random"
        private const val TARGET_FAVORITES = "favorites"

        // Matches the iOS Random Cocktail widget's ~4 hour timeline
        private const val UPDATE_INTERVAL_HOURS = 4L
        private const val MAX_RETRIES = 3

        private val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private val periodicConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        /**
         * Schedule the periodic refresh of both widget types. Called from
         * the receivers' onEnabled; UPDATE makes repeat calls (the second
         * widget type, a migrated old request) keep a single schedule.
         */
        fun schedulePeriodicRefresh(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(periodicConstraints)
                .setInputData(workDataOf(KEY_TARGET to TARGET_ALL))
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        /**
         * Load a random cocktail into the Random Cocktail widgets as soon as
         * the device is online — for a widget that was just placed and would
         * otherwise stay empty until the next periodic run.
         */
        fun requestRandomCocktailRefresh(context: Context) {
            // KEEP: placement and first render both ask; one fetch serves all
            enqueueOneTime(context, TARGET_RANDOM, ExistingWorkPolicy.KEEP, networkConstraints)
        }

        /**
         * Re-read the favorites into the Favorites widgets now. Call this
         * after the favorites change — from any thread; it only enqueues
         * work, and does nothing when no Favorites widget is placed.
         * Favorites are local data, so this runs offline too.
         */
        fun requestFavoritesRefresh(context: Context) {
            if (!hasPlacedWidgets(context, FavoritesWidgetReceiver::class.java)) return
            // REPLACE: a refresh already running may have read the old list
            enqueueOneTime(context, TARGET_FAVORITES, ExistingWorkPolicy.REPLACE, Constraints.NONE)
        }

        private fun enqueueOneTime(
            context: Context,
            target: String,
            policy: ExistingWorkPolicy,
            constraints: Constraints
        ) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_TARGET to target))
                .addTag(TAG)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(ONE_TIME_WORK_PREFIX + target, policy, request)
        }

        /**
         * Cancel only when no widget instances of EITHER type remain — both
         * widgets share the periodic refresh, so a receiver's onDisabled must
         * not tear it down for the other widget (AN-2).
         */
        suspend fun cancelIfNoWidgetsRemain(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val anyLeft = manager.getGlanceIds(RandomCocktailWidget::class.java).isNotEmpty() ||
                manager.getGlanceIds(FavoritesWidget::class.java).isNotEmpty()
            if (!anyLeft) cancelAll(context)
        }

        private fun cancelAll(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
            workManager.cancelAllWorkByTag(TAG)
        }

        private fun hasPlacedWidgets(context: Context): Boolean =
            hasPlacedWidgets(context, RandomCocktailWidgetReceiver::class.java) ||
                hasPlacedWidgets(context, FavoritesWidgetReceiver::class.java)

        private fun hasPlacedWidgets(context: Context, receiver: Class<*>): Boolean =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, receiver))
                .isNotEmpty()
    }
}

/**
 * Fetch a random cocktail and show it on [glanceIds] (default: every placed
 * Random Cocktail widget). Returns false when nothing could be loaded. A
 * failed fetch keeps the cocktail a widget already shows; the error state
 * replaces it only when it has nothing to show or [showErrorOnFailure] (an
 * explicit tap on refresh).
 */
internal suspend fun refreshRandomCocktailWidgets(
    context: Context,
    provider: WidgetDataProvider,
    glanceIds: List<GlanceId>? = null,
    showErrorOnFailure: Boolean = false
): Boolean {
    val ids = glanceIds
        ?: GlanceAppWidgetManager(context).getGlanceIds(RandomCocktailWidget::class.java)
    // No widgets placed: don't burn a network call (AN-2)
    if (ids.isEmpty()) return true

    val cocktail = provider.getRandomCocktail()
    val widget = RandomCocktailWidget()
    ids.forEach { glanceId ->
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[RandomCocktailKeys.IS_LOADING] = "false"
            if (cocktail != null) {
                prefs[RandomCocktailKeys.ID] = cocktail.id
                prefs[RandomCocktailKeys.NAME] = cocktail.name
                prefs[RandomCocktailKeys.CATEGORY] = cocktail.category ?: ""
                prefs.remove(RandomCocktailKeys.ERROR)
            } else if (showErrorOnFailure || prefs[RandomCocktailKeys.ID] == null) {
                prefs[RandomCocktailKeys.ERROR] = true
            }
        }
        widget.update(context, glanceId)
    }
    return cocktail != null
}

/**
 * Read the favorites and show them on [glanceIds] (default: every placed
 * Favorites widget). Returns false when the read failed; a widget then keeps
 * its list unless it has none or [showErrorOnFailure].
 */
internal suspend fun refreshFavoritesWidgets(
    context: Context,
    provider: WidgetDataProvider,
    glanceIds: List<GlanceId>? = null,
    showErrorOnFailure: Boolean = false
): Boolean {
    val ids = glanceIds
        ?: GlanceAppWidgetManager(context).getGlanceIds(FavoritesWidget::class.java)
    if (ids.isEmpty()) return true

    val favorites = provider.getFavoriteCocktails()
    val favoritesJson = favorites?.let { list ->
        FavoriteItem.toJsonArray(
            list.map { cocktail ->
                FavoriteItem(
                    id = cocktail.id,
                    name = cocktail.name,
                    category = cocktail.category ?: "",
                    imageUrl = cocktail.imageUrl ?: ""
                )
            }
        )
    }
    val widget = FavoritesWidget()
    ids.forEach { glanceId ->
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[FavoritesWidgetKeys.IS_LOADING] = "false"
            if (favorites != null && favoritesJson != null) {
                prefs[FavoritesWidgetKeys.FAVORITES_LIST] = favoritesJson
                prefs[FavoritesWidgetKeys.FAVORITES_COUNT] = favorites.size.toString()
                prefs.remove(FavoritesWidgetKeys.ERROR)
            } else if (showErrorOnFailure || prefs[FavoritesWidgetKeys.FAVORITES_LIST] == null) {
                prefs[FavoritesWidgetKeys.ERROR] = true
            }
        }
        widget.update(context, glanceId)
    }
    return favorites != null
}
