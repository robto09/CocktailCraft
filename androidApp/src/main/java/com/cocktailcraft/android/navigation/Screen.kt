package com.cocktailcraft.android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom-navigation metadata. `route` is the type-safe route instance
 * passed to NavController.navigate(); match the selected tab with
 * `destination.hasRoute(screen.route::class)`.
 */
sealed class Screen(val route: Any, val title: String, val icon: ImageVector) {
    object Home : Screen(HomeRoute, "Home", Icons.Filled.Home)
    object Cart : Screen(CartRoute, "Cart", Icons.Filled.ShoppingCart)
    object Profile : Screen(ProfileRoute, "Profile", Icons.Filled.Person)
    object Favorites : Screen(FavoritesRoute, "Favorites", Icons.Filled.Favorite)
    object OrderList : Screen(OrderListRoute, "Orders", Icons.Filled.List)
}
