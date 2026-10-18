package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.OrderStatus
import com.cocktailcraft.domain.model.sumOfMoney
import com.cocktailcraft.domain.repository.CocktailDetailRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.FakeCartRepository
import com.cocktailcraft.testutil.FakeDetailRepository
import com.cocktailcraft.testutil.partialCocktail
import com.cocktailcraft.testutil.testCocktail
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

        override fun observeCocktail(id: String): Flow<Cocktail?> = flowOf(byId[id])
        override suspend fun refreshCocktail(id: String, force: Boolean): Result<Unit> = Result.Success(Unit)
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

    @Test
    fun hydrationRethrowsCancellationInsteadOfStoringThePartialCocktail() = runTest {
        // A cancelled caller must stop, not fall back to persisting the
        // placeholder record as if the lookup had merely failed.
        val detail = FakeDetailRepository().apply { throwOnLookup = CancellationException("left screen") }
        val useCase = ManageCartUseCase(cart, detail)

        assertFailsWith<CancellationException> { useCase.addToCart(partialCocktail("1")) }
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
    }

    private fun order(vararg items: OrderItem) = Order(
        id = "ORD-1",
        date = "2026-10-01",
        items = items.toList(),
        total = items.toList().sumOfMoney { it.lineTotal },
        status = OrderStatus.DELIVERED
    )

    @Test
    fun reorderAddsEveryItemWithItsQuantityAndCurrentDetails() = runTest {
        val margarita = testCocktail("11007", name = "Margarita", price = Money(1250))
        val mojito = testCocktail("11000", name = "Mojito", price = Money(999))
        val useCase = ManageCartUseCase(cart, TrackingDetailRepository(mapOf("11007" to margarita, "11000" to mojito)))

        val added = useCase.reorder(
            order(
                OrderItem(name = "Margarita", quantity = 2, price = Money(1250), cocktailId = "11007"),
                OrderItem(name = "Mojito", quantity = 1, price = Money(999), cocktailId = "11000")
            )
        )

        assertEquals(3, added.getOrThrow())
        assertEquals(
            listOf(CocktailCartItem(margarita, 2), CocktailCartItem(mojito, 1)),
            cart.getCartItems().getOrThrow()
        )
    }

    @Test
    fun reorderAddsToQuantitiesAlreadyInTheCart() = runTest {
        val margarita = testCocktail("11007", name = "Margarita")
        val useCase = ManageCartUseCase(cart, TrackingDetailRepository(mapOf("11007" to margarita)))
        useCase.addToCart(margarita, quantity = 1)

        useCase.reorder(order(OrderItem(name = "Margarita", quantity = 2, price = Money(1000), cocktailId = "11007")))

        assertEquals(3, cart.getCartItems().getOrThrow().single().quantity)
    }

    @Test
    fun reorderFallsBackToTheOrderedNameAndPriceWhenTheCocktailCantBeLookedUp() = runTest {
        val detail = TrackingDetailRepository().apply { throwOnLookup = true }
        val useCase = ManageCartUseCase(cart, detail)

        useCase.reorder(order(OrderItem(name = "Margarita", quantity = 2, price = Money(1250), cocktailId = "11007")))

        val item = cart.getCartItems().getOrThrow().single()
        assertEquals("11007", item.cocktail.id)
        assertEquals("Margarita", item.cocktail.name)
        assertEquals(Money(1250), item.cocktail.price)
        assertEquals(2, item.quantity)
    }

    @Test
    fun reorderRejectsOrdersWhoseItemsHaveNoCocktailIdAndLeavesTheCartAlone() = runTest {
        val useCase = ManageCartUseCase(cart, TrackingDetailRepository())

        val result = useCase.reorder(
            order(
                OrderItem(name = "Margarita", quantity = 2, price = Money(1250), cocktailId = "11007"),
                // An item migrated from before order items recorded their cocktail
                OrderItem(name = "Mojito", quantity = 1, price = Money(999), cocktailId = null)
            )
        )

        assertEquals(ErrorCode.INVALID_DATA, (result as Result.Error).code)
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
    }

    @Test
    fun aFailedReorderAddsNothing() = runTest {
        val useCase = ManageCartUseCase(cart, TrackingDetailRepository())
        cart.failNextMutation = true

        val result = useCase.reorder(
            order(
                OrderItem(name = "Margarita", quantity = 2, price = Money(1250), cocktailId = "11007"),
                OrderItem(name = "Mojito", quantity = 1, price = Money(999), cocktailId = "11000")
            )
        )

        assertTrue(result is Result.Error)
        assertTrue(cart.getCartItems().getOrThrow().isEmpty())
    }
}
