package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.testutil.testCocktail
import kotlin.test.Test
import kotlin.test.assertEquals

class SortCocktailsUseCaseTest {

    private val useCase = SortCocktailsUseCase()

    // Distinct values per axis so each sort produces a unique order.
    private val cocktails = listOf(
        testCocktail("a", name = "Banana Daiquiri", price = Money(1200), rating = 3.5f, popularity = 10),
        testCocktail("b", name = "Aperol Spritz", price = Money(800), rating = 5.0f, popularity = 99),
        testCocktail("c", name = "Cosmopolitan", price = Money(1000), rating = 4.0f, popularity = 50)
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
    fun nameSortIgnoresCase() {
        val mixed = listOf(testCocktail("z", name = "Zombie"), testCocktail("m", name = "mai tai"))

        assertEquals(listOf("m", "z"), useCase(mixed, SortCocktailsUseCase.SortType.NAME_ASC).map(Cocktail::id))
        assertEquals(listOf("z", "m"), useCase(mixed, SortCocktailsUseCase.SortType.NAME_DESC).map(Cocktail::id))
    }

    @Test
    fun ratingSortRanksBySuppliedRating() {
        // Review averages that invert the stored ratings
        val reviewed = mapOf("a" to 5.0f, "b" to 1.0f, "c" to 3.0f)

        val sorted = useCase(cocktails, SortCocktailsUseCase.SortType.RATING) { reviewed.getValue(it.id) }

        assertEquals(listOf("a", "c", "b"), sorted.map(Cocktail::id))
    }

    @Test
    fun sortingDoesNotMutateTheInputList() {
        val before = cocktails.map(Cocktail::id)

        useCase(cocktails, SortCocktailsUseCase.SortType.PRICE_ASC)

        assertEquals(before, cocktails.map(Cocktail::id))
    }
}
