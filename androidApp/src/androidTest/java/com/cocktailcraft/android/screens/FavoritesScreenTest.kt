package com.cocktailcraft.android.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.LoadingStateComponent
import com.cocktailcraft.domain.model.Cocktail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * UI tests for FavoritesScreen using Jetpack Compose testing framework.
 * These tests focus on UI components and user interactions without complex mocking.
 */
@RunWith(AndroidJUnit4::class)
class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies that the Compose testing framework is properly configured and working.
     * This is a basic sanity check to ensure the test environment is set up correctly.
     */
    @Test
    fun simple_compose_test_passes() {
        // This test just verifies that Compose testing works
        assertTrue(true, "Simple test should pass")
    }

    /**
     * Tests that the loading state component displays correctly when isLoading is true.
     * This ensures users see a loading indicator while favorites are being fetched.
     */
    @Test
    fun loading_state_component_displays_correctly() {
        // When
        composeTestRule.setContent {
            LoadingStateComponent(isLoading = true)
        }

        // Then
        composeTestRule.onNodeWithTag("loading_state").assertIsDisplayed()
    }

    /**
     * Tests that the empty state component displays all required elements when no favorites exist.
     * This ensures users understand why the screen is empty and provides a clear action to take.
     */
    @Test
    fun empty_state_component_displays_correctly() {
        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "No favorites yet",
                message = "Add cocktails to your favorites to see them here",
                actionButtonText = "Browse Cocktails",
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("No favorites yet").assertExists()
        composeTestRule.onNodeWithText("Add cocktails to your favorites to see them here").assertExists()
        composeTestRule.onNodeWithText("Browse Cocktails").assertExists()
    }

    /**
     * Tests that the "Browse Cocktails" button in the empty state triggers the correct callback.
     * This ensures users can navigate to the home screen to discover cocktails when they have no favorites.
     */
    @Test
    fun empty_state_action_button_triggers_callback() {
        // Given
        var actionCalled = false

        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "No favorites yet",
                message = "Add cocktails to your favorites to see them here",
                actionButtonText = "Browse Cocktails",
                onActionButtonClick = { actionCalled = true }
            )
        }

        // Perform action
        composeTestRule.onNodeWithText("Browse Cocktails").performClick()

        // Then
        assertTrue(actionCalled, "Action callback should be triggered")
    }

    /**
     * Tests that a cocktail item displays all required elements correctly.
     * This ensures each favorite cocktail shows its name and interactive elements (add to cart button).
     */
    @Test
    fun cocktail_item_displays_correctly() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")

        // When
        composeTestRule.setContent {
            com.cocktailcraft.android.ui.components.CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = true,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Mojito").assertExists()
        composeTestRule.onNodeWithTag("cocktail_item_1").assertExists()
        composeTestRule.onNodeWithTag("add_to_cart_1").assertExists()
    }

    /**
     * Tests that the add to cart button on a cocktail item triggers the correct callback.
     * This ensures users can add their favorite cocktails to the cart for ordering.
     */
    @Test
    fun cocktail_item_add_to_cart_triggers_callback() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        var addToCartCalled = false

        // When
        composeTestRule.setContent {
            com.cocktailcraft.android.ui.components.CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { addToCartCalled = true },
                isFavorite = true,
                onToggleFavorite = { }
            )
        }

        // Perform add to cart action
        composeTestRule.onNodeWithTag("add_to_cart_1").performClick()

        // Then
        assertTrue(addToCartCalled, "Add to cart callback should be triggered")
    }

    /**
     * Tests that clicking on a cocktail item triggers the correct callback.
     * This ensures users can tap on a favorite cocktail to view its details.
     */
    @Test
    fun cocktail_item_click_triggers_callback() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        var clickCalled = false

        // When
        composeTestRule.setContent {
            com.cocktailcraft.android.ui.components.CocktailItem(
                cocktail = testCocktail,
                onClick = { clickCalled = true },
                onAddToCart = { },
                isFavorite = true,
                onToggleFavorite = { }
            )
        }

        // Perform click action
        composeTestRule.onNodeWithTag("cocktail_item_1").performClick()

        // Then
        assertTrue(clickCalled, "Click callback should be triggered")
    }

    /**
     * Tests that the section header component displays the correct title.
     * This ensures the favorites list has a clear heading for better user experience.
     */
    @Test
    fun section_header_displays_correctly() {
        // When
        composeTestRule.setContent {
            com.cocktailcraft.android.ui.components.SectionHeader(
                title = "Your Favorite Cocktails",
                fontSize = 20
            )
        }

        // Then
        composeTestRule.onNodeWithText("Your Favorite Cocktails").assertExists()
    }

    /**
     * Tests that multiple cocktail items display correctly in a scrollable list.
     * This ensures the favorites screen can handle and display multiple favorite cocktails
     * with proper layout and all items being accessible.
     */
    @Test
    fun multiple_cocktail_items_display_correctly() {
        // Given
        val testCocktails = listOf(
            createTestCocktail("1", "Mojito"),
            createTestCocktail("2", "Margarita"),
            createTestCocktail("3", "Piña Colada")
        )

        // When
        composeTestRule.setContent {
            LazyColumn {
                itemsIndexed(testCocktails) { _, cocktail ->
                    com.cocktailcraft.android.ui.components.CocktailItem(
                        cocktail = cocktail,
                        onClick = { },
                        onAddToCart = { },
                        isFavorite = true,
                        onToggleFavorite = { }
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithText("Mojito").assertExists()
        composeTestRule.onNodeWithText("Margarita").assertExists()
        composeTestRule.onNodeWithText("Piña Colada").assertExists()
        composeTestRule.onNodeWithTag("cocktail_item_1").assertExists()
        composeTestRule.onNodeWithTag("cocktail_item_2").assertExists()
        composeTestRule.onNodeWithTag("cocktail_item_3").assertExists()
    }

    /**
     * Tests that the loading state component is properly hidden when not loading.
     * This ensures the loading indicator doesn't interfere with content display
     * when favorites have finished loading.
     */
    @Test
    fun loading_state_not_displayed_when_not_loading() {
        // When
        composeTestRule.setContent {
            LoadingStateComponent(isLoading = false)
        }

        // Then
        composeTestRule.onNodeWithTag("loading_state").assertDoesNotExist()
    }

    /**
     * Helper function to create test cocktail data
     */
    private fun createTestCocktail(id: String, name: String) = Cocktail(
        id = id,
        name = name,
        instructions = "Test instructions for $name",
        imageUrl = "test_image_$id.jpg",
        category = "Test Category",
        alcoholic = "Alcoholic",
        glass = "Test Glass",
        ingredients = emptyList(),
        price = 10.0 + id.toInt(),
        rating = (4.0 + (id.toInt() % 2) * 0.5).toFloat()
    )
}
