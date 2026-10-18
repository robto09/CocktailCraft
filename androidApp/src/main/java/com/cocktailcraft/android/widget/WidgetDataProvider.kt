package com.cocktailcraft.android.widget

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import kotlinx.coroutines.CancellationException

/**
 * Data source for the home-screen widgets, over the shared repositories.
 * A Koin single (widgetModule) — the only way to obtain one; the widget
 * worker and refresh actions inject it.
 *
 * Failures come back as null rather than exceptions so callers can keep the
 * widget's last-known-good content; cancellation always propagates.
 */
class WidgetDataProvider(
    private val detailRepository: CocktailDetailRepository,
    private val offlineRepository: CocktailOfflineRepository,
    private val favoritesRepository: CocktailFavoritesRepository,
    // Injectable for tests; production picks any recently viewed cocktail.
    private val pickFallback: (List<Cocktail>) -> Cocktail = { it.random() }
) {

    /**
     * A random cocktail from the API, or — when that fails or comes back
     * empty (offline, rate limited) — one of the recently viewed cocktails
     * from the local cache. Null only when neither is available.
     */
    suspend fun getRandomCocktail(): Cocktail? =
        resultOrNull { detailRepository.getRandomCocktail().getOrNull() }
            ?: resultOrNull { offlineRepository.getRecentlyViewedCocktails().getOrNull() }
                ?.takeIf { it.isNotEmpty() }
                ?.let(pickFallback)

    /**
     * The user's favorites (a local database read, so it works offline), or
     * null when the read failed — distinct from an empty list, so a failed
     * read never replaces the widget's list with "no favorites".
     */
    suspend fun getFavoriteCocktails(): List<Cocktail>? =
        resultOrNull { favoritesRepository.getFavoriteCocktails().getOrNull() }

    private inline fun <T> resultOrNull(block: () -> T?): T? =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
}
