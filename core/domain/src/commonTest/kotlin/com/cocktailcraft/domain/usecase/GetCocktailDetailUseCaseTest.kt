package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeFavoritesRepository
import com.cocktailcraft.testutil.FakeSearchRepository
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetCocktailDetailUseCaseTest {

    private val detail = FakeDetailRepository()
    private val favorites = FakeFavoritesRepository()
    private val search = FakeSearchRepository()

    private val useCase = GetCocktailDetailUseCase(
        detailRepository = detail,
        favoritesRepository = favorites,
        searchRepository = search
    )

    @Test
    fun invokeReturnsCocktailFromDetailRepository() = runTest {
        detail.byId["1"] = testCocktail("1", name = "Margarita")

        val result = useCase("1")

        assertEquals("Margarita", (result as Result.Success).data?.name)
    }

    @Test
    fun invokeReturnsNullSuccessForUnknownId() = runTest {
        val result = useCase("404")

        assertNull((result as Result.Success).data)
    }

    @Test
    fun isFavoriteTrueOnlyWhenIdIsAmongStoredFavorites() = runTest {
        favorites.addToFavorites(testCocktail("1"))

        assertTrue(useCase.isFavorite("1"))
        assertFalse(useCase.isFavorite("2"))
    }

    // --- Related cocktails: same category, excluding the cocktail itself ---

    @Test
    fun relatedCocktailsComeFromSameCategoryExcludingSelf() = runTest {
        val self = testCocktail("1", category = "Cocktail")
        search.all = listOf(
            self,
            testCocktail("2", category = "Cocktail"),
            testCocktail("3", category = "Cocktail"),
            testCocktail("4", category = "Cocktail"),
            testCocktail("5", category = "Cocktail"),
            testCocktail("9", category = "Shot")
        )

        val related = useCase.getRelatedCocktails(self, limit = 3)

        assertEquals(3, related.size)
        val ids = related.map(Cocktail::id).toSet()
        assertFalse("1" in ids, "a cocktail must never relate to itself")
        assertTrue(ids.all { it in setOf("2", "3", "4", "5") }, "unexpected ids: $ids")
    }

    @Test
    fun relatedCocktailsFallBackToDefaultCategoryWhenCocktailHasNone() = runTest {
        // testCocktail's default category "Cocktail" == CocktailCategories.DEFAULT
        search.all = listOf(testCocktail("2"), testCocktail("3"))

        val related = useCase.getRelatedCocktails(testCocktail("1", category = null))

        assertEquals(setOf("2", "3"), related.map(Cocktail::id).toSet())
    }

    @Test
    fun relatedCocktailsAreEmptyWhenSearchFails() = runTest {
        search.all = listOf(testCocktail("2"))
        search.errorMessage = "API down"

        val related = useCase.getRelatedCocktails(testCocktail("1"))

        assertTrue(related.isEmpty(), "a failed category lookup must degrade to no suggestions")
    }

    @Test
    fun refreshReturnsLatestFromDetailRepository() = runTest {
        detail.byId["1"] = testCocktail("1", name = "Refreshed Margarita")

        val result = useCase.refresh("1")

        assertEquals("Refreshed Margarita", (result as Result.Success).data?.name)
    }
}
