package com.cocktailcraft.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cocktailcraft.domain.model.Cocktail

/**
 * Centralized navigation manager for the app.
 * All navigation actions should go through this class to maintain consistency.
 */
class NavigationManager(private val navController: NavController) {

    /**
     * Navigate to the cocktail detail screen
     */
    fun navigateToCocktailDetail(cocktailId: String) {
        navController.navigate(Screen.CocktailDetail.createRoute(cocktailId))
    }

    /**
     * Navigate to the cocktail detail screen from a cocktail object
     */
    fun navigateToCocktailDetail(cocktail: Cocktail) {
        navigateToCocktailDetail(cocktail.id)
    }

    /**
     * Navigate to the cart screen
     */
    fun navigateToCart() {
        navController.navigate(Screen.Cart.route)
    }

    /**
     * Navigate to the home screen
     */
    fun navigateToHome() {
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }

    /**
     * Navigate to the favorites screen
     */
    fun navigateToFavorites() {
        navController.navigate(Screen.Favorites.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }

    /**
     * Navigate to the profile screen
     */
    fun navigateToProfile() {
        navController.navigate(Screen.Profile.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }

    /**
     * Navigate to the order list screen
     */
    fun navigateToOrderList() {
        navController.navigate(Screen.OrderList.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }

    /**
     * Navigate back to the previous screen
     */
    fun navigateBack() {
        navController.popBackStack()
    }

    /**
     * Navigate to a bottom navigation destination
     */
    fun navigateToBottomNavDestination(screen: Screen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }
} 