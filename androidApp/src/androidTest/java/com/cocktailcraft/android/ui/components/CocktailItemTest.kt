package com.cocktailcraft.android.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocktailcraft.domain.model.Cocktail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * UI tests for CocktailItem component using Jetpack Compose testing framework.
 * Tests cover component display, interactions, and state changes.
 */
@RunWith(AndroidJUnit4::class)
class CocktailItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cocktailItem_displays_cocktail_information() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Mojito").assertExists()
        composeTestRule.onNodeWithText("Test Category").assertExists()
        composeTestRule.onNodeWithText("$15.00").assertExists()
    }

    @Test
    fun cocktailItem_displays_favorite_state_correctly() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")

        // When - Not favorite
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("favorite_toggle_1").assertExists()
        
        // When - Is favorite
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = true,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("favorite_toggle_1").assertExists()
    }

    @Test
    fun cocktailItem_click_callback_triggered() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        var clickCalled = false

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { clickCalled = true },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Perform click
        composeTestRule.onNodeWithTag("cocktail_item_1").performClick()

        // Then
        assertTrue(clickCalled, "Click callback should be triggered")
    }

    @Test
    fun cocktailItem_add_to_cart_callback_triggered() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        var addToCartCalled = false

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { addToCartCalled = true },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Perform add to cart
        composeTestRule.onNodeWithTag("add_to_cart_1").performClick()

        // Then
        assertTrue(addToCartCalled, "Add to cart callback should be triggered")
    }

    @Test
    fun cocktailItem_toggle_favorite_callback_triggered() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito")
        var toggleFavoriteCalled = false

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { toggleFavoriteCalled = true }
            )
        }

        // Perform toggle favorite
        composeTestRule.onNodeWithTag("favorite_toggle_1").performClick()

        // Then
        assertTrue(toggleFavoriteCalled, "Toggle favorite callback should be triggered")
    }

    @Test
    fun cocktailItem_displays_rating_when_available() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito").copy(rating = 4.5)

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("4.5").assertExists()
    }

    @Test
    fun cocktailItem_displays_popular_indicator_when_popular() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito").copy(isPopular = true)

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("popular_indicator").assertExists()
    }

    @Test
    fun cocktailItem_displays_alcoholic_indicator() {
        // Given
        val alcoholicCocktail = createTestCocktail("1", "Mojito").copy(alcoholic = "Alcoholic")
        val nonAlcoholicCocktail = createTestCocktail("2", "Virgin Mojito").copy(alcoholic = "Non alcoholic")

        // When - Alcoholic
        composeTestRule.setContent {
            CocktailItem(
                cocktail = alcoholicCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Alcoholic").assertExists()

        // When - Non-alcoholic
        composeTestRule.setContent {
            CocktailItem(
                cocktail = nonAlcoholicCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Non alcoholic").assertExists()
    }

    @Test
    fun cocktailItem_displays_glass_type() {
        // Given
        val testCocktail = createTestCocktail("1", "Mojito").copy(glass = "Highball glass")

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Highball glass").assertExists()
    }

    @Test
    fun cocktailItem_handles_long_cocktail_names() {
        // Given
        val testCocktail = createTestCocktail("1", "Very Long Cocktail Name That Should Be Handled Properly")

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Very Long Cocktail Name That Should Be Handled Properly").assertExists()
    }

    @Test
    fun cocktailItem_handles_zero_price() {
        // Given
        val testCocktail = createTestCocktail("1", "Free Cocktail").copy(price = 0.0)

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("$0.00").assertExists()
    }

    @Test
    fun cocktailItem_handles_high_price() {
        // Given
        val testCocktail = createTestCocktail("1", "Expensive Cocktail").copy(price = 99.99)

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = false,
                onToggleFavorite = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("$99.99").assertExists()
    }

    @Test
    fun cocktailItem_displays_all_required_elements() {
        // Given
        val testCocktail = createTestCocktail("1", "Complete Cocktail").copy(
            rating = 4.8,
            isPopular = true,
            alcoholic = "Alcoholic",
            glass = "Martini glass"
        )

        // When
        composeTestRule.setContent {
            CocktailItem(
                cocktail = testCocktail,
                onClick = { },
                onAddToCart = { },
                isFavorite = true,
                onToggleFavorite = { }
            )
        }

        // Then - Verify all elements are present
        composeTestRule.onNodeWithText("Complete Cocktail").assertExists()
        composeTestRule.onNodeWithText("Test Category").assertExists()
        composeTestRule.onNodeWithText("$15.00").assertExists()
        composeTestRule.onNodeWithText("4.8").assertExists()
        composeTestRule.onNodeWithText("Alcoholic").assertExists()
        composeTestRule.onNodeWithText("Martini glass").assertExists()
        composeTestRule.onNodeWithTag("popular_indicator").assertExists()
        composeTestRule.onNodeWithTag("favorite_toggle_1").assertExists()
        composeTestRule.onNodeWithTag("add_to_cart_1").assertExists()
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
}
