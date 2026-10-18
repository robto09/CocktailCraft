package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.onError
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

/**
 * Category pages, read from the local catalog ([CocktailCache]) and
 * refreshed from the network into it. Backs category browsing
 * ([CocktailCatalogRepositoryImpl]) and the search's category filter
 * ([CocktailSearchRepositoryImpl.filterByCategory]).
 *
 * The database is the single source of truth: [refresh] writes the fetched
 * cocktails into cached_cocktail and the page (ids, order, fetch time) into
 * category_page; [observe] emits the page from the database again after
 * every write. Both survive a restart.
 *
 * API Limitations & Workarounds:
 * - Filter endpoints (filter.php) only return partial data (id, name, thumbnail)
 * - Full cocktail details require individual lookup.php calls; the cache
 *   keeps a full record when a page refresh brings its placeholder again
 * - The free API has rate limits, so pages are refreshed only when stale
 */
internal class CocktailCategoryFetcher(
    private val remote: CocktailRemoteDataSource,
    private val cocktailCache: CocktailCache,
    private val offlineRepository: CocktailOfflineRepository,
    // Injectable for tests: coroutine virtual time does not move Clock.System.
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {

    // Concurrent refreshes of one category share one request.
    private val inFlight = InFlightRequests<String, Result<Unit>>()

    /**
     * The cocktails of [category] as stored in the database, emitted right
     * away and again whenever the stored rows change. Once the category has
     * been fetched the page lists exactly the fetched cocktails, in the API's
     * (alphabetical) order. Before that it lists the cached cocktails whose
     * category matches, by name; for the default category, entries cached
     * before category stamping existed stand in when nothing else matches,
     * so they aren't stranded offline.
     */
    fun observe(category: String): Flow<List<Cocktail>> =
        cocktailCache.observeCategoryPage(
            category = category,
            includeUncategorized = category == CocktailCategories.DEFAULT
        )

    /** The page as currently stored (see [observe]). */
    suspend fun snapshot(category: String): List<Cocktail> = observe(category).first()

    /**
     * Fetch [category] from the network into the database, unless it was
     * fetched less than [CatalogRefreshPolicy.FRESHNESS_MS] ago ([force] fetches anyway).
     *
     * Failures come back as Result.Error and leave the stored page as it
     * was, so callers keep showing it. While offline (per the network
     * monitor) a category with stored rows is not fetched at all
     * ([CatalogRefreshPolicy.OFFLINE_MESSAGE], ErrorCode.NETWORK). With nothing stored the
     * fetch is attempted anyway: the monitor can be stale (path updates lag
     * or, on the simulator, never arrive), and when genuinely offline the
     * request fails fast.
     *
     * Concurrent calls for the same category share one request; different
     * categories fetch in parallel.
     */
    suspend fun refresh(category: String, force: Boolean = false): Result<Unit> {
        if (!force && isFresh(category)) return Result.Success(Unit)
        return inFlight.run(category) { fetchIntoCache(category) }
            .onError { message, _ -> Logger.w { "Refreshing category $category failed: $message" } }
    }

    private suspend fun isFresh(category: String): Boolean {
        val fetchedAt = cocktailCache.categoryPageFetchedAt(category) ?: return false
        return nowMs() - fetchedAt < CatalogRefreshPolicy.FRESHNESS_MS
    }

    private suspend fun fetchIntoCache(category: String): Result<Unit> =
        runCatchingResult("Failed to load cocktails for $category") {
            if (offlineRepository.isOffline() && snapshot(category).isNotEmpty()) {
                return Result.Error(CatalogRefreshPolicy.OFFLINE_MESSAGE, ErrorCode.NETWORK)
            }
            // filterByCategory takes the rate-limit slot (retry/backoff is
            // Ktor's job — SH-7) and stamps the category filter.php omits.
            val cocktails = remote.filterByCategory(category)
            cocktailCache.cacheCategoryPage(category, cocktails, fetchedAtMs = nowMs())
            Result.Success(Unit)
        }
}
