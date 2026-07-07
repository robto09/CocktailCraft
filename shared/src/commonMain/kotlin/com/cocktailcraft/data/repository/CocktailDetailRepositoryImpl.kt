package com.cocktailcraft.data.repository

import co.touchlab.kermit.Logger
import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.delay

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
    private val cacheManager: CocktailCacheManager,
    private val appConfig: AppConfig,
    private val offlineRepository: CocktailOfflineRepositoryImpl
) : CocktailDetailRepository {

    private suspend fun isOffline(): Boolean = offlineRepository.isOffline()

    override suspend fun getCocktailById(id: String): Result<Cocktail?> {
        return try {
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
                delay(200) // Avoid rate limiting on rapid successive calls
                val cocktail = remote.getById(id)
                if (cocktail != null) {
                    cocktailCache.cacheCocktail(cocktail)
                    cocktailCache.addToRecentlyViewed(cocktail)
                    cacheManager.updateGlobalCocktailsCache { if (it.id == id) cocktail else it }
                    Result.Success(cocktail)
                } else {
                    Result.Success(cachedCocktail)
                }
            } catch (e: Exception) {
                // On API error, return cached version
                Result.Success(cachedCocktail)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cocktail by ID")
        }
    }

    override suspend fun refreshCocktailById(id: String): Result<Cocktail?> {
        return try {
            if (isOffline()) {
                Result.Success(cocktailCache.getCachedCocktail(id))
            } else {
                delay(200) // Avoid rate limiting
                val cocktail = remote.getById(id)
                if (cocktail != null) {
                    cocktailCache.cacheCocktail(cocktail)
                    cocktailCache.addToRecentlyViewed(cocktail)
                    cacheManager.updateGlobalCocktailsCache { if (it.id == id) cocktail else it }
                    Result.Success(cocktail)
                } else {
                    Result.Success(cocktailCache.getCachedCocktail(id))
                }
            }
        } catch (e: Exception) {
            Logger.e { "Error refreshing cocktail $id: ${e.message}" }
            Result.Error(e.message ?: "Failed to refresh cocktail")
        }
    }

    override suspend fun getRandomCocktail(): Result<Cocktail?> {
        return try {
            Result.Success(remote.getRandom())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get random cocktail")
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
