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
 */
sealed class Screen(val route: Any, @StringRes val titleRes: Int, val icon: ImageVector) {
    object Home : Screen(HomeRoute, R.string.home, Icons.Filled.Home)
    object Cart : Screen(CartRoute, R.string.cart, Icons.Filled.ShoppingCart)
    object Profile : Screen(ProfileRoute, R.string.profile, Icons.Filled.Person)
    object Favorites : Screen(FavoritesRoute, R.string.favorites, Icons.Filled.Favorite)
    object OrderList : Screen(OrderListRoute, R.string.orders, Icons.Filled.List)
}
