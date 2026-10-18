package com.cocktailcraft.data.repository

import com.cocktailcraft.data.remote.ApiRateLimiter
import com.cocktailcraft.data.remote.CategoryDto
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.data.remote.IngredientDto
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCocktailApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CocktailCatalogRepositoryImplTest {

    // Mirrors the DataModule wiring: the catalog impl only needs the remote source.
    private fun repository(api: FakeCocktailApi = FakeCocktailApi()): CocktailCatalogRepositoryImpl =
        CocktailCatalogRepositoryImpl(remote = CocktailRemoteDataSource(api, ApiRateLimiter()))

    // --- getCategories / getIngredients: list.php DTO name mapping ---

    @Test
    fun getCategoriesMapsDtoNames() = runTest {
        val api = FakeCocktailApi()
        api.categories = listOf(CategoryDto("Cocktail"), CategoryDto("Shot"))
        val repo = repository(api = api)

        val result = repo.getCategories()

        assertEquals(listOf("Cocktail", "Shot"), (result as Result.Success).data)
    }

    @Test
    fun getIngredientsMapsDtoNames() = runTest {
        val api = FakeCocktailApi()
        api.ingredients = listOf(IngredientDto("Gin"), IngredientDto("Tequila"))
        val repo = repository(api = api)

        val result = repo.getIngredients()

        assertEquals(listOf("Gin", "Tequila"), (result as Result.Success).data)
    }
}
