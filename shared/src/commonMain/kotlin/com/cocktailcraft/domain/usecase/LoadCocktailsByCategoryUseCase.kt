package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result

internal class LoadCocktailsByCategoryUseCase(
    private val searchRepository: CocktailSearchRepository
) {
    suspend operator fun invoke(category: String): Result<List<Cocktail>> {
        return searchRepository.filterByCategory(category)
    }

    /**
     * Slice out the next page ([page] is 0-indexed: page 1 = items 10..19 for
     * a size-10 page, following the first page loaded by [invoke]).
     * Failures propagate as [Result.Error] so callers can tell a failed fetch
     * apart from a genuinely empty final page.
     */
    suspend fun loadMore(category: String, page: Int, pageSize: Int): Result<List<Cocktail>> {
        return searchRepository.filterByCategory(category).map { cocktails ->
            cocktails.drop(page * pageSize).take(pageSize)
        }
    }
}

