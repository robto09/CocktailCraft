package com.cocktailcraft.android.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
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
import com.cocktailcraft.android.R
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
        val hasError = prefs[FavoritesWidgetKeys.ERROR] == true
        
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
                        text = LocalContext.current.getString(R.string.widget_favorites_header, favoritesCount),
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
                            text = LocalContext.current.getString(R.string.widget_refresh_icon),
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
                    hasError -> ErrorContent()
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
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = LocalContext.current.getString(R.string.widget_error_icon),
                    style = TextStyle(fontSize = 24.sp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = LocalContext.current.getString(R.string.widget_tap_refresh_to_retry),
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
                Text(
                    text = LocalContext.current.getString(R.string.widget_no_favorites_icon),
                    style = TextStyle(fontSize = 32.sp)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = LocalContext.current.getString(R.string.no_favorites),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = LocalContext.current.getString(R.string.tap_to_add_favorites),
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
                            text = LocalContext.current.getString(R.string.widget_view_all_favorites, favorites.size),
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
        // Open the tapped favorite's detail screen instead of cold-launching
        // to the Home tab
        val context = LocalContext.current
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .cornerRadius(8.dp)
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
                .clickable(actionStartActivity(cocktailDetailIntent(context, favorite.id))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cocktail emoji
            Text(
                text = context.getString(R.string.widget_cocktail_glass_icon),
                style = TextStyle(fontSize = 24.sp)
            )

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
            Text(
                text = context.getString(R.string.widget_favorite_heart_icon),
                style = TextStyle(fontSize = 16.sp)
            )
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

    // Boolean flag; renamed from the retired string key "favorites_error"
    // so DataStore never sees the same key name with two types across upgrades.
    val ERROR = booleanPreferencesKey("favorites_has_error")
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
                    prefs[FavoritesWidgetKeys.ERROR] = true
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

