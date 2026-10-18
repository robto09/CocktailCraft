package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CategoryPageIndex
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.onError
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Category pages, read from the local catalog ([CocktailCache]) and
 * refreshed from the network into it. Backs category browsing
 * ([CocktailCatalogRepositoryImpl]) and the search's category filter
 * ([CocktailSearchRepositoryImpl.filterByCategory]).
 *
 * The database is the single source of truth: [refresh] writes the fetched
 * page into cached_cocktail and records its ids in [CategoryPageIndex];
 * [observe] emits the page from the database again after every write.
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
    private val pageIndex: CategoryPageIndex = CategoryPageIndex(),
    // Injectable for tests: coroutine virtual time does not move Clock.System.
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {

    // Concurrent refreshes of one category share one request.
    private val inFlight = InFlightRequests<String, Result<Unit>>()

    /**
     * The cocktails of [category] as stored in the database, emitted right
     * away and again whenever the stored rows change. Once the category has
     * been fetched in this process the page lists exactly the fetched ids,
     * in the API's (alphabetical) order. Before that it lists the rows whose
     * category matches, by name; for the default category, entries cached
     * before category stamping existed stand in when nothing else matches, so
     * they aren't stranded offline.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(category: String): Flow<List<Cocktail>> =
        pageIndex.observeIds(category)
            .flatMapLatest { ids ->
                if (ids == null) {
                    cocktailCache.observeCocktailsByCategory(
                        category = category,
                        includeUncategorized = category == CocktailCategories.DEFAULT
                    )
                } else {
                    cocktailCache.observeCocktailsByIds(ids).map { byId -> ids.mapNotNull(byId::get) }
                }
            }
            .distinctUntilChanged()

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

    private fun isFresh(category: String): Boolean {
        val page = pageIndex.page(category) ?: return false
        return nowMs() - page.fetchedAtMs < CatalogRefreshPolicy.FRESHNESS_MS
    }

    private suspend fun fetchIntoCache(category: String): Result<Unit> =
        runCatchingResult("Failed to load cocktails for $category") {
            if (offlineRepository.isOffline() && snapshot(category).isNotEmpty()) {
                return Result.Error(CatalogRefreshPolicy.OFFLINE_MESSAGE, ErrorCode.NETWORK)
            }
            // filterByCategoryRaw takes the rate-limit slot (retry/backoff
            // is Ktor's job — SH-7).
            val cocktails = remote.filterByCategoryRaw(category).map { dto ->
                val cocktail = remote.mapToDomain(dto)
                // filter.php omits the category, so stamp the one we fetched
                // by; the offline fallback finds rows by that column.
                if (cocktail.category.isNullOrBlank()) cocktail.copy(category = category) else cocktail
            }
            // Rows first, then the index: the page never lists ids whose
            // rows aren't written yet.
            cocktailCache.cacheCocktails(cocktails)
            pageIndex.put(category, cocktails.map { it.id }, nowMs())
            Result.Success(Unit)
        }
}
