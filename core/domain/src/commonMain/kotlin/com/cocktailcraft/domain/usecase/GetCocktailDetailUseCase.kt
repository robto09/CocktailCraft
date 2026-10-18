package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.runCatchingNonCancellation
import kotlin.native.HiddenFromObjC

@HiddenFromObjC // Public for :presentation; not Swift API (AR-12)
class GetCocktailDetailUseCase(
    private val detailRepository: CocktailDetailRepository,
    private val favoritesRepository: CocktailFavoritesRepository,
    private val searchRepository: CocktailSearchRepository
) {
    suspend operator fun invoke(cocktailId: String): Result<Cocktail?> {
        return detailRepository.getCocktailById(cocktailId)
    }

    suspend fun isFavorite(cocktailId: String): Boolean {
        // Point lookup, not a load (and decode) of every stored favorite
        return runCatchingNonCancellation(onFailure = { false }) {
            favoritesRepository.isCocktailFavorite(cocktailId).getOrDefault(false)
        }
    }

    suspend fun getRelatedCocktails(cocktail: Cocktail, limit: Int = 3): List<Cocktail> {
        return runCatchingNonCancellation(onFailure = { emptyList() }) {
            val cocktails = searchRepository.filterByCategory(cocktail.category ?: CocktailCategories.DEFAULT).getOrDefault(emptyList())
            cocktails.filter { it.id != cocktail.id }.shuffled().take(limit)
        }
    }

    suspend fun refresh(cocktailId: String): Result<Cocktail?> {
        return detailRepository.refreshCocktailById(cocktailId)
    }
}

