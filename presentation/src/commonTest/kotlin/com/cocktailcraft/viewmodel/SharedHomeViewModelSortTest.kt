package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.viewmodel.state.CocktailRatingSummary
import com.cocktailcraft.viewmodel.state.HomeSortOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Home's sort menu: every option, and the sort holding across listing changes. */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedHomeViewModelSortTest : MainDispatcherTest() {

    // --- Sort menu ---

    /** Distinct values per axis, listed in an order no sort produces. */
    private val sortable = listOf(
        testCocktail("b", name = "banana Daiquiri", price = Money(1200), rating = 3.5f, popularity = 10),
        testCocktail("a", name = "Aperol Spritz", price = Money(800), rating = 5.0f, popularity = 50),
        testCocktail("c", name = "Cosmopolitan", price = Money(1000), rating = 4.0f, popularity = 99)
    )

    private suspend fun TestScope.sortedIds(option: HomeSortOption): List<String> {
        val harness = HomeViewModelHarness()
        harness.all = sortable
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.setSortOption(option)
        assertEquals(option, vm.uiState.value.sortOption)
        return vm.uiState.value.cocktails.map { it.id }
    }

    @Test
    fun defaultSortShowsTheCatalogOrder() = runTest {
        assertEquals(listOf("b", "a", "c"), sortedIds(HomeSortOption.DEFAULT))
    }

    @Test
    fun nameSortIsAlphabeticalIgnoringCase() = runTest {
        assertEquals(listOf("a", "b", "c"), sortedIds(HomeSortOption.NAME_ASC))
    }

    @Test
    fun priceLowToHighSort() = runTest {
        assertEquals(listOf("a", "c", "b"), sortedIds(HomeSortOption.PRICE_ASC))
    }

    @Test
    fun priceHighToLowSort() = runTest {
        assertEquals(listOf("b", "c", "a"), sortedIds(HomeSortOption.PRICE_DESC))
    }

    @Test
    fun ratingSortPutsTheBestRatedFirst() = runTest {
        assertEquals(listOf("a", "c", "b"), sortedIds(HomeSortOption.RATING))
    }

    @Test
    fun popularitySortPutsTheMostPopularFirst() = runTest {
        assertEquals(listOf("c", "a", "b"), sortedIds(HomeSortOption.POPULARITY))
    }

    @Test
    fun switchingBackToDefaultRestoresTheCatalogOrder() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = sortable
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setSortOption(HomeSortOption.NAME_ASC)
        vm.setSortOption(HomeSortOption.DEFAULT)

        assertEquals(listOf("b", "a", "c"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun sortIsKeptAcrossCategoryChanges() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = sortable + listOf(
            testCocktail("s1", category = "Shot", price = Money(900)),
            testCocktail("s2", category = "Shot", price = Money(300)),
            testCocktail("s3", category = "Shot", price = Money(600))
        )
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.setSortOption(HomeSortOption.PRICE_ASC)

        vm.loadCocktailsByCategory("Shot")
        advanceUntilIdle()

        assertEquals(HomeSortOption.PRICE_ASC, vm.uiState.value.sortOption)
        assertEquals(listOf("s2", "s3", "s1"), vm.uiState.value.cocktails.map { it.id })

        vm.loadCocktailsByCategory(null)
        advanceUntilIdle()

        assertEquals(listOf("a", "c", "b"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun sortStillAppliesWhenTheDatabaseReEmitsDifferentCocktails() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = sortable
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.setSortOption(HomeSortOption.PRICE_ASC)

        // A background refresh replaces the stored page with a new listing
        harness.catalog.store(
            "Cocktail",
            listOf(
                testCocktail("x", price = Money(2000)),
                testCocktail("a", price = Money(800)),
                testCocktail("y", price = Money(100))
            )
        )
        advanceUntilIdle()

        assertEquals(listOf("y", "a", "x"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun sameCocktailsReEmittedUnderDefaultSortKeepTheirPlaces() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = sortable
        val vm = harness.viewModel()
        advanceUntilIdle()

        // Same cocktails in a different stored order (e.g. a refresh): rows
        // must not jump under the user
        harness.catalog.store("Cocktail", sortable.reversed())
        advanceUntilIdle()

        assertEquals(listOf("b", "a", "c"), vm.uiState.value.cocktails.map { it.id })
    }

    @Test
    fun sortAppliesToSearchResultsAndLoadedPages() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = (1..15).map { testCocktail("$it", name = "Drink $it", price = Money((100L - it) * 100)) }
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.setSortOption(HomeSortOption.PRICE_ASC)

        vm.loadMoreCocktails()
        advanceUntilIdle()
        assertEquals(
            (15 downTo 1).map { "$it" },
            vm.uiState.value.cocktails.map { it.id },
            "a loaded page joins the listing in sort order"
        )

        vm.searchCocktails("Drink 1")
        assertEquals(listOf("15", "14", "13", "12", "11", "10", "1"), vm.uiState.value.cocktails.map { it.id })
    }

    // --- Ratings: review averages over the demo value ---

    @Test
    fun ratingSortUsesReviewAveragesAndFallsBackToTheDemoRating() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = sortable // demo ratings: a 5.0, c 4.0, b 3.5
        harness.reviews.addReview(review("b", 5f))
        harness.reviews.addReview(review("b", 4f))
        harness.reviews.addReview(review("a", 2f))
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setSortOption(HomeSortOption.RATING)

        // b: reviews 4.5, c: no reviews, demo 4.0, a: review 2.0
        assertEquals(listOf("b", "c", "a"), vm.uiState.value.cocktails.map { it.id })
        assertEquals(
            mapOf("b" to CocktailRatingSummary(4.5f, 2), "a" to CocktailRatingSummary(2f, 1)),
            vm.uiState.value.ratingSummaries
        )
    }

    @Test
    fun aNewReviewReordersTheRatingSort() = runTest {
        val harness = HomeViewModelHarness()
        harness.all = sortable
        val vm = harness.viewModel()
        advanceUntilIdle()
        vm.setSortOption(HomeSortOption.RATING)
        assertEquals(listOf("a", "c", "b"), vm.uiState.value.cocktails.map { it.id })

        harness.reviews.addReview(review("a", 1f))
        advanceUntilIdle()

        assertEquals(listOf("c", "b", "a"), vm.uiState.value.cocktails.map { it.id })
    }

    private fun review(cocktailId: String, rating: Float) =
        Review(cocktailId = cocktailId, userName = "Tester", rating = rating, comment = "A fine drink indeed.")
}
