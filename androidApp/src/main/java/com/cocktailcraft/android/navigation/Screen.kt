package com.cocktailcraft.android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.cart
import com.cocktailcraft.resources.favorites
import com.cocktailcraft.resources.home
import com.cocktailcraft.resources.orders
import com.cocktailcraft.resources.profile
import dev.icerock.moko.resources.StringResource

/**
 * Bottom-navigation metadata. `route` is the type-safe route instance
 * passed to NavController.navigate(); match the selected tab with
 * `destination.hasRoute(screen.route::class)`.
 *
 * `tag` is a stable, locale-independent testTag (surfaced as a UiAutomator
 * resource-id via testTagsAsResourceId) used by the baseline profile
 * generator and UI tests.
 */
sealed class Screen(val route: Any, val title: StringResource, val icon: ImageVector, val tag: String) {
    object Home : Screen(HomeRoute, MR.strings.home, Icons.Filled.Home, "nav_home")
    object Cart : Screen(CartRoute, MR.strings.cart, Icons.Filled.ShoppingCart, "nav_cart")
    object Profile : Screen(ProfileRoute, MR.strings.profile, Icons.Filled.Person, "nav_profile")
    object Favorites : Screen(FavoritesRoute, MR.strings.favorites, Icons.Filled.Favorite, "nav_favorites")
    object OrderList : Screen(OrderListRoute, MR.strings.orders, Icons.AutoMirrored.Filled.List, "nav_orders")
}
