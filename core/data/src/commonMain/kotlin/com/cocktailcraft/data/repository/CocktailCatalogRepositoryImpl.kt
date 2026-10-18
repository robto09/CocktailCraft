package com.cocktailcraft.data.repository

import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult

/**
 * Catalog lookups (categories, ingredients), extracted from the former
 * CocktailRepositoryImpl monolith. Browsing a category's cocktails goes
 * through [CocktailSearchRepositoryImpl.filterByCategory].
 */
internal class CocktailCatalogRepositoryImpl(
    private val remote: CocktailRemoteDataSource
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
}
