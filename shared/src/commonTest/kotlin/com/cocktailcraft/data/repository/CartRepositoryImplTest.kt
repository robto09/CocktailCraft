package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.testCocktail
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CartRepositoryImplTest {

    private val settings = MapSettings()
    private val json = Json { ignoreUnknownKeys = true }

    private fun repository() = CartRepositoryImpl(settings, json)

    @Test
    fun addToCartStoresItemAndMergesDuplicates() = runTest {
        val repository = repository()

        repository.addToCart(CocktailCartItem(testCocktail("1", price = 10.0), quantity = 1)).getOrThrow()
        repository.addToCart(CocktailCartItem(testCocktail("1", price = 10.0), quantity = 2)).getOrThrow()

        val items = repository.getCartItems().getOrThrow()
        assertEquals(1, items.size)
        assertEquals(3, items.single().quantity)
        assertEquals(30.0, repository.getCartTotal().getOrThrow())
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
        repository.addToCart(CocktailCartItem(testCocktail("1", price = 10.0), quantity = 1))

        repository.updateQuantity("1", 5).getOrThrow()
        assertEquals(5, repository.getCartItems().getOrThrow().single().quantity)
        assertEquals(50.0, repository.getCartTotal().getOrThrow())

        repository.updateQuantity("1", 0).getOrThrow()
        assertTrue(repository.getCartItems().getOrThrow().isEmpty())
    }

    @Test
    fun clearCartEmptiesItemsAndPersistedState() = runTest {
        val repository = repository()
        repository.addToCart(CocktailCartItem(testCocktail("1"), quantity = 2))

        repository.clearCart().getOrThrow()

        assertTrue(repository.getCartItems().getOrThrow().isEmpty())
        assertEquals(0.0, repository.getCartTotal().getOrThrow())
        assertNull(settings.getStringOrNull("cocktail_cart_items"), "persisted cart must be removed")
    }

    @Test
    fun cartSurvivesRepositoryRecreation() = runTest {
        val first = repository()
        first.addToCart(CocktailCartItem(testCocktail("1", price = 12.5), quantity = 2))
        first.addToCart(CocktailCartItem(testCocktail("2", price = 8.0), quantity = 1))

        // A new repository over the same settings simulates an app relaunch
        val second = repository()
        val items = second.getCartItems().getOrThrow()

        assertEquals(listOf("1", "2"), items.map { it.cocktail.id })
        assertEquals(listOf(2, 1), items.map { it.quantity })
        assertEquals(testCocktail("1", price = 12.5), items.first().cocktail)
        assertEquals(33.0, second.getCartTotal().getOrThrow())
    }

    @Test
    fun corruptedPersistedCartFallsBackToEmpty() = runTest {
        settings.putString("cocktail_cart_items", "{not valid json")

        val repository = repository()

        assertTrue(repository.getCartItems().getOrThrow().isEmpty())
    }
}
