package com.cocktailcraft.data.repository

import app.cash.turbine.test
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.Money
import com.cocktailcraft.domain.model.sumOfMoney
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.createTestDatabase
import com.cocktailcraft.testutil.createTestLocalDatabase
import com.cocktailcraft.testutil.testCocktail
import com.cocktailcraft.testutil.testJson
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CartRepositoryImplTest {

    private val db = createTestDatabase()
    private val localDatabase = createTestLocalDatabase(db)

    // A new repository over the same database simulates an app relaunch.
    private fun repository() = CartRepositoryImpl(localDatabase, testJson)

    @Test
    fun addToCartStoresItemAndMergesDuplicates() = runTest {
        val repository = repository()

        repository.addToCart(CocktailCartItem(testCocktail("1", price = Money(1000)), quantity = 1)).getOrThrow()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = Money(1000)), quantity = 2)).getOrThrow()

        val items = repository.getCartItems().getOrThrow()
        assertEquals(1, items.size)
        assertEquals(3, items.single().quantity)
        assertEquals(Money(3000), repository.getCartItems().getOrThrow().sumOfMoney { it.lineTotal })
    }

    @Test
    fun addAllToCartAppendsNewItemsAndGrowsExistingOnes() = runTest {
        val repository = repository()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = Money(1250)), quantity = 1)).getOrThrow()

        repository.addAllToCart(
            listOf(
                CocktailCartItem(testCocktail("1", price = Money(1250)), quantity = 2),
                CocktailCartItem(testCocktail("2", price = Money(999)), quantity = 1)
            )
        ).getOrThrow()

        val items = repository.getCartItems().getOrThrow()
        assertEquals(listOf("1" to 3, "2" to 1), items.map { it.cocktail.id to it.quantity })
        assertEquals(Money(3 * 1250 + 999), repository.getCartItems().getOrThrow().sumOfMoney { it.lineTotal })
    }

    @Test
    fun mergingADuplicateKeepsTheStoredCocktailAndPosition() = runTest {
        val repository = repository()
        repository.addToCart(CocktailCartItem(testCocktail("1", name = "First"), quantity = 1)).getOrThrow()
        repository.addToCart(CocktailCartItem(testCocktail("2"), quantity = 1)).getOrThrow()

        repository.addToCart(CocktailCartItem(testCocktail("1", name = "Second"), quantity = 1)).getOrThrow()

        val items = repository.getCartItems().getOrThrow()
        assertEquals(listOf("1", "2"), items.map { it.cocktail.id })
        assertEquals("First", items.first().cocktail.name)
    }

    @Test
    fun removeFromCartDeletesOnlyThatItem() = runTest {
        val repository = repository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1))
        repository.addToCart(CocktailCartItem(testCocktail("2"), quantity = 1))

        repository.removeFromCart("1").getOrThrow()

        assertEquals(listOf("2"), repository.getCartItems().getOrThrow().map { it.cocktail.id })
    }

    @Test
    fun updateQuantityChangesItemOrRemovesItAtZero() = runTest {
        val repository = repository()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = Money(1000)), quantity = 1))

        repository.updateQuantity("1", 5).getOrThrow()
        assertEquals(5, repository.getCartItems().getOrThrow().single().quantity)
        assertEquals(Money(5000), repository.getCartItems().getOrThrow().sumOfMoney { it.lineTotal })

        repository.updateQuantity("1", 0).getOrThrow()
        assertTrue(repository.getCartItems().getOrThrow().isEmpty())
    }

    @Test
    fun clearCartEmptiesItemsAndPersistedState() = runTest {
        val repository = repository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 2))

        repository.clearCart().getOrThrow()

        assertTrue(repository.getCartItems().getOrThrow().isEmpty())
        assertTrue(repository().getCartItems().getOrThrow().isEmpty(), "persisted cart must be empty")
    }

    @Test
    fun cartSurvivesRepositoryRecreation() = runTest {
        val first = repository()
        first.addToCart(CocktailCartItem(testCocktail("1", price = Money(1250)), quantity = 2))
        first.addToCart(CocktailCartItem(testCocktail("2", price = Money(800)), quantity = 1))

        val second = repository()
        val items = second.getCartItems().getOrThrow()

        assertEquals(listOf("1", "2"), items.map { it.cocktail.id })
        assertEquals(listOf(2, 1), items.map { it.quantity })
        assertEquals(testCocktail("1", price = Money(1250)), items.first().cocktail)
        assertEquals(Money(3300), second.getCartItems().getOrThrow().sumOfMoney { it.lineTotal })
    }

    @Test
    fun unreadableStoredItemIsSkippedNotFatal() = runTest {
        repository().addToCart(CocktailCartItem(testCocktail("1"), quantity = 1)).getOrThrow()
        db.cartItemQueries.insert(cocktailId = "broken", payload = "{not valid json", quantity = 1)

        assertEquals(listOf("1"), repository().getCartItems().getOrThrow().map { it.cocktail.id })
    }

    @Test
    fun observeCartItemsEmitsAfterEveryChange() = runTest {
        val repository = repository()

        repository.observeCartItems().test {
            assertEquals(emptyList(), awaitItem())

            repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 1)).getOrThrow()
            assertEquals(listOf(1), awaitItem().map { it.quantity })

            repository.updateQuantity("1", 4).getOrThrow()
            assertEquals(listOf(4), awaitItem().map { it.quantity })

            repository.clearCart().getOrThrow()
            assertEquals(emptyList(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
