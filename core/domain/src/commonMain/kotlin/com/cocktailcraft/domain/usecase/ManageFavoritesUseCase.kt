package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.runCatchingNonCancellation
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.flow.Flow
import kotlin.native.HiddenFromObjC

@HiddenFromObjC // Public for :presentation; not Swift API
class ManageFavoritesUseCase(
    private val favoritesRepository: CocktailFavoritesRepository,
    private val detailRepository: CocktailDetailRepository
) {
    /** Hot stream of favorites; mutations from any screen publish here. */
    fun observeFavorites(): Flow<List<Cocktail>> = favoritesRepository.observeFavorites()

    suspend fun toggle(cocktail: Cocktail): Result<Boolean> = runCatchingResult("Failed to update favorites") {
        val isFav = favoritesRepository.isCocktailFavorite(cocktail.id).getOrNull() ?: false
        if (isFav) {
            when (val removed = favoritesRepository.removeFromFavorites(cocktail)) {
                is Result.Error -> removed
                else -> Result.Success(false)
            }
        } else {
            when (val added = addToFavorites(cocktail)) {
                is Result.Error -> added
                else -> Result.Success(true)
            }
        }
    }

    suspend fun loadFavorites(): Result<List<Cocktail>> = favoritesRepository.getFavoriteCocktails()

    suspend fun isFavorite(cocktailId: String): Boolean = favoritesRepository.isCocktailFavorite(
        cocktailId
    ).getOrNull() ?: false

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
     * List endpoints return partial records; fetch full details so stored
     * favorites are complete.
     */
    private suspend fun hydrated(cocktail: Cocktail): Cocktail {
        if (cocktail.hasFullDetails) return cocktail
        return runCatchingNonCancellation(onFailure = { cocktail }) {
            detailRepository.getCocktailById(cocktail.id).getOrNull() ?: cocktail
        }
    }

    suspend fun removeFromFavorites(cocktail: Cocktail): Result<Unit> = favoritesRepository.removeFromFavorites(
        cocktail
    )
}
