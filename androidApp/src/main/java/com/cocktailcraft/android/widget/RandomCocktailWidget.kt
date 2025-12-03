package com.cocktailcraft.android.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cocktailcraft.android.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import java.net.URL

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
        val cocktailName = prefs[RandomCocktailKeys.NAME] ?: "Tap to load"
        val cocktailCategory = prefs[RandomCocktailKeys.CATEGORY] ?: ""
        val cocktailId = prefs[RandomCocktailKeys.ID] ?: ""
        val isLoading = prefs[RandomCocktailKeys.IS_LOADING] == "true"
        val errorMessage = prefs[RandomCocktailKeys.ERROR]
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
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
                        text = "🍹 Random Cocktail",
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
                            text = "↻",
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
                    errorMessage != null -> {
                        ErrorContent(errorMessage)
                    }
                    cocktailName == "Tap to load" -> {
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
                text = "Loading...",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp
                )
            )
        }
    }

    @Composable
    private fun ErrorContent(message: String) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                style = TextStyle(fontSize = 24.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Tap ↻ to retry",
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
                text = "🍸",
                style = TextStyle(fontSize = 32.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Tap ↻ to discover",
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
                text = "🍹",
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
                text = "Tap to view details",
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
    val IMAGE_URL = stringPreferencesKey("random_cocktail_image_url")
    val IS_LOADING = stringPreferencesKey("random_cocktail_is_loading")
    val ERROR = stringPreferencesKey("random_cocktail_error")
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
                        prefs[RandomCocktailKeys.IMAGE_URL] = cocktail.imageUrl ?: ""
                        prefs.remove(RandomCocktailKeys.ERROR)
                    } else {
                        prefs[RandomCocktailKeys.ERROR] = "No cocktail found"
                    }
                }
            } catch (e: Exception) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[RandomCocktailKeys.IS_LOADING] = "false"
                    prefs[RandomCocktailKeys.ERROR] = "Failed to load"
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
}

