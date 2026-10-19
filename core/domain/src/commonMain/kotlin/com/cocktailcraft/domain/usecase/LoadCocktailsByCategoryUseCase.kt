package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.native.HiddenFromObjC

/**
 * Category browsing over the database-backed category pages of
 * [CocktailCatalogRepository]: [observe] for live updates, [invoke] for a
 * refresh-then-read snapshot. A stored page is the whole category; callers
 * page through it themselves.
 */
@HiddenFromObjC // Public for :presentation; not Swift API (AR-12)
class LoadCocktailsByCategoryUseCase(private val catalogRepository: CocktailCatalogRepository) {
    /** The stored page of [category], re-emitted whenever it changes (e.g. after a refresh). */
    fun observe(category: String): Flow<List<Cocktail>> = catalogRepository.observeCategory(category)

    /**
     * Refresh [category] (when stale, or always with [forceRefresh]) and
     * return the stored page. A failed refresh still returns what is stored;
     * only a failure with nothing stored is a [Result.Error], so callers can
     * tell a failed fetch from a genuinely empty category.
     */
    suspend operator fun invoke(category: String, forceRefresh: Boolean = false): Result<List<Cocktail>> {
        val refreshed = catalogRepository.refreshCategory(category, force = forceRefresh)
        val stored = catalogRepository.observeCategory(category).first()
        return if (stored.isEmpty() && refreshed is Result.Error) refreshed else Result.Success(stored)
    }
}
