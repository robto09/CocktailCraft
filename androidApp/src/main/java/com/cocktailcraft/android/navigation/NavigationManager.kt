package com.cocktailcraft.android.navigation

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
        navController.navigate(CocktailDetailRoute(cocktailId))
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
        navController.navigate(CartRoute)
    }

    /**
     * Navigate to the home screen
     */
    fun navigateToHome() {
        navController.navigate(HomeRoute) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }

    /**
     * Navigate to the favorites screen
     */
    fun navigateToFavorites() {
        navController.navigate(FavoritesRoute) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }

    /**
     * Navigate to the profile screen
     */
    fun navigateToProfile() {
        navController.navigate(ProfileRoute) {
            popUpTo(navController.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }

    /**
     * Navigate to the order list screen
     */
    fun navigateToOrderList() {
        navController.navigate(OrderListRoute) {
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
     * Navigate to the offline mode screen
     */
    fun navigateToOfflineMode() {
        navController.navigate(OfflineModeRoute)
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
