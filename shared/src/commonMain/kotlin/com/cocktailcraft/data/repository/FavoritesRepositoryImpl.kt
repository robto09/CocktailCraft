package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.FavoritesRepository
import com.cocktailcraft.domain.util.Result
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Implementation of the FavoritesRepository interface.
 * This implementation delegates to the CocktailRepository for most operations.
 */
internal class FavoritesRepositoryImpl(
    private val cocktailRepository: CocktailFavoritesRepository,
    private val settings: Settings,
    private val appConfig: AppConfig,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FavoritesRepository {

    override suspend fun getFavorites(): Result<List<Cocktail>> {
        return cocktailRepository.getFavoriteCocktails()
    }

    override suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        return cocktailRepository.addToFavorites(cocktail)
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> {
        return cocktailRepository.removeFromFavorites(cocktail)
    }

    override suspend fun isFavorite(id: String): Result<Boolean> {
        return cocktailRepository.isCocktailFavorite(id)
    }

    override suspend fun toggleFavorite(cocktail: Cocktail): Result<Unit> {
        return try {
            // Check if the cocktail is already a favorite
            val isFav = withContext(ioDispatcher) {
                settings.getStringOrNull(appConfig.favoritesStorageKey)
                    ?.split(",")
                    ?.filter { it.isNotEmpty() }
                    ?.contains(cocktail.id)
                    ?: false
            }

            if (isFav) {
                removeFromFavorites(cocktail)
            } else {
                addToFavorites(cocktail)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to toggle favorite")
        }
    }
}
