package com.cocktailcraft.data.repository

import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.util.Result

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
        return try {
            Result.Success(remote.getCategories())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get categories")
        }
    }

    override suspend fun getIngredients(): Result<List<String>> {
        return try {
            Result.Success(remote.getIngredients())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get ingredients")
        }
    }

    override suspend fun getCocktailsSortedByNewest(): Result<List<Cocktail>> {
        return try {
            Result.Success(categoryFetcher.fetchCocktailsByCategory("Cocktail"))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get cocktails sorted by newest")
        }
    }
}
