package com.cocktailcraft.android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.cocktailcraft.android.R

/**
 * Bottom-navigation metadata. `route` is the type-safe route instance
 * passed to NavController.navigate(); match the selected tab with
 * `destination.hasRoute(screen.route::class)`.
 *
 * `tag` is a stable, locale-independent testTag (surfaced as a UiAutomator
 * resource-id via testTagsAsResourceId) used by the baseline profile
 * generator and UI tests.
 */
sealed class Screen(
    val route: Any,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val tag: String
) {
    object Home : Screen(HomeRoute, R.string.home, Icons.Filled.Home, "nav_home")
    object Cart : Screen(CartRoute, R.string.cart, Icons.Filled.ShoppingCart, "nav_cart")
    object Profile : Screen(ProfileRoute, R.string.profile, Icons.Filled.Person, "nav_profile")
    object Favorites : Screen(FavoritesRoute, R.string.favorites, Icons.Filled.Favorite, "nav_favorites")
    object OrderList : Screen(OrderListRoute, R.string.orders, Icons.Filled.List, "nav_orders")
}
