package com.cocktailcraft.testutil

import com.cocktailcraft.data.remote.CategoryDto
import com.cocktailcraft.data.remote.CocktailApi
import com.cocktailcraft.data.remote.CocktailDto
import com.cocktailcraft.data.remote.IngredientDto
import kotlinx.coroutines.delay

/**
 * In-memory [CocktailApi] for the data-layer repository tests. Lives with the
 * data tests (not :core:testing) because the API and its DTOs are internal
 * to the data layer.
 */
internal class FakeCocktailApi(var drinks: List<CocktailDto> = emptyList()) : CocktailApi {
    /** When set, name-search and filter.php endpoints throw it (to simulate an API failure). */
    var endpointError: Exception? = null

    override suspend fun searchCocktailsByName(name: String): List<CocktailDto> {
        endpointError?.let { throw it }
        return drinks.filter { it.name.contains(name, ignoreCase = true) }
    }

    override suspend fun getCocktailById(id: String): CocktailDto? = drinks.find { it.id == id }
    override suspend fun getRandomCocktail(): CocktailDto? = drinks.firstOrNull()

    override suspend fun filterByIngredient(ingredient: String): List<CocktailDto> {
        endpointError?.let { throw it }
        return drinks.filter { dto -> dto.getIngredients().any { it.name.contains(ingredient, ignoreCase = true) } }
    }

    override suspend fun filterByAlcoholic(alcoholic: Boolean): List<CocktailDto> {
        endpointError?.let { throw it }
        // The real server matches ?a=Non_Alcoholic against records whose
        // strAlcoholic is "Non alcoholic" — replicate that tolerance here.
        val target = if (alcoholic) "Alcoholic" else "Non alcoholic"
        return drinks.filter { it.alcoholic?.replace('_', ' ').equals(target, ignoreCase = true) }
    }

    /** When set, only filter.php?c= throws it — kept separate from [endpointError] so intersection tests can fail a later filter while category succeeds. */
    var categoryEndpointError: Exception? = null

    /**
     * When true, filter.php?c= strips matches down to the partial records the
     * real endpoint serves (id, name, thumbnail — no category/instructions).
     */
    var categoryFilterReturnsPartialDtos = false

    /** filter.php?c= calls served so far, to assert request deduplication. */
    var categoryRequests = 0
        private set

    /** When > 0, filter.php?c= suspends this long first (a slow network). */
    var categoryDelayMs = 0L

    override suspend fun filterByCategory(category: String): List<CocktailDto> {
        categoryRequests++
        if (categoryDelayMs > 0) delay(categoryDelayMs)
        categoryEndpointError?.let { throw it }
        val matches = drinks.filter { it.category == category }
        return if (categoryFilterReturnsPartialDtos) {
            matches.map { CocktailDto(id = it.id, name = it.name, imageUrl = it.imageUrl) }
        } else {
            matches
        }
    }

    /** list.php?c=list payload — seeded by catalog tests, empty by default. */
    var categories: List<CategoryDto> = emptyList()

    /** list.php?i=list payload — seeded by catalog tests, empty by default. */
    var ingredients: List<IngredientDto> = emptyList()

    override suspend fun getCategories(): List<CategoryDto> = categories
    override suspend fun getIngredients(): List<IngredientDto> = ingredients
}
