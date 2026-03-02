package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault

class SearchCocktailsUseCase(
    private val searchRepository: CocktailSearchRepository
) {
    suspend operator fun invoke(query: String): Result<List<Cocktail>> {
        return searchRepository.searchCocktailsByName(query)
    }

    suspend fun advancedSearch(filters: SearchFilters): Result<List<Cocktail>> {
        return searchRepository.advancedSearch(filters)
    }
}

