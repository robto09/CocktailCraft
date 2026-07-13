package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
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

class ManageFavoritesUseCaseTest {

    private val favorites = FakeFavoritesRepository()
    private val detail = FakeDetailRepository()

    private val useCase = ManageFavoritesUseCase(
        favoritesRepository = favorites,
        detailRepository = detail
    )

    @Test
    fun toggleAddsCocktailWhenAbsent() = runTest {
        val result = useCase.toggle(testCocktail("1"))

        assertEquals(true, (result as Result.Success).data)
        assertEquals(listOf("1"), favorites.stored.map(Cocktail::id))
    }

    @Test
    fun toggleRemovesCocktailWhenPresent() = runTest {
        useCase.addToFavorites(testCocktail("1"))

        val result = useCase.toggle(testCocktail("1"))

        assertEquals(false, (result as Result.Success).data)
        assertTrue(favorites.stored.isEmpty())
    }

    // --- Add-time hydration: list endpoints fabricate placeholder details ---

    @Test
    fun toggleHydratesPartialCocktailBeforePersisting() = runTest {
        detail.byId["1"] = testCocktail("1", name = "Full Margarita")

        useCase.toggle(partialCocktail("1"))

        val stored = favorites.stored.single()
        assertTrue(stored.hasFullDetails, "stored favorite must carry full details")
        assertEquals("Full Margarita", stored.name)
        assertEquals("Gin", stored.ingredients.first().name)
    }

    @Test
    fun addToFavoritesHydratesPartialCocktailBeforePersisting() = runTest {
        detail.byId["1"] = testCocktail("1", name = "Full Margarita")

        useCase.addToFavorites(partialCocktail("1"))

        val stored = favorites.stored.single()
        assertTrue(stored.hasFullDetails)
        assertEquals("Full Margarita", stored.name)
    }

    @Test
    fun addToFavoritesKeepsPartialWhenDetailLookupComesUpEmpty() = runTest {
        // No detail record seeded: hydration falls back to the original
        // partial rather than failing the add.
        useCase.addToFavorites(partialCocktail("1", name = "Partial Margarita"))

        val stored = favorites.stored.single()
        assertFalse(stored.hasFullDetails)
        assertEquals("Partial Margarita", stored.name)
    }

    @Test
    fun toggleDoesNotRehydrateCocktailThatAlreadyHasFullDetails() = runTest {
        // A differing detail record would overwrite the passed cocktail if
        // hydration ran; full-details input must be persisted as-is.
        detail.byId["1"] = testCocktail("1", name = "Detail Version")

        useCase.toggle(testCocktail("1", name = "Original Version"))

        assertEquals("Original Version", favorites.stored.single().name)
    }

    @Test
    fun isFavoriteReflectsStoredState() = runTest {
        assertFalse(useCase.isFavorite("1"))

        useCase.addToFavorites(testCocktail("1"))

        assertTrue(useCase.isFavorite("1"))
        assertFalse(useCase.isFavorite("2"))
    }

    @Test
    fun loadFavoritesReturnsStoredCocktails() = runTest {
        useCase.addToFavorites(testCocktail("1"))
        useCase.addToFavorites(testCocktail("2"))

        val result = useCase.loadFavorites()

        assertEquals(listOf("1", "2"), (result as Result.Success).data.map(Cocktail::id))
    }

    @Test
    fun removeFromFavoritesRemovesOnlyThatCocktail() = runTest {
        useCase.addToFavorites(testCocktail("1"))
        useCase.addToFavorites(testCocktail("2"))

        useCase.removeFromFavorites(testCocktail("1"))

        assertEquals(listOf("2"), favorites.stored.map(Cocktail::id))
    }
}
