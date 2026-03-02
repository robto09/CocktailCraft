package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault

class ManageFavoritesUseCase(
    private val favoritesRepository: CocktailFavoritesRepository
) {
    suspend fun toggle(cocktail: Cocktail): Result<Boolean> {
        return try {
            val isFav = favoritesRepository.isCocktailFavorite(cocktail.id).getOrNull() ?: false
            if (isFav) {
                favoritesRepository.removeFromFavorites(cocktail)
                Result.Success(false)
            } else {
                favoritesRepository.addToFavorites(cocktail)
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
        return favoritesRepository.addToFavorites(cocktail)
    }

    suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> {
        return favoritesRepository.removeFromFavorites(cocktail)
    }
}

