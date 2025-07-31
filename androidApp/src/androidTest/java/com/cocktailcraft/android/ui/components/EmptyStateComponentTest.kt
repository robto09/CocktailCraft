package com.cocktailcraft.android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * UI tests for EmptyStateComponent using Jetpack Compose testing framework.
 * Tests cover empty state display, action buttons, and user interactions.
 */
@RunWith(AndroidJUnit4::class)
class EmptyStateComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateComponent_displays_title_and_message() {
        // Given
        val title = "No items found"
        val message = "Try adjusting your search criteria"

        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = title,
                message = message,
                actionButtonText = "Retry",
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(title).assertExists()
        composeTestRule.onNodeWithText(message).assertExists()
    }

    @Test
    fun emptyStateComponent_displays_action_button() {
        // Given
        val actionButtonText = "Browse Cocktails"

        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Empty State",
                message = "No content available",
                actionButtonText = actionButtonText,
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(actionButtonText).assertExists()
    }

    @Test
    fun emptyStateComponent_action_button_callback_triggered() {
        // Given
        var actionButtonClicked = false

        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Empty State",
                message = "No content available",
                actionButtonText = "Take Action",
                onActionButtonClick = { actionButtonClicked = true }
            )
        }

        // Perform action button click
        composeTestRule.onNodeWithText("Take Action").performClick()

        // Then
        assertTrue(actionButtonClicked, "Action button callback should be triggered")
    }

    @Test
    fun emptyStateComponent_displays_icon_when_provided() {
        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "No Favorites",
                message = "Add items to your favorites",
                actionButtonText = "Browse",
                onActionButtonClick = { },
                icon = Icons.Filled.Favorite
            )
        }

        // Then
        composeTestRule.onNodeWithTag("empty_state_icon").assertExists()
    }

    @Test
    fun emptyStateComponent_no_icon_when_not_provided() {
        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Empty State",
                message = "No content available",
                actionButtonText = "Action",
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("empty_state_icon").assertDoesNotExist()
    }

    @Test
    fun emptyStateComponent_handles_long_title() {
        // Given
        val longTitle = "This is a very long title that should be handled properly by the empty state component"

        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = longTitle,
                message = "Short message",
                actionButtonText = "Action",
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(longTitle).assertExists()
    }

    @Test
    fun emptyStateComponent_handles_long_message() {
        // Given
        val longMessage = "This is a very long message that explains the empty state in detail and should be displayed properly without breaking the layout or causing any issues with the component rendering"

        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Short Title",
                message = longMessage,
                actionButtonText = "Action",
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(longMessage).assertExists()
    }

    @Test
    fun emptyStateComponent_handles_long_action_button_text() {
        // Given
        val longActionText = "This is a very long action button text"

        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Title",
                message = "Message",
                actionButtonText = longActionText,
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(longActionText).assertExists()
    }

    @Test
    fun emptyStateComponent_shopping_cart_icon_scenario() {
        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Your cart is empty",
                message = "Add some cocktails to your cart and they will appear here",
                actionButtonText = "Start Shopping",
                onActionButtonClick = { },
                icon = Icons.Filled.ShoppingCart
            )
        }

        // Then
        composeTestRule.onNodeWithText("Your cart is empty").assertExists()
        composeTestRule.onNodeWithText("Add some cocktails to your cart and they will appear here").assertExists()
        composeTestRule.onNodeWithText("Start Shopping").assertExists()
        composeTestRule.onNodeWithTag("empty_state_icon").assertExists()
    }

    @Test
    fun emptyStateComponent_favorites_icon_scenario() {
        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "No favorites yet",
                message = "Add cocktails to your favorites to see them here",
                actionButtonText = "Browse Cocktails",
                onActionButtonClick = { },
                icon = Icons.Filled.Favorite
            )
        }

        // Then
        composeTestRule.onNodeWithText("No favorites yet").assertExists()
        composeTestRule.onNodeWithText("Add cocktails to your favorites to see them here").assertExists()
        composeTestRule.onNodeWithText("Browse Cocktails").assertExists()
        composeTestRule.onNodeWithTag("empty_state_icon").assertExists()
    }

    @Test
    fun emptyStateComponent_error_scenario() {
        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Error",
                message = "An unknown error occurred",
                actionButtonText = "Try Again",
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Error").assertExists()
        composeTestRule.onNodeWithText("An unknown error occurred").assertExists()
        composeTestRule.onNodeWithText("Try Again").assertExists()
    }

    @Test
    fun emptyStateComponent_multiple_clicks_handled() {
        // Given
        var clickCount = 0

        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Empty State",
                message = "No content available",
                actionButtonText = "Click Me",
                onActionButtonClick = { clickCount++ }
            )
        }

        // Perform multiple clicks
        composeTestRule.onNodeWithText("Click Me").performClick()
        composeTestRule.onNodeWithText("Click Me").performClick()
        composeTestRule.onNodeWithText("Click Me").performClick()

        // Then
        assertTrue(clickCount == 3, "Multiple clicks should be handled correctly")
    }

    @Test
    fun emptyStateComponent_all_elements_present() {
        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Complete Empty State",
                message = "This shows all possible elements",
                actionButtonText = "Complete Action",
                onActionButtonClick = { },
                icon = Icons.Filled.Favorite
            )
        }

        // Then - Verify all elements are present
        composeTestRule.onNodeWithText("Complete Empty State").assertExists()
        composeTestRule.onNodeWithText("This shows all possible elements").assertExists()
        composeTestRule.onNodeWithText("Complete Action").assertExists()
        composeTestRule.onNodeWithTag("empty_state_icon").assertExists()
    }

    @Test
    fun emptyStateComponent_maintains_layout_consistency() {
        // When - Minimal content
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "A",
                message = "B",
                actionButtonText = "C",
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("A").assertExists()
        composeTestRule.onNodeWithText("B").assertExists()
        composeTestRule.onNodeWithText("C").assertExists()

        // When - Maximum content
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Very Long Title That Tests Layout",
                message = "Very long message that tests how the component handles extensive text content and maintains proper spacing",
                actionButtonText = "Very Long Action Button Text",
                onActionButtonClick = { },
                icon = Icons.Filled.ShoppingCart
            )
        }

        // Then
        composeTestRule.onNodeWithText("Very Long Title That Tests Layout").assertExists()
        composeTestRule.onNodeWithText("Very long message that tests how the component handles extensive text content and maintains proper spacing").assertExists()
        composeTestRule.onNodeWithText("Very Long Action Button Text").assertExists()
        composeTestRule.onNodeWithTag("empty_state_icon").assertExists()
    }

    @Test
    fun emptyStateComponent_action_button_is_clickable() {
        // When
        composeTestRule.setContent {
            EmptyStateComponent(
                title = "Test",
                message = "Test message",
                actionButtonText = "Test Action",
                onActionButtonClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Action").assertHasClickAction()
    }
}
