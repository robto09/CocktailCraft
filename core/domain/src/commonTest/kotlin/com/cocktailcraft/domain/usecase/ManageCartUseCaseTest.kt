package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.partialCocktail
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ManageCartUseCaseTest {

    /** Detail lookups counted (and optionally failed) to pin down hydration behavior. */
    private class TrackingDetailRepository(
        private val byId: Map<String, Cocktail> = emptyMap()
    ) : CocktailDetailRepository {
        var lookups = 0
            private set
        var throwOnLookup = false

        override suspend fun getCocktailById(id: String): Result<Cocktail?> {
            lookups++
            if (throwOnLookup) throw IllegalStateException("lookup failed")
            return Result.Success(byId[id])
        }

        override suspend fun refreshCocktailById(id: String): Result<Cocktail?> = getCocktailById(id)
        override suspend fun getRandomCocktail(): Result<Cocktail?> = Result.Success(byId.values.firstOrNull())
        override fun getCocktailImageUrl(cocktail: Cocktail): String = cocktail.imageUrl.orEmpty()
    }

    private val cart = FakeCartRepository()

    private suspend fun storedCocktail(): Cocktail = cart.getCartItems().getOrThrow().single().cocktail

    @Test
    fun addToCartHydratesPartialCocktailBeforePersisting() = runTest {
        // List endpoints fabricate placeholder ingredients/instructions; the
        // stored item must carry the full details from the lookup endpoint.
        val full = testCocktail("1")
        val detail = TrackingDetailRepository(byId = mapOf("1" to full))
        val useCase = ManageCartUseCase(cart, detail)

        useCase.addToCart(partialCocktail("1"), quantity = 2)

        assertEquals(1, detail.lookups)
        assertTrue(storedCocktail().hasFullDetails, "stored item must be hydrated")
        assertEquals(full.ingredients, storedCocktail().ingredients)
        assertEquals(2, cart.getCartItems().getOrThrow().single().quantity)
    }

    @Test
    fun addToCartSkipsLookupWhenDetailsAreAlreadyFull() = runTest {
        val detail = TrackingDetailRepository()
        val useCase = ManageCartUseCase(cart, detail)

        useCase.addToCart(testCocktail("1"))

        assertEquals(0, detail.lookups, "a full cocktail must not trigger a detail fetch")
        assertTrue(storedCocktail().hasFullDetails)
    }

    @Test
    fun addToCartFallsBackToPartialWhenLookupFindsNothing() = runTest {
        val detail = TrackingDetailRepository() // byId empty: lookup returns null
        val useCase = ManageCartUseCase(cart, detail)

        useCase.addToCart(partialCocktail("1"))

        val stored = storedCocktail()
        assertEquals("1", stored.id, "the partial item must still be stored")
        assertFalse(stored.hasFullDetails)
    }

    @Test
    fun addToCartFallsBackToPartialWhenLookupThrows() = runTest {
        // Offline / API failure during hydration must not block adding to cart.
        val detail = TrackingDetailRepository().apply { throwOnLookup = true }
        val useCase = ManageCartUseCase(cart, detail)

        val result = useCase.addToCart(partialCocktail("1"))

        assertTrue(result.isSuccess())
        assertFalse(storedCocktail().hasFullDetails)
    }

    @Test
    fun addToCartWithCartItemHydratesAndKeepsQuantity() = runTest {
        val full = testCocktail("1")
        val detail = TrackingDetailRepository(byId = mapOf("1" to full))
        val useCase = ManageCartUseCase(cart, detail)

        useCase.addToCart(CocktailCartItem(partialCocktail("1"), quantity = 3))

        assertTrue(storedCocktail().hasFullDetails)
        assertEquals(3, cart.getCartItems().getOrThrow().single().quantity)
    }
}
