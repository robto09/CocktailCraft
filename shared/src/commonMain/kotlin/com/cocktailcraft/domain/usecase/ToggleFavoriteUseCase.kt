package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.Result

internal class ToggleFavoriteUseCase(
    private val cocktailRepository: CocktailFavoritesRepository
) {
    suspend operator fun invoke(cocktail: Cocktail): Result<Boolean> {
        return try {
            val favoriteResult = cocktailRepository.isCocktailFavorite(cocktail.id)
            val isAlreadyFavorite = favoriteResult.getOrNull() ?: false

            if (isAlreadyFavorite) {
                cocktailRepository.removeFromFavorites(cocktail)
                Result.Success(false) // Now not a favorite
            } else {
                cocktailRepository.addToFavorites(cocktail)
                Result.Success(true) // Now a favorite
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
}