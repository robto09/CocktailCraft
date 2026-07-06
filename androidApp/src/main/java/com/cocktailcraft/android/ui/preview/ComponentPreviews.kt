package com.cocktailcraft.android.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cocktailcraft.android.ui.components.CocktailSearchBar
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.SignInDialog
import com.cocktailcraft.android.ui.components.SignUpDialog
import com.cocktailcraft.android.ui.theme.CocktailBarTheme

/**
 * Design-time previews for the most-reused components, in light and dark.
 * Add a preview here whenever a new reusable component lands.
 */

@Preview(name = "Search bar", showBackground = true)
@Composable
private fun CocktailSearchBarPreview() {
    CocktailBarTheme(darkTheme = false) {
        CocktailSearchBar(
            searchQuery = "Margarita",
            isAdvancedSearchActive = false,
            hasActiveFilters = false,
            onSearchQueryChange = {},
            onClearSearch = {},
            onToggleAdvancedSearch = {}
        )
    }
}

@Preview(name = "Search bar — filters active (dark)", showBackground = true)
@Composable
private fun CocktailSearchBarActiveDarkPreview() {
    CocktailBarTheme(darkTheme = true) {
        CocktailSearchBar(
            searchQuery = "",
            isAdvancedSearchActive = true,
            hasActiveFilters = true,
            onSearchQueryChange = {},
            onClearSearch = {},
            onToggleAdvancedSearch = {}
        )
    }
}

@Preview(name = "Empty state", showBackground = true)
@Composable
private fun EmptyStatePreview() {
    CocktailBarTheme(darkTheme = false) {
        EmptyStateComponent(
            title = "No favorites yet",
            message = "Add cocktails to your favorites to see them here",
            actionButtonText = "Browse Cocktails"
        )
    }
}

@Preview(name = "Sign in dialog", showBackground = true)
@Composable
private fun SignInDialogPreview() {
    CocktailBarTheme(darkTheme = false) {
        SignInDialog(onDismiss = {}, onSignIn = { _, _ -> })
    }
}

@Preview(name = "Sign up dialog (dark)", showBackground = true)
@Composable
private fun SignUpDialogPreview() {
    CocktailBarTheme(darkTheme = true) {
        SignUpDialog(onDismiss = {}, onSignUp = { _, _, _ -> })
    }
}
