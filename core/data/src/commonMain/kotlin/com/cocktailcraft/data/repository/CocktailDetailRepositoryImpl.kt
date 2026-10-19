package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.onError
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

/**
 * Single-cocktail detail lookups, extracted from
 * the former CocktailRepositoryImpl monolith.
 *
 * The local catalog ([CocktailCache]) is the source of truth: [refreshCocktail]
 * fetches lookup.php into it and [observeCocktail] reads it back. Filter
 * endpoints only return partial data, so full details need these individual
 * lookups.
 */
internal class CocktailDetailRepositoryImpl(
    private val remote: CocktailRemoteDataSource,
    private val cocktailCache: CocktailCache,
    private val offlineRepository: CocktailOfflineRepository,
    // App-wide scope the fetches run in, so they finish even if the caller
    // goes away (see InFlightRequests)
    appScope: CoroutineScope,
    // Injectable for tests: coroutine virtual time does not move Clock.System.
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) : CocktailDetailRepository {

    // Concurrent refreshes of one cocktail share one request.
    private val inFlight = InFlightRequests<String, Result<Unit>>(appScope)

    override suspend fun getCocktailById(id: String): Result<Cocktail?> {
        return runCatchingResult("Failed to get cocktail by ID") {
            // A cached cocktail with full details can be served immediately
            val cachedCocktail = cocktailCache.getCachedCocktail(id)
            if (cachedCocktail != null && cachedCocktail.hasFullDetails) {
                cocktailCache.addToRecentlyViewed(cachedCocktail)
                return Result.Success(cachedCocktail)
            }

            // Missing or partial: fetch into the cache, then serve what it
            // holds. Offline or failing, that is the cached copy (or null).
            refreshCocktail(id, force = true)
            Result.Success(cocktailCache.getCachedCocktail(id) ?: cachedCocktail)
        }
    }

    override fun observeCocktail(id: String): Flow<Cocktail?> = cocktailCache.observeCocktail(id)

    override suspend fun refreshCocktail(id: String, force: Boolean): Result<Unit> {
        if (!force && isFresh(id)) return Result.Success(Unit)
        return inFlight.run(id) { fetchIntoCache(id) }
            .onError { message, _ -> Logger.w { "Refreshing cocktail $id failed: $message" } }
    }

    /** Stored with full details and written less than [CatalogRefreshPolicy.FRESHNESS_MS] ago. */
    private suspend fun isFresh(id: String): Boolean {
        val row = cocktailCache.peekCachedCocktail(id) ?: return false
        return row.cocktail.hasFullDetails && nowMs() - row.cachedAtMs < CatalogRefreshPolicy.FRESHNESS_MS
    }

    private suspend fun fetchIntoCache(id: String): Result<Unit> =
        runCatchingResult("Failed to refresh cocktail") {
            if (offlineRepository.isOffline()) {
                return Result.Error(CatalogRefreshPolicy.OFFLINE_MESSAGE, ErrorCode.NETWORK)
            }
            // getById is not throttled: opening a cocktail is user-initiated
            // and must never wait behind a category page's slot (SH-7).
            // A null lookup (unknown id) leaves the cache as it is.
            remote.getById(id)?.let { cocktail ->
                cocktailCache.cacheCocktail(cocktail)
                cocktailCache.addToRecentlyViewed(cocktail)
            }
            Result.Success(Unit)
        }

    override suspend fun getRandomCocktail(): Result<Cocktail?> {
        return runCatchingResult("Failed to get random cocktail") {
            Result.Success(remote.getRandom())
        }
    }
}
