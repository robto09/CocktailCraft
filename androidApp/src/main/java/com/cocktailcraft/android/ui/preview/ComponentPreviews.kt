package com.cocktailcraft.android.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.CartItemCard
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.CocktailLoadingShimmer
import com.cocktailcraft.android.ui.components.CocktailSearchBar
import com.cocktailcraft.android.ui.components.EmptyStateComponent
import com.cocktailcraft.android.ui.components.NetworkStatusCard
import com.cocktailcraft.android.ui.components.SignInDialog
import com.cocktailcraft.android.ui.components.SignUpDialog
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme

/**
 * Design-time previews for the most-reused components, in light and dark.
 * Add a preview here whenever a new reusable component lands.
 */

@Preview(name = "Search bar", showBackground = true)
@Composable
private fun CocktailSearchBarPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
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
    AnimatedCocktailBarTheme(darkTheme = true) {
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
    AnimatedCocktailBarTheme(darkTheme = false) {
        EmptyStateComponent(
            title = stringResource(R.string.no_favorites),
            message = stringResource(R.string.favorites_empty_message),
            actionButtonText = stringResource(R.string.browse_cocktails)
        )
    }
}

@Preview(name = "Sign in dialog", showBackground = true)
@Composable
private fun SignInDialogPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        SignInDialog(onDismiss = {}, onSignIn = { _, _ -> })
    }
}

@Preview(name = "Sign up dialog (dark)", showBackground = true)
@Composable
private fun SignUpDialogPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        SignUpDialog(onDismiss = {}, onSignUp = { _, _, _ -> })
    }
}

// ---- List component previews ----

@Preview(name = "Cocktail item", showBackground = true)
@Composable
private fun CocktailItemPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        CocktailItem(
            cocktail = PreviewData.cocktail,
            onClick = {},
            onAddToCart = {},
            isFavorite = true,
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "Cocktail item — out of stock (dark)", showBackground = true)
@Composable
private fun CocktailItemOutOfStockDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        CocktailItem(
            cocktail = PreviewData.outOfStockCocktail,
            onClick = {},
            onAddToCart = {},
            isFavorite = false,
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "Cocktail item — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun CocktailItemLargeFontPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        CocktailItem(
            cocktail = PreviewData.cocktail,
            onClick = {},
            onAddToCart = {},
            isFavorite = false,
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "Cart item card", showBackground = true)
@Composable
private fun CartItemCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        CartItemCard(
            item = PreviewData.cartItem,
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            onRemove = {},
            isFavorite = true,
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "Cart item card (dark)", showBackground = true)
@Composable
private fun CartItemCardDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        CartItemCard(
            item = PreviewData.cartItem,
            onIncreaseQuantity = {},
            onDecreaseQuantity = {},
            onRemove = {},
            isFavorite = false,
            onToggleFavorite = {}
        )
    }
}

@Preview(name = "Network status — online", showBackground = true)
@Composable
private fun NetworkStatusCardOnlinePreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        NetworkStatusCard(isNetworkAvailable = true)
    }
}

@Preview(name = "Network status — offline", showBackground = true)
@Composable
private fun NetworkStatusCardOfflinePreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        NetworkStatusCard(isNetworkAvailable = false)
    }
}

@Preview(name = "Loading shimmer", showBackground = true)
@Composable
private fun CocktailLoadingShimmerPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        CocktailLoadingShimmer(itemCount = 3)
    }
}
