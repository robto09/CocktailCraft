package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import kotlinx.coroutines.flow.Flow

internal class ManageFavoritesUseCase(
    private val favoritesRepository: CocktailFavoritesRepository,
    private val detailRepository: CocktailDetailRepository
) {
    /** Hot stream of favorites; mutations from any screen publish here (SH-4). */
    fun observeFavorites(): Flow<List<Cocktail>> = favoritesRepository.observeFavorites()

    suspend fun toggle(cocktail: Cocktail): Result<Boolean> {
        return try {
            val isFav = favoritesRepository.isCocktailFavorite(cocktail.id).getOrNull() ?: false
            if (isFav) {
                favoritesRepository.removeFromFavorites(cocktail)
                Result.Success(false)
            } else {
                favoritesRepository.addToFavorites(hydrated(cocktail))
                Result.Success(true)
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun loadFavorites(): Result<List<Cocktail>> {
        return favoritesRepository.getFavoriteCocktails()
    }

    suspend fun isFavorite(cocktailId: String): Boolean {
        return favoritesRepository.isCocktailFavorite(cocktailId).getOrNull() ?: false
    }

    suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        return favoritesRepository.addToFavorites(hydrated(cocktail))
    }

    /**
     * List endpoints fabricate placeholder ingredients/instructions; fetch
     * full details before persisting so stored favorites are complete.
     */
    private suspend fun hydrated(cocktail: Cocktail): Cocktail {
        if (cocktail.hasFullDetails) return cocktail
        return try {
            detailRepository.getCocktailById(cocktail.id).getOrNull() ?: cocktail
        } catch (e: Exception) {
            cocktail
        }
    }

    suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> {
        return favoritesRepository.removeFromFavorites(cocktail)
    }
}

