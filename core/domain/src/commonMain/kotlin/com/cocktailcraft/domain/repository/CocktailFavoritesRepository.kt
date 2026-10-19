package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlin.native.HiddenFromObjC

/**
 * Repository interface for cocktail favorites operations.
 * Part of the Interface Segregation split of CocktailRepository.
 */
@HiddenFromObjC // Kotlin-side port, not Swift API (AR-12): Swift reaches data through the ViewModels
interface CocktailFavoritesRepository {
    /**
     * Stream of favorite cocktails: the stored list on collection, then again
     * after every add, remove or in-place upgrade from any screen (SH-4) —
     * mirrors [CartRepository.observeCartItems].
     */
    fun observeFavorites(): Flow<List<Cocktail>>

    suspend fun getFavoriteCocktails(): Result<List<Cocktail>>
    suspend fun addToFavorites(cocktail: Cocktail): Result<Unit>
    suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit>
    suspend fun isCocktailFavorite(id: String): Result<Boolean>
}
