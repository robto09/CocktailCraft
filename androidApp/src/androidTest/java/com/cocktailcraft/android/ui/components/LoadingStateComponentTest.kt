package com.cocktailcraft.android.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple UI tests for LoadingStateComponent.
 */
@RunWith(AndroidJUnit4::class)
class LoadingStateComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingStateComponent_displays_when_loading_is_true() {
        // When
        composeTestRule.setContent {
            LoadingStateComponent(isLoading = true)
        }

        // Then
        composeTestRule.onNodeWithTag("loading_state").assertIsDisplayed()
    }

    @Test
    fun loadingStateComponent_not_displayed_when_loading_is_false() {
        // When
        composeTestRule.setContent {
            LoadingStateComponent(isLoading = false)
        }

        // Then
        composeTestRule.onNodeWithTag("loading_state").assertDoesNotExist()
    }

    @Test
    fun simple_test_passes() {
        // This test should always pass
        assert(true)
    }
}
