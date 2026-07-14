package com.cocktailcraft.android.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker for periodic widget updates.
 * Updates both Random Cocktail and Favorites widgets in the background.
 */
class WidgetUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                updateRandomCocktailWidgets()
                updateFavoritesWidgets()
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    private suspend fun updateRandomCocktailWidgets() {
        try {
            // Check for widget instances BEFORE fetching — an hourly run with
            // no widgets placed must not burn a network call (AN-2).
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(RandomCocktailWidget::class.java)
            if (glanceIds.isEmpty()) return

            val dataProvider = WidgetDataProvider.getInstance()
            val cocktail = dataProvider.getRandomCocktail()

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[RandomCocktailKeys.IS_LOADING] = "false"
                    if (cocktail != null) {
                        prefs[RandomCocktailKeys.ID] = cocktail.id
                        prefs[RandomCocktailKeys.NAME] = cocktail.name
                        prefs[RandomCocktailKeys.CATEGORY] = cocktail.category ?: ""
                        prefs.remove(RandomCocktailKeys.ERROR)
                    }
                }
            }

            RandomCocktailWidget().updateAll(context)
        } catch (e: Exception) {
            // Log error but don't fail the entire worker
        }
    }

    private suspend fun updateFavoritesWidgets() {
        try {
            // Same no-widgets short-circuit as the random widget path (AN-2).
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(FavoritesWidget::class.java)
            if (glanceIds.isEmpty()) return

            val dataProvider = WidgetDataProvider.getInstance()
            val favorites = dataProvider.getFavoriteCocktails()

            val favoriteItems = favorites.map { cocktail ->
                FavoriteItem(
                    id = cocktail.id,
                    name = cocktail.name,
                    category = cocktail.category ?: "",
                    imageUrl = cocktail.imageUrl ?: ""
                )
            }

            val favoritesJson = FavoriteItem.toJsonArray(favoriteItems)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[FavoritesWidgetKeys.IS_LOADING] = "false"
                    prefs[FavoritesWidgetKeys.FAVORITES_LIST] = favoritesJson
                    prefs[FavoritesWidgetKeys.FAVORITES_COUNT] = favorites.size.toString()
                    prefs.remove(FavoritesWidgetKeys.ERROR)
                }
            }

            FavoritesWidget().updateAll(context)
        } catch (e: Exception) {
            // Log error but don't fail the entire worker
        }
    }

    companion object {
        private const val WORK_NAME = "widget_update_work"
        private const val UPDATE_INTERVAL_HOURS = 1L

        /**
         * Schedule periodic widget updates.
         * Updates every hour by default.
         */
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_HOURS, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Cancel scheduled widget updates.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Cancel only when no widget instances of EITHER type remain — both
         * widgets share this one worker, so a receiver's onDisabled must not
         * tear down refresh for the other widget (AN-2).
         */
        suspend fun cancelIfNoWidgetsRemain(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val anyLeft = manager.getGlanceIds(RandomCocktailWidget::class.java).isNotEmpty() ||
                manager.getGlanceIds(FavoritesWidget::class.java).isNotEmpty()
            if (!anyLeft) cancel(context)
        }

        /**
         * Trigger an immediate widget update.
         */
        suspend fun updateNow(context: Context) {
            withContext(Dispatchers.IO) {
                updateAllWidgets(context)
            }
        }

        private suspend fun updateAllWidgets(context: Context) {
            try {
                val dataProvider = WidgetDataProvider.getInstance()

                // Update random cocktail widgets
                val cocktail = dataProvider.getRandomCocktail()
                val manager = GlanceAppWidgetManager(context)

                manager.getGlanceIds(RandomCocktailWidget::class.java).forEach { glanceId ->
                    updateAppWidgetState(context, glanceId) { prefs ->
                        if (cocktail != null) {
                            prefs[RandomCocktailKeys.ID] = cocktail.id
                            prefs[RandomCocktailKeys.NAME] = cocktail.name
                            prefs[RandomCocktailKeys.CATEGORY] = cocktail.category ?: ""
                        }
                    }
                }
                RandomCocktailWidget().updateAll(context)

                // Update favorites widgets
                val favorites = dataProvider.getFavoriteCocktails()
                val favoriteItems = favorites.map { c ->
                    FavoriteItem(c.id, c.name, c.category ?: "", c.imageUrl ?: "")
                }
                val favoritesJson = FavoriteItem.toJsonArray(favoriteItems)

                manager.getGlanceIds(FavoritesWidget::class.java).forEach { glanceId ->
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[FavoritesWidgetKeys.FAVORITES_LIST] = favoritesJson
                        prefs[FavoritesWidgetKeys.FAVORITES_COUNT] = favorites.size.toString()
                    }
                }
                FavoritesWidget().updateAll(context)
            } catch (e: Exception) {
                // Silent fail for widget updates
            }
        }
    }
}

