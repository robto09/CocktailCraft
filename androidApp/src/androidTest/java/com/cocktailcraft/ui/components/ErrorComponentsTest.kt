package com.cocktailcraft.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.cocktailcraft.MainActivity
import com.cocktailcraft.util.ErrorUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@LargeTest
class ErrorComponentsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun errorBanner_shouldDisplayErrorInfo() {
        // Given
        val error = ErrorUtils.createUserFriendlyError(
            title = "Test Error",
            message = "This is a test error message",
            category = ErrorUtils.ErrorCategory.NETWORK
        )

        // When
        composeTestRule.setContent {
            ErrorBanner(
                error = error,
                onDismiss = {},
                onAction = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("This is a test error message").assertIsDisplayed()
    }

    @Test
    fun errorBanner_shouldCallOnDismissWhenDismissed() {
        // Given
        val error = ErrorUtils.createUserFriendlyError(
            title = "Test Error",
            message = "This is a test error message"
        )
        val onDismiss = mock(Runnable::class.java)

        // When
        composeTestRule.setContent {
            ErrorBanner(
                error = error,
                onDismiss = { onDismiss.run() },
                onAction = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Dismiss").performClick()
        verify(onDismiss).run()
    }

    @Test
    fun errorBanner_shouldCallOnActionWhenActionClicked() {
        // Given
        val recoveryAction = ErrorUtils.RecoveryAction("Retry") { /* no-op */ }
        val error = ErrorUtils.createUserFriendlyError(
            title = "Test Error",
            message = "This is a test error message",
            recoveryAction = recoveryAction
        )
        val onAction = mock(Runnable::class.java)

        // When
        composeTestRule.setContent {
            ErrorBanner(
                error = error,
                onDismiss = {},
                onAction = { onAction.run() }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Retry").performClick()
        verify(onAction).run()
    }

    @Test
    fun errorDialog_shouldDisplayErrorInfo() {
        // Given
        val error = ErrorUtils.createUserFriendlyError(
            title = "Test Error",
            message = "This is a test error message",
            category = ErrorUtils.ErrorCategory.NETWORK
        )

        // When
        composeTestRule.setContent {
            ErrorDialog(
                error = error,
                onDismiss = {},
                onRetry = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("This is a test error message").assertIsDisplayed()
    }

    @Test
    fun errorDialog_shouldCallOnDismissWhenDismissed() {
        // Given
        val error = ErrorUtils.createUserFriendlyError(
            title = "Test Error",
            message = "This is a test error message"
        )
        val onDismiss = mock(Runnable::class.java)

        // When
        composeTestRule.setContent {
            ErrorDialog(
                error = error,
                onDismiss = { onDismiss.run() },
                onRetry = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Dismiss").performClick()
        verify(onDismiss).run()
    }

    @Test
    fun errorDialog_shouldCallOnRetryWhenRetryClicked() {
        // Given
        val error = ErrorUtils.createUserFriendlyError(
            title = "Test Error",
            message = "This is a test error message"
        )
        val onRetry = mock(Runnable::class.java)

        // When
        composeTestRule.setContent {
            ErrorDialog(
                error = error,
                onDismiss = {},
                onRetry = { onRetry.run() }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Retry").performClick()
        verify(onRetry).run()
    }
}
