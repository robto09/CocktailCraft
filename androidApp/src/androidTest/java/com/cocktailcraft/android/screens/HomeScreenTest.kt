package com.cocktailcraft.android.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocktailcraft.android.viewmodel.FavoritesViewModelSKIE
import com.cocktailcraft.android.viewmodel.HomeViewModelSKIE
import com.cocktailcraft.domain.model.Cocktail
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * UI tests for HomeScreen using Jetpack Compose testing framework.
 * Tests cover main user interactions, loading states, and error handling.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockHomeViewModel: HomeViewModelSKIE
    private lateinit var mockFavoritesViewModel: FavoritesViewModelSKIE
    
    // StateFlow mocks for HomeViewModel
    private val cocktailsFlow = MutableStateFlow<List<Cocktail>>(emptyList())
    private val isLoadingFlow = MutableStateFlow(false)
    private val isLoadingMoreFlow = MutableStateFlow(false)
    private val hasMoreDataFlow = MutableStateFlow(true)
    private val errorStringFlow = MutableStateFlow("")
    private val isSearchActiveFlow = MutableStateFlow(false)
    private val searchQueryFlow = MutableStateFlow("")
    private val isOfflineModeFlow = MutableStateFlow(false)
    
    // StateFlow mocks for FavoritesViewModel
    private val favoritesFlow = MutableStateFlow<List<Cocktail>>(emptyList())

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock HomeViewModel
        mockHomeViewModel = mockk(relaxed = true) {
            every { cocktails } returns cocktailsFlow
            every { isLoading } returns isLoadingFlow
            every { isLoadingMore } returns isLoadingMoreFlow
            every { hasMoreData } returns hasMoreDataFlow
            every { errorString } returns errorStringFlow
            every { isSearchActive } returns isSearchActiveFlow
            every { searchQuery } returns searchQueryFlow
            every { isOfflineMode } returns isOfflineModeFlow
        }
        
        // Mock FavoritesViewModel
        mockFavoritesViewModel = mockk(relaxed = true) {
            every { favorites } returns favoritesFlow
        }
    }

    @Test
    fun homeScreen_displays_loading_state_when_loading() {
        // Given
        isLoadingFlow.value = true
        cocktailsFlow.value = emptyList()

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { },
                onCocktailClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("loading_shimmer").assertExists()
    }

    @Test
    fun homeScreen_displays_cocktail_list_when_data_loaded() {
        // Given
        val testCocktails = listOf(
            createTestCocktail("1", "Mojito"),
            createTestCocktail("2", "Margarita")
        )
        cocktailsFlow.value = testCocktails
        isLoadingFlow.value = false

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { },
                onCocktailClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Mojito").assertExists()
        composeTestRule.onNodeWithText("Margarita").assertExists()
    }

    @Test
    fun homeScreen_displays_search_bar() {
        // Given
        cocktailsFlow.value = emptyList()
        isLoadingFlow.value = false

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { },
                onCocktailClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("search_bar").assertExists()
    }

    @Test
    fun homeScreen_search_functionality_works() {
        // Given
        cocktailsFlow.value = emptyList()
        isLoadingFlow.value = false

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { },
                onCocktailClick = { }
            )
        }

        // Perform search
        composeTestRule.onNodeWithTag("search_bar").performTextInput("mojito")

        // Then
        verify { mockHomeViewModel.searchCocktails("mojito") }
    }

    @Test
    fun homeScreen_displays_empty_state_when_no_cocktails() {
        // Given
        cocktailsFlow.value = emptyList()
        isLoadingFlow.value = false
        errorStringFlow.value = ""

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { },
                onCocktailClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("empty_search_results").assertExists()
    }

    @Test
    fun homeScreen_displays_error_state_when_error_occurs() {
        // Given
        cocktailsFlow.value = emptyList()
        isLoadingFlow.value = false
        errorStringFlow.value = "Network error occurred"

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { },
                onCocktailClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Network error occurred").assertExists()
    }

    @Test
    fun homeScreen_add_to_cart_callback_triggered() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        cocktailsFlow.value = listOf(testCocktail)
        isLoadingFlow.value = false
        var addToCartCalled = false

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { addToCartCalled = true },
                onCocktailClick = { }
            )
        }

        // Perform add to cart action
        composeTestRule.onNodeWithTag("add_to_cart_1").performClick()

        // Then
        assertTrue(addToCartCalled, "Add to cart callback should be triggered")
    }

    @Test
    fun homeScreen_cocktail_click_callback_triggered() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        cocktailsFlow.value = listOf(testCocktail)
        isLoadingFlow.value = false
        var cocktailClickCalled = false

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { },
                onCocktailClick = { cocktailClickCalled = true }
            )
        }

        // Perform cocktail click
        composeTestRule.onNodeWithTag("cocktail_item_1").performClick()

        // Then
        assertTrue(cocktailClickCalled, "Cocktail click callback should be triggered")
    }

    @Test
    fun homeScreen_pull_to_refresh_works() {
        // Given
        cocktailsFlow.value = listOf(createTestCocktail("1", "Mojito"))
        isLoadingFlow.value = false

        // When
        composeTestRule.setContent {
            HomeScreen(
                viewModel = mockHomeViewModel,
                favoritesViewModel = mockFavoritesViewModel,
                onAddToCart = { },
                onCocktailClick = { }
            )
        }

        // Perform pull to refresh
        composeTestRule.onNodeWithTag("cocktail_list").performTouchInput {
            swipeDown()
        }

        // Then
        verify { mockHomeViewModel.retry() }
    }

    /**
     * Helper function to create test cocktail data
     */
    private fun createTestCocktail(id: String, name: String) = Cocktail(
        id = id,
        name = name,
        instructions = "Test instructions",
        imageUrl = "test_image.jpg",
        category = "Test Category",
        alcoholic = "Alcoholic",
        glass = "Test Glass",
        ingredients = emptyList(),
        price = 10.0,
        rating = 4.5f
    )
}
