package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.Result
import com.russhwolf.settings.Settings

/**
 * Favorites persistence (comma-separated ids in Settings, full objects
 * resolved through the cache or the API), extracted from the former
 * CocktailRepositoryImpl god class.
 */
internal class CocktailFavoritesRepositoryImpl(
    private val settings: Settings,
    private val appConfig: AppConfig,
    private val cocktailCache: CocktailCache,
    private val remote: CocktailRemoteDataSource,
    private val offlineRepository: CocktailOfflineRepositoryImpl
) : CocktailFavoritesRepository {

    private fun favoriteIds(): List<String> =
        settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

    private fun saveFavoriteIds(ids: List<String>) {
        settings.putString(appConfig.favoritesStorageKey, ids.joinToString(","))
    }

    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> {
        return try {
            val offline = offlineRepository.isOffline()
            val favorites = mutableListOf<Cocktail>()
            for (id in favoriteIds()) {
                val cocktail = cocktailCache.getCachedCocktail(id)
                    ?: if (offline) null else remote.getById(id)?.also { cocktailCache.cacheCocktail(it) }
                if (cocktail != null) favorites.add(cocktail)
            }
            Result.Success(favorites)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get favorite cocktails")
        }
    }

    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        return try {
            val ids = favoriteIds()
            if (!ids.contains(cocktail.id)) {
                saveFavoriteIds(ids + cocktail.id)
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add to favorites")
        }
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> {
        return try {
            saveFavoriteIds(favoriteIds() - cocktail.id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to remove from favorites")
        }
    }

    override suspend fun isCocktailFavorite(id: String): Result<Boolean> {
        return try {
            Result.Success(favoriteIds().contains(id))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to check if cocktail is favorite")
        }
    }
}
