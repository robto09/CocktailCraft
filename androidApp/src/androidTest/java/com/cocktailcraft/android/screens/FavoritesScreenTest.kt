package com.cocktailcraft.android.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocktailcraft.android.viewmodel.CartViewModelSKIE
import com.cocktailcraft.android.viewmodel.FavoritesViewModelSKIE
import com.cocktailcraft.domain.model.Cocktail
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * UI tests for FavoritesScreen using Jetpack Compose testing framework.
 * Tests cover favorites display, empty states, and user interactions.
 */
@RunWith(AndroidJUnit4::class)
class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockCartViewModel: CartViewModelSKIE
    private lateinit var mockFavoritesViewModel: FavoritesViewModelSKIE
    
    // StateFlow mocks for FavoritesViewModel
    private val favoritesFlow = MutableStateFlow<List<Cocktail>>(emptyList())
    private val isLoadingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<Throwable?>(null)

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock CartViewModel
        mockCartViewModel = mockk(relaxed = true)
        
        // Mock FavoritesViewModel
        mockFavoritesViewModel = mockk(relaxed = true) {
            every { favorites } returns favoritesFlow
            every { isLoading } returns isLoadingFlow
            every { error } returns errorFlow
        }
    }

    @Test
    fun favoritesScreen_displays_loading_state_when_loading() {
        // Given
        isLoadingFlow.value = true
        favoritesFlow.value = emptyList()

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { },
                onAddToCart = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("loading_state").assertExists()
    }

    @Test
    fun favoritesScreen_displays_empty_state_when_no_favorites() {
        // Given
        favoritesFlow.value = emptyList()
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { },
                onAddToCart = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("No favorites yet").assertExists()
        composeTestRule.onNodeWithText("Add cocktails to your favorites to see them here").assertExists()
        composeTestRule.onNodeWithText("Browse Cocktails").assertExists()
    }

    @Test
    fun favoritesScreen_displays_favorites_list_when_data_available() {
        // Given
        val testFavorites = listOf(
            createTestCocktail("1", "Mojito"),
            createTestCocktail("2", "Margarita"),
            createTestCocktail("3", "Piña Colada")
        )
        favoritesFlow.value = testFavorites
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { },
                onAddToCart = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Your Favorite Cocktails").assertExists()
        composeTestRule.onNodeWithText("Mojito").assertExists()
        composeTestRule.onNodeWithText("Margarita").assertExists()
        composeTestRule.onNodeWithText("Piña Colada").assertExists()
    }

    @Test
    fun favoritesScreen_displays_error_state_when_error_occurs() {
        // Given
        favoritesFlow.value = emptyList()
        isLoadingFlow.value = false
        errorFlow.value = RuntimeException("Failed to load favorites")

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { },
                onAddToCart = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertExists()
        composeTestRule.onNodeWithText("Try Again").assertExists()
    }

    @Test
    fun favoritesScreen_browse_products_callback_triggered() {
        // Given
        favoritesFlow.value = emptyList()
        isLoadingFlow.value = false
        errorFlow.value = null
        var browseProductsCalled = false

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { browseProductsCalled = true },
                onAddToCart = { }
            )
        }

        // Perform browse products action
        composeTestRule.onNodeWithText("Browse Cocktails").performClick()

        // Then
        assertTrue(browseProductsCalled, "Browse products callback should be triggered")
    }

    @Test
    fun favoritesScreen_add_to_cart_functionality_works() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        favoritesFlow.value = listOf(testCocktail)
        isLoadingFlow.value = false
        errorFlow.value = null
        var addToCartCalled = false

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { },
                onAddToCart = { addToCartCalled = true }
            )
        }

        // Perform add to cart action
        composeTestRule.onNodeWithTag("add_to_cart_1").performClick()

        // Then
        verify { mockCartViewModel.addToCart(testCocktail) }
        assertTrue(addToCartCalled, "Add to cart callback should be triggered")
    }

    @Test
    fun favoritesScreen_remove_from_favorites_functionality_works() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        favoritesFlow.value = listOf(testCocktail)
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { },
                onAddToCart = { }
            )
        }

        // Perform remove from favorites action
        composeTestRule.onNodeWithTag("favorite_toggle_1").performClick()

        // Then
        verify { mockFavoritesViewModel.toggleFavorite(testCocktail) }
    }

    @Test
    fun favoritesScreen_displays_multiple_favorites_correctly() {
        // Given
        val testFavorites = listOf(
            createTestCocktail("1", "Mojito"),
            createTestCocktail("2", "Margarita"),
            createTestCocktail("3", "Piña Colada"),
            createTestCocktail("4", "Daiquiri"),
            createTestCocktail("5", "Cosmopolitan")
        )
        favoritesFlow.value = testFavorites
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { },
                onAddToCart = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Your Favorite Cocktails").assertExists()
        
        // Verify all favorites are displayed
        testFavorites.forEach { cocktail ->
            composeTestRule.onNodeWithText(cocktail.name).assertExists()
        }
    }

    @Test
    fun favoritesScreen_scrollable_list_works() {
        // Given
        val testFavorites = (1..10).map { 
            createTestCocktail(it.toString(), "Cocktail $it") 
        }
        favoritesFlow.value = testFavorites
        isLoadingFlow.value = false
        errorFlow.value = null

        // When
        composeTestRule.setContent {
            FavoritesScreen(
                cartViewModel = mockCartViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onBrowseProducts = { },
                onAddToCart = { }
            )
        }

        // Then
        // Verify first few items are visible
        composeTestRule.onNodeWithText("Cocktail 1").assertExists()
        composeTestRule.onNodeWithText("Cocktail 2").assertExists()
        
        // Scroll down to see more items
        composeTestRule.onNodeWithTag("favorites_list").performScrollToNode(
            hasText("Cocktail 10")
        )
        
        // Verify last item is now visible
        composeTestRule.onNodeWithText("Cocktail 10").assertExists()
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
        price = 10.0 + id.toInt(),
        rating = 4.0 + (id.toInt() % 2) * 0.5,
        reviews = emptyList(),
        isPopular = id.toInt() % 2 == 0,
        tags = emptyList()
    )
}
