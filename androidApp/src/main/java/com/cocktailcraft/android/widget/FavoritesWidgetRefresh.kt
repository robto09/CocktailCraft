package com.cocktailcraft.android.widget

import android.content.Context
import com.cocktailcraft.domain.model.Cocktail
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

/**
 * One emission per settled change to which cocktails are favorites, from a
 * stream of the favorites list: the first list (what the widgets already
 * show) is skipped, re-emissions with the same ids — a favorite's stored
 * details being upgraded in place — are ignored, and a burst (Undo right
 * after a removal, clearing several) settles into one once the ids have
 * stayed put for [debounceMillis].
 */
@OptIn(FlowPreview::class)
internal fun favoriteIdChanges(
    favorites: Flow<List<Cocktail>>,
    debounceMillis: Long = 300L
): Flow<List<String>> =
    favorites
        .map { list -> list.map { it.id } }
        .distinctUntilChanged()
        .drop(1)
        .debounce(debounceMillis)

/**
 * Keeps the Favorites home-screen widgets in step with the app: re-reads
 * the favorites into them as soon as they change, instead of waiting for
 * the periodic refresh. Collect while the UI is alive (MainActivity) — that
 * is the only place favorites change.
 */
internal suspend fun refreshFavoritesWidgetOnChange(context: Context, favorites: Flow<List<Cocktail>>) {
    val appContext = context.applicationContext
    favoriteIdChanges(favorites).collect {
        WidgetUpdateWorker.requestFavoritesRefresh(appContext)
    }
}
