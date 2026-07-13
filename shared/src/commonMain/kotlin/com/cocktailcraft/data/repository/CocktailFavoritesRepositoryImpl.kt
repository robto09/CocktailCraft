package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailOfflineRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

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
    private val offlineRepository: CocktailOfflineRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CocktailFavoritesRepository {

    // Hot favorites stream (SH-4): re-published after every mutation so all
    // observers (Home, Favorites, Detail — any screen) stay in sync without
    // manual re-pulls. Mirrors CartRepositoryImpl's observe pattern.
    private val _favorites = MutableStateFlow<List<Cocktail>>(emptyList())

    override fun observeFavorites(): Flow<List<Cocktail>> =
        _favorites.asStateFlow().onStart { getFavoriteCocktails() }

    private fun favoriteIds(): List<String> =
        settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

    private fun saveFavoriteIds(ids: List<String>) {
        settings.putString(appConfig.favoritesStorageKey, ids.joinToString(","))
    }

    override suspend fun getFavoriteCocktails(): Result<List<Cocktail>> = withContext(ioDispatcher) {
        runCatchingResult("Failed to get favorite cocktails") {
            val offline = offlineRepository.isOffline()
            val favorites = mutableListOf<Cocktail>()
            for (id in favoriteIds()) {
                val cocktail = cocktailCache.getCachedCocktail(id)
                    ?: if (offline) null else remote.getById(id)?.also { cocktailCache.cacheCocktail(it) }
                if (cocktail != null) favorites.add(cocktail)
            }
            _favorites.value = favorites
            Result.Success(favorites)
        }
    }

    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to add to favorites") {
            val ids = favoriteIds()
            if (!ids.contains(cocktail.id)) {
                saveFavoriteIds(ids + cocktail.id)
            }
            // Offline safety (SH-4): persist the object too, not just the id,
            // so the favorite is still resolvable with no connectivity.
            cocktailCache.cacheCocktail(cocktail)
            getFavoriteCocktails() // re-publish onto the observed flow
            Result.Success(Unit)
        }
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> = withContext(ioDispatcher) {
        runCatchingResult("Failed to remove from favorites") {
            saveFavoriteIds(favoriteIds() - cocktail.id)
            getFavoriteCocktails() // re-publish onto the observed flow
            Result.Success(Unit)
        }
    }

    override suspend fun isCocktailFavorite(id: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatchingResult("Failed to check if cocktail is favorite") {
            Result.Success(favoriteIds().contains(id))
        }
    }
}
