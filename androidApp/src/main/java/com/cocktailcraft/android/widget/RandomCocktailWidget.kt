package com.cocktailcraft.android.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cocktailcraft.android.MainActivity
import com.cocktailcraft.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Random Cocktail Widget that displays a random cocktail from the API.
 * Supports manual refresh and opens app on tap.
 */
class RandomCocktailWidget : GlanceAppWidget() {
    
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CocktailWidgetTheme {
                RandomCocktailContent()
            }
        }
    }
    
    @Composable
    private fun RandomCocktailContent() {
        val prefs = currentState<Preferences>()
        val cocktailName = prefs[RandomCocktailKeys.NAME]
        val cocktailCategory = prefs[RandomCocktailKeys.CATEGORY] ?: ""
        val cocktailId = prefs[RandomCocktailKeys.ID] ?: ""
        val isLoading = prefs[RandomCocktailKeys.IS_LOADING] == "true"
        val hasError = prefs[RandomCocktailKeys.ERROR] == true
        
        // Deep-link to the shown cocktail's detail screen; fall back to a
        // plain app launch until a cocktail has been loaded
        val context = LocalContext.current
        val tapAction = if (cocktailId.isNotEmpty()) {
            actionStartActivity(cocktailDetailIntent(context, cocktailId))
        } else {
            actionStartActivity<MainActivity>()
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .clickable(tapAction),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title and refresh button
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = context.getString(R.string.widget_random_cocktail_header),
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    
                    // Refresh button
                    Box(
                        modifier = GlanceModifier
                            .size(32.dp)
                            .cornerRadius(16.dp)
                            .background(GlanceTheme.colors.primary)
                            .clickable(actionRunCallback<RefreshRandomCocktailAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = context.getString(R.string.widget_refresh_icon),
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimary,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                when {
                    isLoading -> {
                        LoadingContent()
                    }
                    hasError -> {
                        ErrorContent()
                    }
                    cocktailName == null -> {
                        EmptyContent()
                    }
                    else -> {
                        CocktailDisplayContent(
                            name = cocktailName,
                            category = cocktailCategory
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    private fun LoadingContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalContext.current.getString(R.string.loading),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp
                )
            )
        }
    }

    @Composable
    private fun ErrorContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalContext.current.getString(R.string.widget_error_icon),
                style = TextStyle(fontSize = 24.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = LocalContext.current.getString(R.string.widget_tap_refresh_to_retry),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp
                )
            )
        }
    }

    @Composable
    private fun EmptyContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = LocalContext.current.getString(R.string.widget_cocktail_glass_icon),
                style = TextStyle(fontSize = 32.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = LocalContext.current.getString(R.string.widget_tap_refresh_to_discover),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp
                )
            )
        }
    }

    @Composable
    private fun CocktailDisplayContent(name: String, category: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cocktail emoji as placeholder
            Text(
                text = LocalContext.current.getString(R.string.widget_tropical_drink_icon),
                style = TextStyle(fontSize = 48.sp)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Cocktail name
            Text(
                text = name,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )

            // Category
            if (category.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = category,
                    style = TextStyle(
                        color = GlanceTheme.colors.secondary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Tap hint
            Text(
                text = LocalContext.current.getString(R.string.tap_to_view_details),
                style = TextStyle(
                    color = WidgetColorProviders.textSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Keys for storing widget state in preferences.
 */
object RandomCocktailKeys {
    val ID = stringPreferencesKey("random_cocktail_id")
    val NAME = stringPreferencesKey("random_cocktail_name")
    val CATEGORY = stringPreferencesKey("random_cocktail_category")
    val IS_LOADING = stringPreferencesKey("random_cocktail_is_loading")

    // Boolean flag; renamed from the retired string key "random_cocktail_error"
    // so DataStore never sees the same key name with two types across upgrades.
    val ERROR = booleanPreferencesKey("random_cocktail_has_error")
}

/**
 * Action callback to refresh the random cocktail.
 */
class RefreshRandomCocktailAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Set loading state
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[RandomCocktailKeys.IS_LOADING] = "true"
            prefs.remove(RandomCocktailKeys.ERROR)
        }
        RandomCocktailWidget().update(context, glanceId)

        // Fetch new cocktail
        withContext(Dispatchers.IO) {
            try {
                val dataProvider = WidgetDataProvider.getInstance()
                val cocktail = dataProvider.getRandomCocktail()

                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[RandomCocktailKeys.IS_LOADING] = "false"
                    if (cocktail != null) {
                        prefs[RandomCocktailKeys.ID] = cocktail.id
                        prefs[RandomCocktailKeys.NAME] = cocktail.name
                        prefs[RandomCocktailKeys.CATEGORY] = cocktail.category ?: ""
                        prefs.remove(RandomCocktailKeys.ERROR)
                    } else {
                        prefs[RandomCocktailKeys.ERROR] = true
                    }
                }
            } catch (e: Exception) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[RandomCocktailKeys.IS_LOADING] = "false"
                    prefs[RandomCocktailKeys.ERROR] = true
                }
            }
        }

        RandomCocktailWidget().update(context, glanceId)
    }
}

/**
 * Receiver for Random Cocktail Widget.
 */
class RandomCocktailWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RandomCocktailWidget()

    // Widget refresh lifecycle (AN-2): start the shared hourly worker when the
    // first widget is placed; tear it down only when no widget of either type
    // remains (both widgets share one worker).
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                WidgetUpdateWorker.cancelIfNoWidgetsRemain(context)
            } finally {
                pending.finish()
            }
        }
    }
}

