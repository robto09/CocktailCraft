package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailSearchRepository
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault

internal class LoadCocktailsByCategoryUseCase(
    private val searchRepository: CocktailSearchRepository,
    private val catalogRepository: CocktailCatalogRepository
) {
    suspend operator fun invoke(category: String): Result<List<Cocktail>> {
        return searchRepository.filterByCategory(category)
    }

    suspend fun loadPage(category: String, page: Int, pageSize: Int): Result<List<Cocktail>> {
        val allResult = searchRepository.filterByCategory(category)
        return allResult.map { cocktails ->
            val startIndex = (page - 1) * pageSize
            cocktails.drop(startIndex).take(pageSize)
        }
    }

    suspend fun loadMore(allCocktails: List<Cocktail>, page: Int, pageSize: Int): List<Cocktail> {
        val catalogResult = catalogRepository.getCocktailsSortedByNewest().getOrDefault(emptyList())
        val startIndex = page * pageSize
        return catalogResult.drop(startIndex).take(pageSize)
    }
}

