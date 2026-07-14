package com.cocktailcraft.data.repository

import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCategories
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult

/**
 * Catalog/browsing lookups (categories, ingredients, default cocktail list),
 * extracted from the former CocktailRepositoryImpl monolith. The default list
 * shares the cached fetch path in [CocktailCategoryFetcher] with the search
 * repository's category filter.
 */
internal class CocktailCatalogRepositoryImpl(
    private val remote: CocktailRemoteDataSource,
    private val categoryFetcher: CocktailCategoryFetcher
) : CocktailCatalogRepository {

    override suspend fun getCategories(): Result<List<String>> {
        return runCatchingResult("Failed to get categories") {
            Result.Success(remote.getCategories())
        }
    }

    override suspend fun getIngredients(): Result<List<String>> {
        return runCatchingResult("Failed to get ingredients") {
            Result.Success(remote.getIngredients())
        }
    }

    override suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>> {
        return runCatchingResult("Failed to get cocktails sorted by newest") {
            Result.Success(categoryFetcher.fetchCocktailsByCategory(CocktailCategories.DEFAULT))
        }
    }
}
