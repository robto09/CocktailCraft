package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result

internal class SearchCocktailsUseCase(
    private val searchRepository: CocktailSearchRepository
) {
    /** Single entry point: run an advanced search over the 5 supported filters. */
    suspend fun search(filters: SearchFilters): Result<List<Cocktail>> =
        searchRepository.advancedSearch(filters)

    /** Convenience for query-only callers. */
    suspend operator fun invoke(query: String): Result<List<Cocktail>> =
        search(SearchFilters(query = query))
}
