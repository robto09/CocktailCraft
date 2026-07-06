package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.FakeFavoritesRepository
import com.cocktailcraft.testutil.partialCocktail
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for ManageFavoritesUseCase.toggle and its hydration behavior
 * (list-endpoint cocktails carry placeholder ingredients and must be
 * hydrated from the detail repository before being persisted).
 */
class ToggleFavoriteUseCaseTest {

    private val favorites = FakeFavoritesRepository()
    private val details = FakeDetailRepository()
    private val useCase = ManageFavoritesUseCase(favorites, details)

    @Test
    fun toggleAddsWhenNotFavoriteAndReturnsTrue() = runTest {
        val result = useCase.toggle(testCocktail("1"))

        assertEquals(true, (result as Result.Success).data)
        assertTrue(useCase.isFavorite("1"))
    }

    @Test
    fun toggleRemovesWhenAlreadyFavoriteAndReturnsFalse() = runTest {
        val cocktail = testCocktail("1")
        favorites.addToFavorites(cocktail)

        val result = useCase.toggle(cocktail)

        assertEquals(false, (result as Result.Success).data)
        assertFalse(useCase.isFavorite("1"))
    }

    @Test
    fun addToFavoritesHydratesPartialCocktailFromDetailRepository() = runTest {
        val full = testCocktail("7", name = "Negroni")
        details.byId["7"] = full

        useCase.addToFavorites(partialCocktail("7", name = "Negroni"))

        val stored = favorites.stored.single()
        assertTrue(stored.hasFullDetails, "persisted favorite must be hydrated, not the placeholder version")
        assertEquals(full.ingredients, stored.ingredients)
    }

    @Test
    fun addToFavoritesFallsBackToPartialWhenDetailLookupFails() = runTest {
        val partial = partialCocktail("9")

        useCase.addToFavorites(partial) // details repo has no entry for "9"

        val stored = favorites.stored.single()
        assertEquals("9", stored.id)
        assertFalse(stored.hasFullDetails)
    }

    @Test
    fun loadFavoritesReturnsPersistedCocktails() = runTest {
        favorites.addToFavorites(testCocktail("1"))
        favorites.addToFavorites(testCocktail("2"))

        val result = useCase.loadFavorites()

        assertEquals(listOf("1", "2"), (result as Result.Success).data.map { it.id })
    }
}
