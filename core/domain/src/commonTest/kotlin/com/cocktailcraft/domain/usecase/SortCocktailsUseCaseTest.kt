package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.testutil.testCocktail
import kotlin.test.Test
import kotlin.test.assertEquals

class SortCocktailsUseCaseTest {

    private val useCase = SortCocktailsUseCase()

    // Distinct values per axis so each sort produces a unique order.
    private val cocktails = listOf(
        testCocktail("a", name = "Banana Daiquiri", price = 12.0, rating = 3.5f, popularity = 10),
        testCocktail("b", name = "Aperol Spritz", price = 8.0, rating = 5.0f, popularity = 99),
        testCocktail("c", name = "Cosmopolitan", price = 10.0, rating = 4.0f, popularity = 50)
    )

    private fun sortedIds(sortType: SortCocktailsUseCase.SortType): List<String> =
        useCase(cocktails, sortType).map(Cocktail::id)

    @Test
    fun sortsByPriceAscending() {
        assertEquals(listOf("b", "c", "a"), sortedIds(SortCocktailsUseCase.SortType.PRICE_ASC))
    }

    @Test
    fun sortsByPriceDescending() {
        assertEquals(listOf("a", "c", "b"), sortedIds(SortCocktailsUseCase.SortType.PRICE_DESC))
    }

    @Test
    fun sortsByRatingDescending() {
        assertEquals(listOf("b", "c", "a"), sortedIds(SortCocktailsUseCase.SortType.RATING))
    }

    @Test
    fun sortsByPopularityDescending() {
        assertEquals(listOf("b", "c", "a"), sortedIds(SortCocktailsUseCase.SortType.POPULARITY))
    }

    @Test
    fun sortsByNameAscending() {
        assertEquals(listOf("b", "a", "c"), sortedIds(SortCocktailsUseCase.SortType.NAME_ASC))
    }

    @Test
    fun sortsByNameDescending() {
        assertEquals(listOf("c", "a", "b"), sortedIds(SortCocktailsUseCase.SortType.NAME_DESC))
    }

    @Test
    fun sortingDoesNotMutateTheInputList() {
        val before = cocktails.map(Cocktail::id)

        useCase(cocktails, SortCocktailsUseCase.SortType.PRICE_ASC)

        assertEquals(before, cocktails.map(Cocktail::id))
    }
}
