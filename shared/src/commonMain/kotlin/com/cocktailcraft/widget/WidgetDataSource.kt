package com.cocktailcraft.widget

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.util.runCatchingNonCancellation
import kotlin.native.HiddenFromObjC

/**
 * What the home-screen widgets show, the same on both platforms: Android's
 * widgets read it directly, iOS writes it to the App Group snapshot its
 * widget extension reads (KoinHelper.getRandomCocktailWidgetPool).
 *
 * The Random Cocktail widget draws from a pool: a fresh random cocktail from
 * the API when online, then the recently viewed cocktails, so it has
 * something to show offline. Offline the API isn't tried at all (it would
 * only fail after the request timeout); a failed or empty fetch is skipped.
 *
 * Failures come back as null or are skipped rather than thrown, so callers
 * keep the widget's last-known-good content; cancellation always
 * propagates.
 */
@HiddenFromObjC // Kotlin-side: Swift goes through KoinHelper
class WidgetDataSource(
    private val detailRepository: CocktailDetailRepository,
    private val offlineRepository: CocktailOfflineRepository,
    private val favoritesRepository: CocktailFavoritesRepository,
    // Injectable for tests; production picks any of the stored cocktails.
    private val pickStored: (List<Cocktail>) -> Cocktail = { it.random() }
) {

    /** The random cocktail pool: the fresh pick first, then recently viewed; at most [limit], no duplicates. */
    suspend fun randomCocktailPool(limit: Int): List<Cocktail> =
        (listOfNotNull(freshRandomCocktail()) + storedCocktails()).distinctBy { it.id }.take(limit)

    /**
     * One cocktail from the pool, for a widget that shows one: the fresh
     * pick when there is one, else any of the stored cocktails. Null only
     * when the pool is empty.
     */
    suspend fun randomCocktail(): Cocktail? =
        freshRandomCocktail() ?: storedCocktails().takeIf { it.isNotEmpty() }?.let(pickStored)

    /**
     * The user's favorites (a local database read, so it works offline), or
     * null when the read failed — distinct from an empty list, so a failed
     * read never replaces the widget's list with "no favorites".
     */
    suspend fun favoriteCocktails(): List<Cocktail>? =
        resultOrNull { favoritesRepository.getFavoriteCocktails().getOrNull() }

    private suspend fun freshRandomCocktail(): Cocktail? =
        if (resultOrNull { offlineRepository.isOffline() } != false) {
            null
        } else {
            resultOrNull { detailRepository.getRandomCocktail().getOrNull() }
        }

    private suspend fun storedCocktails(): List<Cocktail> =
        resultOrNull { offlineRepository.getRecentlyViewedCocktails().getOrNull() }.orEmpty()

    private inline fun <T> resultOrNull(block: () -> T?): T? =
        runCatchingNonCancellation(onFailure = { null }, block = block)
}
