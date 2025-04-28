package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.config.AppConfig
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailRepository
import com.cocktailcraft.domain.repository.FavoritesRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of the FavoritesRepository interface.
 * This implementation delegates to the CocktailRepository for most operations.
 */
class FavoritesRepositoryImpl(
    private val cocktailRepository: CocktailRepository,
    private val settings: Settings,
    private val appConfig: AppConfig
) : FavoritesRepository {

    override suspend fun getFavorites(): Flow<List<Cocktail>> {
        return cocktailRepository.getFavoriteCocktails()
    }

    override suspend fun addToFavorites(cocktail: Cocktail) {
        cocktailRepository.addToFavorites(cocktail)
    }

    override suspend fun removeFromFavorites(cocktail: Cocktail) {
        cocktailRepository.removeFromFavorites(cocktail)
    }

    override suspend fun isFavorite(id: String): Flow<Boolean> {
        return cocktailRepository.isCocktailFavorite(id)
    }

    override suspend fun toggleFavorite(cocktail: Cocktail) {
        // Check if the cocktail is already a favorite
        val isFavorite = settings.getStringOrNull(appConfig.favoritesStorageKey)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.contains(cocktail.id)
            ?: false

        if (isFavorite) {
            removeFromFavorites(cocktail)
        } else {
            addToFavorites(cocktail)
        }
    }
}
