package com.cocktailcraft.android.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.viewmodel.CartViewModelSKIE
import com.cocktailcraft.android.viewmodel.FavoritesViewModelSKIE
import com.cocktailcraft.android.viewmodel.OrderViewModelSKIE
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * UI tests for CartScreen using Jetpack Compose testing framework.
 * Tests cover cart display, empty states, quantity updates, and checkout flow.
 */
@RunWith(AndroidJUnit4::class)
class CartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockCartViewModel: CartViewModelSKIE
    private lateinit var mockOrderViewModel: OrderViewModelSKIE
    private lateinit var mockFavoritesViewModel: FavoritesViewModelSKIE
    private lateinit var mockNavigationManager: NavigationManager
    
    // StateFlow mocks for CartViewModel
    private val cartItemsFlow = MutableStateFlow<List<CocktailCartItem>>(emptyList())
    private val totalPriceFlow = MutableStateFlow(0.0)
    private val isLoadingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<Throwable?>(null)
    
    // StateFlow mocks for FavoritesViewModel
    private val favoritesFlow = MutableStateFlow<List<Cocktail>>(emptyList())

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock CartViewModel
        mockCartViewModel = mockk(relaxed = true) {
            every { cartItems } returns cartItemsFlow
            every { totalPrice } returns totalPriceFlow
            every { isLoading } returns isLoadingFlow
            every { error } returns errorFlow
        }
        
        // Mock OrderViewModel
        mockOrderViewModel = mockk(relaxed = true)
        
        // Mock FavoritesViewModel
        mockFavoritesViewModel = mockk(relaxed = true) {
            every { favorites } returns favoritesFlow
        }
        
        // Mock NavigationManager
        mockNavigationManager = mockk(relaxed = true)
    }

    @Test
    fun cartScreen_displays_loading_state_when_loading() {
        // Given
        isLoadingFlow.value = true
        cartItemsFlow.value = emptyList()

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithTag("loading_state").assertExists()
    }

    @Test
    fun cartScreen_displays_empty_state_when_no_items() {
        // Given
        cartItemsFlow.value = emptyList()
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Your cart is empty").assertExists()
        composeTestRule.onNodeWithText("Add some cocktails to your cart and they will appear here").assertExists()
        composeTestRule.onNodeWithText("Start Shopping").assertExists()
    }

    @Test
    fun cartScreen_displays_cart_items_when_data_available() {
        // Given
        val testCartItems = listOf(
            createTestCartItem("1", "Mojito", 2),
            createTestCartItem("2", "Margarita", 1)
        )
        cartItemsFlow.value = testCartItems
        totalPriceFlow.value = 35.0
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Mojito").assertExists()
        composeTestRule.onNodeWithText("Margarita").assertExists()
        composeTestRule.onNodeWithText("$35.00").assertExists()
    }

    @Test
    fun cartScreen_displays_error_state_when_error_occurs() {
        // Given
        cartItemsFlow.value = emptyList()
        isLoadingFlow.value = false
        errorFlow.value = RuntimeException("Failed to load cart")

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertExists()
        composeTestRule.onNodeWithText("Try Again").assertExists()
    }

    @Test
    fun cartScreen_start_shopping_callback_triggered() {
        // Given
        cartItemsFlow.value = emptyList()
        isLoadingFlow.value = false
        errorFlow.value = null
        var startShoppingCalled = false

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { startShoppingCalled = true },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Perform start shopping action
        composeTestRule.onNodeWithText("Start Shopping").performClick()

        // Then
        assertTrue(startShoppingCalled, "Start shopping callback should be triggered")
    }

    @Test
    fun cartScreen_increase_quantity_functionality_works() {
        // Given
        val testCartItem = createTestCartItem("1", "Mojito", 2)
        cartItemsFlow.value = listOf(testCartItem)
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Perform increase quantity action
        composeTestRule.onNodeWithTag("increase_quantity_1").performClick()

        // Then
        verify { mockCartViewModel.updateQuantity("1", 3) }
    }

    @Test
    fun cartScreen_decrease_quantity_functionality_works() {
        // Given
        val testCartItem = createTestCartItem("1", "Mojito", 2)
        cartItemsFlow.value = listOf(testCartItem)
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Perform decrease quantity action
        composeTestRule.onNodeWithTag("decrease_quantity_1").performClick()

        // Then
        verify { mockCartViewModel.updateQuantity("1", 1) }
    }

    @Test
    fun cartScreen_remove_item_when_quantity_becomes_zero() {
        // Given
        val testCartItem = createTestCartItem("1", "Mojito", 1)
        cartItemsFlow.value = listOf(testCartItem)
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Perform decrease quantity action on item with quantity 1
        composeTestRule.onNodeWithTag("decrease_quantity_1").performClick()

        // Then
        verify { mockCartViewModel.removeFromCart("1") }
    }

    @Test
    fun cartScreen_remove_item_functionality_works() {
        // Given
        val testCartItem = createTestCartItem("1", "Mojito", 2)
        cartItemsFlow.value = listOf(testCartItem)
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Perform remove item action
        composeTestRule.onNodeWithTag("remove_item_1").performClick()

        // Then
        verify { mockCartViewModel.removeFromCart("1") }
    }

    @Test
    fun cartScreen_toggle_favorite_functionality_works() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        val testCartItem = CocktailCartItem(testCocktail, 1)
        cartItemsFlow.value = listOf(testCartItem)
        favoritesFlow.value = emptyList()
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Perform toggle favorite action
        composeTestRule.onNodeWithTag("favorite_toggle_1").performClick()

        // Then
        verify { mockFavoritesViewModel.toggleFavorite(testCocktail) }
    }

    @Test
    fun cartScreen_displays_order_summary_with_items() {
        // Given
        val testCartItems = listOf(
            createTestCartItem("1", "Mojito", 2),
            createTestCartItem("2", "Margarita", 1)
        )
        cartItemsFlow.value = testCartItems
        totalPriceFlow.value = 35.0
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            CartScreen(
                viewModel = mockCartViewModel,
                orderViewModel = mockOrderViewModel,
                navigationManager = mockNavigationManager,
                onStartShopping = { },
                favoritesViewModel = mockFavoritesViewModel
            )
        }

        // Then
        composeTestRule.onNodeWithTag("order_summary").assertExists()
        composeTestRule.onNodeWithText("Place Order").assertExists()
    }

    /**
     * Helper function to create test cocktail data
     */
    private fun createTestCocktail(id: String, name: String) = Cocktail(
        id = id,
        name = name,
        instructions = "Test instructions for $name",
        image = "test_image_$id.jpg",
        category = "Test Category",
        alcoholic = "Alcoholic",
        glass = "Test Glass",
        ingredients = emptyList(),
        price = 15.0,
        rating = 4.5,
        reviews = emptyList(),
        isPopular = false,
        tags = emptyList()
    )

    /**
     * Helper function to create test cart item data
     */
    private fun createTestCartItem(id: String, name: String, quantity: Int) = CocktailCartItem(
        cocktail = createTestCocktail(id, name),
        quantity = quantity
    )
}
