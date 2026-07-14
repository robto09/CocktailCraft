package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingResult

/**
 * Single-cocktail detail lookups (cache-first with API fallback) plus image
 * URL resolution, extracted from the former CocktailRepositoryImpl monolith.
 *
 * Filter endpoints only return partial data, so full details require
 * individual lookup.php calls, which are cached aggressively and lightly
 * rate limited.
 */
internal class CocktailDetailRepositoryImpl(
    private val remote: CocktailRemoteDataSource,
    private val cocktailCache: CocktailCache,
    private val appConfig: AppConfig,
    private val offlineRepository: CocktailOfflineRepository
) : CocktailDetailRepository {

    private suspend fun isOffline(): Boolean = offlineRepository.isOffline()

    override suspend fun getCocktailById(id: String): Result<Cocktail?> {
        return runCatchingResult("Failed to get cocktail by ID") {
            // A cached cocktail with full details can be served immediately
            val cachedCocktail = cocktailCache.getCachedCocktail(id)
            if (cachedCocktail != null && cachedCocktail.hasFullDetails) {
                cocktailCache.addToRecentlyViewed(cachedCocktail)
                return Result.Success(cachedCocktail)
            }

            // If we're offline, return whatever we have cached
            if (isOffline()) {
                return Result.Success(cachedCocktail)
            }

            // If online, fetch full details from API
            try {
                remote.awaitRateLimitWindow() // shared min-interval throttle, not an ad hoc sleep (SH-7)
                remote.noteApiCall()
                val cocktail = remote.getById(id)
                if (cocktail != null) {
                    cocktailCache.cacheCocktail(cocktail)
                    cocktailCache.addToRecentlyViewed(cocktail)
                    Result.Success(cocktail)
                } else {
                    Result.Success(cachedCocktail)
                }
            } catch (e: Exception) {
                // On API error, return cached version
                Result.Success(cachedCocktail)
            }
        }
    }

    override suspend fun refreshCocktailById(id: String): Result<Cocktail?> {
        return try {
            if (isOffline()) {
                Result.Success(cocktailCache.getCachedCocktail(id))
            } else {
                remote.awaitRateLimitWindow() // shared min-interval throttle, not an ad hoc sleep (SH-7)
                remote.noteApiCall()
                val cocktail = remote.getById(id)
                if (cocktail != null) {
                    cocktailCache.cacheCocktail(cocktail)
                    cocktailCache.addToRecentlyViewed(cocktail)
                    Result.Success(cocktail)
                } else {
                    Result.Success(cocktailCache.getCachedCocktail(id))
                }
            }
        } catch (e: Exception) {
            Logger.e { "Error refreshing cocktail $id: ${e.message}" }
            Result.Error(
                message = e.message ?: "Failed to refresh cocktail",
                code = ErrorHandler.getErrorFromException(e, "Failed to refresh cocktail").errorCode
            )
        }
    }

    override suspend fun getRandomCocktail(): Result<Cocktail?> {
        return runCatchingResult("Failed to get random cocktail") {
            Result.Success(remote.getRandom())
        }
    }

    override fun getCocktailImageUrl(cocktail: Cocktail): String {
        if (!cocktail.imageUrl.isNullOrBlank()) {
            return cocktail.imageUrl
        }
        return if (cocktail.id.isNotBlank()) {
            "${appConfig.imageBaseUrl}/${appConfig.cocktailsImagePath}/${cocktail.id}.jpg"
        } else {
            ""
        }
    }
}
