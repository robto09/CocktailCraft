package com.cocktailcraft.data.repository

import com.cocktailcraft.data.cache.CocktailCache
import com.cocktailcraft.data.cache.CocktailCacheManager
import com.cocktailcraft.data.cache.OfflineModePolicy
import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.data.remote.CategoryDto
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.CocktailRemoteDataSource
import com.cocktailcraft.data.remote.IngredientDto
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCocktailApi
import com.cocktailcraft.testutil.FakeNetworkMonitor
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CocktailCatalogRepositoryImplTest {

    private val config = AppConfigImpl()

    // Mirrors the DataModule wiring: remote source + focused offline repo +
    // the shared category fetcher backing the catalog impl.
    private fun repository(
        api: FakeCocktailApi = FakeCocktailApi(),
        settings: Settings = MapSettings(),
        online: Boolean = true
    ): CocktailCatalogRepositoryImpl {
        val networkMonitor = FakeNetworkMonitor(online)
        val policy = OfflineModePolicy(settings, config, networkMonitor)
        val cache = CocktailCache(settings, Json { ignoreUnknownKeys = true }, config, offlineModePolicy = policy)
        val cacheManager = CocktailCacheManager()
        val remote = CocktailRemoteDataSource(api, cacheManager)
        val offline = CocktailOfflineRepositoryImpl(
            offlineModePolicy = policy,
            cocktailCache = cache,
            cacheManager = cacheManager,
            remote = remote
        )
        val fetcher = CocktailCategoryFetcher(
            remote = remote,
            cocktailCache = cache,
            cacheManager = cacheManager,
            offlineRepository = offline
        )
        return CocktailCatalogRepositoryImpl(remote = remote, categoryFetcher = fetcher)
    }

    private fun drinkDto(id: String, name: String, category: String) = CocktailDto(
        id = id,
        name = name,
        category = category,
        alcoholic = "Alcoholic",
        glass = "Highball glass",
        instructions = "Mix well.",
        imageUrl = "https://example.com/$id.jpg",
        ingredient1 = "Rum",
        measure1 = "1 oz"
    )

    private fun sampleDrinks() = listOf(
        drinkDto("1", "Mojito", "Cocktail"),
        drinkDto("2", "Margarita", "Ordinary Drink"),
        drinkDto("3", "Virgin Mojito", "Cocktail")
    )

    // --- getCocktailsSortedByNewest: default-category browse via the fetcher ---

    @Test
    fun getCocktailsSortedByNewestReturnsMappedDefaultCategoryList() = runTest {
        val repo = repository(api = FakeCocktailApi(drinks = sampleDrinks()))

        val result = repo.getCocktailsSortedByNewest()

        // The default browse category is "Cocktail" (CocktailCategories.DEFAULT)
        val cocktails = (result as Result.Success).data
        assertEquals(setOf("1", "3"), cocktails.map { it.id }.toSet())
        val mojito = cocktails.first { it.id == "1" }
        assertEquals("Mojito", mojito.name)
        assertEquals("Cocktail", mojito.category)
        assertEquals("Rum", mojito.ingredients.first().name)
    }

    @Test
    fun getCocktailsSortedByNewestReturnsErrorWhenFetcherFailsWithNoCache() = runTest {
        val api = FakeCocktailApi(drinks = sampleDrinks())
        api.categoryEndpointError = RuntimeException("API down")
        val repo = repository(api = api)

        val result = repo.getCocktailsSortedByNewest()

        assertTrue(result is Result.Error, "a real failure with no cache must not become an empty success")
        assertEquals("API down", result.message)
    }

    @Test
    fun getCocktailsSortedByNewestServesCachedListWhenApiLaterFails() = runTest {
        val api = FakeCocktailApi(drinks = sampleDrinks())
        val repo = repository(api = api)

        // First fetch succeeds and populates the category cache...
        repo.getCocktailsSortedByNewest()

        // ...so a later API failure serves the cached list instead of erroring.
        api.categoryEndpointError = RuntimeException("API down")
        val result = repo.getCocktailsSortedByNewest()

        assertEquals(setOf("1", "3"), (result as Result.Success).data.map { it.id }.toSet())
    }

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
