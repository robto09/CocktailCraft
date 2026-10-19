package com.cocktailcraft.data.repository

import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.runCatchingResult
import kotlinx.coroutines.flow.Flow

/**
 * Catalog lookups (categories, ingredients) and category browsing, extracted
 * from the former CocktailRepositoryImpl monolith. Category pages are read
 * from and refreshed into the local catalog by [CocktailCategoryFetcher],
 * which also backs [CocktailSearchRepositoryImpl.filterByCategory].
 */
internal class CocktailCatalogRepositoryImpl(
    private val remote: CocktailRemoteDataSource,
    private val categoryFetcher: CocktailCategoryFetcher
) : CocktailCatalogRepository {

    override suspend fun getCategories(): Result<List<String>> = runCatchingResult("Failed to get categories") {
        Result.Success(remote.getCategories())
    }

    override suspend fun getIngredients(): Result<List<String>> = runCatchingResult("Failed to get ingredients") {
        Result.Success(remote.getIngredients())
    }

    override fun observeCategory(category: String): Flow<List<Cocktail>> =
        categoryFetcher.observe(category)

    override suspend fun refreshCategory(category: String, force: Boolean): Result<Unit> =
        categoryFetcher.refresh(category, force)
}
