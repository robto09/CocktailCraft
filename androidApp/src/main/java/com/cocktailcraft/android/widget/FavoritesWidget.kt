package com.cocktailcraft.android.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
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
import com.cocktailcraft.android.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Favorites Widget that displays the user's favorite cocktails.
 * Shows a scrollable list of favorites with quick access to the app.
 */
class FavoritesWidget : GlanceAppWidget() {
    
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CocktailWidgetTheme {
                FavoritesContent()
            }
        }
    }
    
    @Composable
    private fun FavoritesContent() {
        val prefs = currentState<Preferences>()
        val favoritesJson = prefs[FavoritesWidgetKeys.FAVORITES_LIST]
        val favoritesCount = prefs[FavoritesWidgetKeys.FAVORITES_COUNT]?.toIntOrNull() ?: 0
        val isLoading = prefs[FavoritesWidgetKeys.IS_LOADING] == "true"
        val errorMessage = prefs[FavoritesWidgetKeys.ERROR]
        
        val favorites = try {
            if (favoritesJson != null) {
                FavoriteItem.fromJsonArray(favoritesJson)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header with title and refresh button
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❤️ Favorites ($favoritesCount)",
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
                            .clickable(actionRunCallback<RefreshFavoritesAction>()),
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
                    isLoading -> LoadingContent()
                    errorMessage != null -> ErrorContent()
                    favorites.isEmpty() -> EmptyFavoritesContent()
                    else -> FavoritesListContent(favorites)
                }
            }
        }
    }
    
    @Composable
    private fun LoadingContent() {
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
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
    private fun ErrorContent() {
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "⚠️", style = TextStyle(fontSize = 24.sp))
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Tap ↻ to retry",
                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 12.sp)
                )
            }
        }
    }

    @Composable
    private fun EmptyFavoritesContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "💔", style = TextStyle(fontSize = 32.sp))
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "No favorites yet",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Tap to add some!",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun FavoritesListContent(favorites: List<FavoriteItem>) {
        LazyColumn(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            items(favorites.take(5)) { favorite ->
                FavoriteItemRow(favorite)
            }

            // Show "View all" if there are more favorites
            if (favorites.size > 5) {
                item {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View all ${favorites.size} favorites →",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun FavoriteItemRow(favorite: FavoriteItem) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .cornerRadius(8.dp)
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cocktail emoji
            Text(text = "🍸", style = TextStyle(fontSize = 24.sp))

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Name and category
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = favorite.name,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                if (favorite.category.isNotEmpty()) {
                    Text(
                        text = favorite.category,
                        style = TextStyle(
                            color = WidgetColorProviders.textSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            // Heart icon
            Text(text = "❤️", style = TextStyle(fontSize = 16.sp))
        }
    }
}

/**
 * Simple data class for favorite items in widget.
 * Uses Android's org.json for serialization to avoid kotlinx.serialization dependency.
 */
data class FavoriteItem(
    val id: String,
    val name: String,
    val category: String = "",
    val imageUrl: String = ""
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("category", category)
            put("imageUrl", imageUrl)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): FavoriteItem {
            return FavoriteItem(
                id = json.optString("id", ""),
                name = json.optString("name", ""),
                category = json.optString("category", ""),
                imageUrl = json.optString("imageUrl", "")
            )
        }

        fun fromJsonArray(jsonString: String): List<FavoriteItem> {
            return try {
                val jsonArray = JSONArray(jsonString)
                (0 until jsonArray.length()).map { i ->
                    fromJson(jsonArray.getJSONObject(i))
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun toJsonArray(items: List<FavoriteItem>): String {
            val jsonArray = JSONArray()
            items.forEach { item ->
                jsonArray.put(item.toJson())
            }
            return jsonArray.toString()
        }
    }
}

/**
 * Keys for storing widget state in preferences.
 */
object FavoritesWidgetKeys {
    val FAVORITES_LIST = stringPreferencesKey("favorites_list_json")
    val FAVORITES_COUNT = stringPreferencesKey("favorites_count")
    val IS_LOADING = stringPreferencesKey("favorites_is_loading")
    val ERROR = stringPreferencesKey("favorites_error")
}

/**
 * Action callback to refresh favorites.
 */
class RefreshFavoritesAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Set loading state
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[FavoritesWidgetKeys.IS_LOADING] = "true"
            prefs.remove(FavoritesWidgetKeys.ERROR)
        }
        FavoritesWidget().update(context, glanceId)

        // Fetch favorites
        withContext(Dispatchers.IO) {
            try {
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

                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[FavoritesWidgetKeys.IS_LOADING] = "false"
                    prefs[FavoritesWidgetKeys.FAVORITES_LIST] = favoritesJson
                    prefs[FavoritesWidgetKeys.FAVORITES_COUNT] = favorites.size.toString()
                    prefs.remove(FavoritesWidgetKeys.ERROR)
                }
            } catch (e: Exception) {
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[FavoritesWidgetKeys.IS_LOADING] = "false"
                    prefs[FavoritesWidgetKeys.ERROR] = "Failed to load"
                }
            }
        }

        FavoritesWidget().update(context, glanceId)
    }
}

/**
 * Receiver for Favorites Widget.
 */
class FavoritesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavoritesWidget()
}

