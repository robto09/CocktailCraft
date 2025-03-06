package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.repository.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {
    
    private lateinit var viewModel: CartViewModel
    private val cartRepository: CartRepository = mock()
    private val testDispatcher = StandardTestDispatcher()
    
    // Sample test data
    private val testCocktail = Cocktail(
        id = "1",
        name = "Mojito",
        instructions = "Mix ingredients and serve with ice",
        imageUrl = "mojito.jpg",
        price = 9.99,
        ingredients = listOf(
            CocktailIngredient("Rum", "2 oz"),
            CocktailIngredient("Mint", "6 leaves")
        ),
        rating = 4.5f,
        category = "Classic"
    )
    
    private val testCartItem = CocktailCartItem(testCocktail, 2)
    private val testCartItems = listOf(testCartItem)
    private val testCartTotal = 19.98 // 9.99 * 2
    
    @Before
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        
        // Mock repository responses
        whenever(cartRepository.getCartItems()).thenReturn(flowOf(testCartItems))
        whenever(cartRepository.getCartTotal()).thenReturn(flowOf(testCartTotal))
        
        // Create view model with mocked repository
        viewModel = CartViewModel(cartRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should load cart items and total`() = runTest {
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify cart items are loaded
        viewModel.cartItems.test(timeout = 5.seconds) {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Mojito", items[0].cocktail.name)
            assertEquals(2, items[0].quantity)
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify total price is calculated
        viewModel.totalPrice.test(timeout = 5.seconds) {
            assertEquals(19.98, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify loading state is false after initialization
        viewModel.isLoading.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify no errors
        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `addToCart should add item to cart`() = runTest {
        // Setup
        val newCocktail = testCocktail.copy(id = "2", name = "Margarita")
        
        // Execute
        viewModel.addToCart(newCocktail, 1)
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify repository method was called with correct parameters
        Mockito.verify(cartRepository).addToCart(CocktailCartItem(newCocktail, 1))
        
        // Verify loadCartItems was called to refresh the cart
        // The method is called once during init and once after addToCart
        Mockito.verify(cartRepository, times(2)).getCartItems()
        Mockito.verify(cartRepository, times(2)).getCartTotal()
    }
    
    @Test
    fun `removeFromCart should remove item from cart`() = runTest {
        // Execute
        viewModel.removeFromCart("1")
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify repository method was called with correct parameters
        Mockito.verify(cartRepository).removeFromCart("1")
        
        // Verify loadCartItems was called to refresh the cart
        // The method is called once during init and once after removeFromCart
        Mockito.verify(cartRepository, times(2)).getCartItems()
        Mockito.verify(cartRepository, times(2)).getCartTotal()
    }
    
    @Test
    fun `updateQuantity should update item quantity in cart`() = runTest {
        // Execute
        viewModel.updateQuantity("1", 3)
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify repository method was called with correct parameters
        Mockito.verify(cartRepository).updateQuantity("1", 3)
        
        // Verify loadCartItems was called to refresh the cart
        // The method is called once during init and once after updateQuantity
        Mockito.verify(cartRepository, times(2)).getCartItems()
        Mockito.verify(cartRepository, times(2)).getCartTotal()
    }
    
    @Test
    fun `clearCart should clear all items from cart`() = runTest {
        // Execute
        viewModel.clearCart()
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify repository method was called
        Mockito.verify(cartRepository).clearCart()
        
        // Verify loadCartItems was called to refresh the cart
        // The method is called once during init and once after clearCart
        Mockito.verify(cartRepository, times(2)).getCartItems()
        Mockito.verify(cartRepository, times(2)).getCartTotal()
    }
    
    @Test
    fun `error handling should set error state`() = runTest {
        // Setup - mock repository to throw exception
        val errorMessage = "Network error"
        whenever(cartRepository.getCartItems()).thenThrow(RuntimeException(errorMessage))
        
        // Execute
        viewModel.loadCartItems()
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify error state is set
        viewModel.error.test {
            val error = awaitItem()
            assertEquals("Failed to load cart: $errorMessage", error)
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify loading state is false after error
        viewModel.isLoading.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
} 