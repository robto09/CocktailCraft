package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.CocktailRatingSummary
import com.cocktailcraft.domain.repository.CocktailCatalogRepository
import com.cocktailcraft.domain.usecase.SortCocktailsUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeCatalogRepository
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.viewmodel.state.HomeSortOption
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HomeListingTest {

    private val sort = SortCocktailsUseCase()
    private val a = testCocktail("a", name = "Alpha", rating = 2f)
    private val b = testCocktail("b", name = "Bravo", rating = 3f)
    private val c = testCocktail("c", name = "Charlie", rating = 4f)

    @Test
    fun defaultSortKeepsTheSourceOrder() {
        val source = listOf(c, a, b)
        assertSame(source, sort.sortListing(source, HomeSortOption.DEFAULT, emptyMap()))
    }

    @Test
    fun everyOtherOptionHasAUseCaseSort() {
        HomeSortOption.entries.filter { it != HomeSortOption.DEFAULT }.forEach { option ->
            assertTrue(option.sortType != null, "$option has no sort")
        }
    }

    @Test
    fun ratingSortPrefersTheReviewAverageOverTheDemoRating() {
        val ratings = mapOf("a" to CocktailRatingSummary(5f, 1))

        assertEquals(listOf(a, c, b), sort.sortListing(listOf(b, a, c), HomeSortOption.RATING, ratings))
    }

    @Test
    fun categoryPagesShowWholePagesAndKnowWhenThereIsMore() {
        val pages = CategoryPages(pageSize = 2)
        pages.all = listOf(a, b, c)

        assertEquals(listOf(a, b), pages.visible(pagesShown = 1))
        assertTrue(pages.hasMore(pagesShown = 1))
        assertEquals(listOf(a, b, c), pages.visible(pagesShown = 2))
        assertFalse(pages.hasMore(pagesShown = 2))
    }

    @Test
    fun keepingOrderKeepsCardsInPlaceWithTheLatestCopies() {
        val upgradedB = b.copy(instructions = "Shake hard.")

        assertEquals(listOf(c, upgradedB, a), keepingOrder(shown = listOf(c, b, a), latest = listOf(a, upgradedB, c)))
    }

    @Test
    fun keepingOrderTakesTheLatestOrderOnceTheCocktailsChange() {
        assertEquals(listOf(a, b), keepingOrder(shown = listOf(c, b), latest = listOf(a, b)))
    }

    @Test
    fun filterOptionsAreRequestedAgainOnlyAfterAFailure() = runTest {
        var calls = 0
        var failCategories = true
        val catalog = object : CocktailCatalogRepository by FakeCatalogRepository() {
            override suspend fun getCategories(): Result<List<String>> {
                calls++
                return if (failCategories) Result.Error("offline") else Result.Success(listOf("Shot"))
            }

            override suspend fun getIngredients(): Result<List<String>> = Result.Success(listOf("Gin"))
        }
        val loader = FilterOptionsLoader(catalog)
        val published = mutableListOf<Result<List<String>>>()

        loader.load(this) { categories, _ -> published += categories }
        advanceUntilIdle()
        failCategories = false
        loader.load(this) { categories, _ -> published += categories }
        advanceUntilIdle()
        loader.load(this) { categories, _ -> published += categories }
        advanceUntilIdle()

        assertEquals(2, calls, "retried after the failure, then never again")
        assertEquals(Result.Success(listOf("Shot")), published.last())
    }
}
