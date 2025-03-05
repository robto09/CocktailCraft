package com.coffee.store.domain.usecase

import com.coffee.store.domain.model.Cocktail
import com.coffee.store.domain.repository.CocktailRepository
import com.coffee.store.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first

class ToggleFavoriteUseCase(
    private val cocktailRepository: CocktailRepository
) {
    suspend operator fun invoke(cocktail: Cocktail): Flow<Result<Boolean>> = flow {
        try {
            // Check if cocktail is already a favorite
            val isAlreadyFavorite = cocktailRepository.isCocktailFavorite(cocktail.id).first()
            
            // Toggle favorite status
            if (isAlreadyFavorite) {
                cocktailRepository.removeFromFavorites(cocktail)
                emit(Result.Success(false)) // Now not a favorite
            } else {
                cocktailRepository.addToFavorites(cocktail)
                emit(Result.Success(true)) // Now a favorite
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }
} 