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
                when (val removed = favoritesRepository.removeFromFavorites(cocktail)) {
                    is Result.Error -> Result.Error(removed.message, removed.code)
                    else -> Result.Success(false)
                }
            } else {
                when (val added = addToFavorites(cocktail)) {
                    is Result.Error -> Result.Error(added.message, added.code)
                    else -> Result.Success(true)
                }
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

    /**
     * Persist-then-hydrate: the list object is stored and published as-is so
     * observers flip immediately, then the detail fetch (which can take a
     * network round trip) upgrades the stored entry in place. The upgrade is
     * skipped if the favorite was removed while hydration was in flight.
     */
    suspend fun addToFavorites(cocktail: Cocktail): Result<Unit> {
        val result = favoritesRepository.addToFavorites(cocktail)
        if (result is Result.Error || cocktail.hasFullDetails) return result

        val full = hydrated(cocktail)
        if (full !== cocktail && favoritesRepository.isCocktailFavorite(cocktail.id).getOrDefault(false)) {
            favoritesRepository.addToFavorites(full)
        }
        return result
    }

    /**
     * List endpoints fabricate placeholder ingredients/instructions; fetch
     * full details so stored favorites are complete.
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

