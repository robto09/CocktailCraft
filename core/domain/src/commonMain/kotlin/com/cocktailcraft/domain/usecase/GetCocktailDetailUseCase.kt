package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.repository.CocktailFavoritesRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.runCatchingNonCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.native.HiddenFromObjC

@HiddenFromObjC // Public for :presentation; not Swift API (AR-12)
class GetCocktailDetailUseCase(
    private val detailRepository: CocktailDetailRepository,
    private val favoritesRepository: CocktailFavoritesRepository,
    private val catalogRepository: CocktailCatalogRepository
) {
    suspend operator fun invoke(cocktailId: String): Result<Cocktail?> = detailRepository.getCocktailById(cocktailId)

    /** The stored cocktail, re-emitted whenever it changes (e.g. after a refresh). */
    fun observe(cocktailId: String): Flow<Cocktail?> = detailRepository.observeCocktail(cocktailId)

    /**
     * Background revalidation: refetch the cocktail only when the stored copy
     * is stale. The result reaches screens through [observe].
     */
    suspend fun refreshIfStale(cocktailId: String): Result<Unit> =
        detailRepository.refreshCocktail(cocktailId, force = false)

    suspend fun isFavorite(cocktailId: String): Boolean {
        // Point lookup, not a load (and decode) of every stored favorite
        return runCatchingNonCancellation(onFailure = { false }) {
            favoritesRepository.isCocktailFavorite(cocktailId).getOrDefault(false)
        }
    }

    /**
     * Up to [limit] other cocktails of [cocktail]'s category, picked at
     * random from the category's stored page. Only a category with nothing
     * stored is fetched first: a handful of suggestions doesn't justify a
     * rate-limited filter.php call and a page of writes on every detail view.
     */
    suspend fun getRelatedCocktails(
        cocktail: Cocktail,
        limit: Int = 3
    ): List<Cocktail> = runCatchingNonCancellation(onFailure = {
        emptyList()
    }) {
        val category = cocktail.category?.takeIf { it.isNotBlank() } ?: CocktailCategories.DEFAULT
        val stored = catalogRepository.observeCategory(category).first().ifEmpty {
            catalogRepository.refreshCategory(category, force = false)
            catalogRepository.observeCategory(category).first()
        }
        stored.filter { it.id != cocktail.id }.shuffled().take(limit)
    }

    /**
     * Force-refresh from the network, then return the stored copy. A failed
     * refresh (offline included) is a [Result.Error]; the stored copy is left
     * as it was.
     */
    suspend fun refresh(
        cocktailId: String
    ): Result<Cocktail?> = when (val refreshed = detailRepository.refreshCocktail(cocktailId, force = true)) {
        is Result.Success -> Result.Success(detailRepository.observeCocktail(cocktailId).first())
        is Result.Error -> refreshed
    }
}
