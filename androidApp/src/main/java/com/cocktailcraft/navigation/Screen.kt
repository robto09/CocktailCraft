package com.cocktailcraft.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Cart : Screen("cart", "Cart", Icons.Filled.ShoppingCart)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Favorites : Screen("favorites", "Favorites", Icons.Filled.Favorite)
    object OrderList : Screen("orders", "Orders", Icons.AutoMirrored.Filled.List)
    object CocktailDetail : Screen("cocktail_detail/{cocktailId}", "Cocktail Detail", Icons.Filled.Home) {
        fun createRoute(cocktailId: String) = "cocktail_detail/$cocktailId"
    }

    object OfflineMode : Screen("offline_mode", "Offline Mode", Icons.Filled.CloudOff)
}