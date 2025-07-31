package com.cocktailcraft.android.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * UI tests for CocktailSearchBar component using Jetpack Compose testing framework.
 * Tests cover search functionality, advanced search toggle, and user interactions.
 */
@RunWith(AndroidJUnit4::class)
class CocktailSearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_displays_correctly() {
        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("search_bar").assertExists()
        composeTestRule.onNodeWithText("Search cocktails...").assertExists()
    }

    @Test
    fun searchBar_displays_search_query() {
        // Given
        val searchQuery = "mojito"

        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = searchQuery,
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("mojito").assertExists()
    }

    @Test
    fun searchBar_search_query_change_callback_triggered() {
        // Given
        var searchQueryChanged = ""

        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { searchQueryChanged = it },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Perform text input
        composeTestRule.onNodeWithTag("search_bar").performTextInput("margarita")

        // Then
        assertTrue(searchQueryChanged.contains("margarita"), "Search query change callback should be triggered")
    }

    @Test
    fun searchBar_clear_search_callback_triggered() {
        // Given
        var clearSearchCalled = false

        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "mojito",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { clearSearchCalled = true },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Perform clear action
        composeTestRule.onNodeWithTag("clear_search").performClick()

        // Then
        assertTrue(clearSearchCalled, "Clear search callback should be triggered")
    }

    @Test
    fun searchBar_shows_clear_button_when_query_not_empty() {
        // When - Empty query
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("clear_search").assertDoesNotExist()

        // When - Non-empty query
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "mojito",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("clear_search").assertExists()
    }

    @Test
    fun searchBar_advanced_search_toggle_callback_triggered() {
        // Given
        var toggleAdvancedSearchCalled = false

        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { toggleAdvancedSearchCalled = true },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Perform advanced search toggle
        composeTestRule.onNodeWithTag("advanced_search_toggle").performClick()

        // Then
        assertTrue(toggleAdvancedSearchCalled, "Advanced search toggle callback should be triggered")
    }

    @Test
    fun searchBar_shows_advanced_search_dialog_callback_triggered() {
        // Given
        var showAdvancedSearchDialogCalled = false

        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { showAdvancedSearchDialogCalled = true }
            )
        }

        // Perform show advanced search dialog
        composeTestRule.onNodeWithTag("show_advanced_search").performClick()

        // Then
        assertTrue(showAdvancedSearchDialogCalled, "Show advanced search dialog callback should be triggered")
    }

    @Test
    fun searchBar_displays_active_filters_indicator() {
        // When - No active filters
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("active_filters_indicator").assertDoesNotExist()

        // When - Has active filters
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = true,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("active_filters_indicator").assertExists()
    }

    @Test
    fun searchBar_displays_advanced_search_active_state() {
        // When - Advanced search not active
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("advanced_search_toggle").assertExists()

        // When - Advanced search active
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = true,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("advanced_search_toggle").assertExists()
    }

    @Test
    fun searchBar_handles_long_search_queries() {
        // Given
        val longSearchQuery = "This is a very long search query that should be handled properly by the search bar component"

        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = longSearchQuery,
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(longSearchQuery).assertExists()
        composeTestRule.onNodeWithTag("clear_search").assertExists()
    }

    @Test
    fun searchBar_handles_special_characters_in_search() {
        // Given
        val specialCharQuery = "piña colada & rum 100%"

        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = specialCharQuery,
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText(specialCharQuery).assertExists()
    }

    @Test
    fun searchBar_all_interactive_elements_present() {
        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "test",
                isAdvancedSearchActive = true,
                hasActiveFilters = true,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Then - Verify all interactive elements are present
        composeTestRule.onNodeWithTag("search_bar").assertExists()
        composeTestRule.onNodeWithTag("clear_search").assertExists()
        composeTestRule.onNodeWithTag("advanced_search_toggle").assertExists()
        composeTestRule.onNodeWithTag("show_advanced_search").assertExists()
        composeTestRule.onNodeWithTag("active_filters_indicator").assertExists()
    }

    @Test
    fun searchBar_supports_text_replacement() {
        // Given
        var currentQuery = "mojito"

        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = currentQuery,
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { currentQuery = it },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Perform text replacement
        composeTestRule.onNodeWithTag("search_bar").performTextClearance()
        composeTestRule.onNodeWithTag("search_bar").performTextInput("margarita")

        // Then
        assertTrue(currentQuery.contains("margarita"), "Text replacement should work correctly")
    }

    @Test
    fun searchBar_maintains_focus_during_interactions() {
        // When
        composeTestRule.setContent {
            CocktailSearchBar(
                searchQuery = "",
                isAdvancedSearchActive = false,
                hasActiveFilters = false,
                onSearchQueryChange = { },
                onClearSearch = { },
                onToggleAdvancedSearch = { },
                onShowAdvancedSearchDialog = { }
            )
        }

        // Perform focus and text input
        composeTestRule.onNodeWithTag("search_bar").performClick()
        composeTestRule.onNodeWithTag("search_bar").performTextInput("test")

        // Then
        composeTestRule.onNodeWithTag("search_bar").assertIsFocused()
    }
}
