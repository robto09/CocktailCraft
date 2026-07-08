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
        navigateToTopLevelDestination(HomeRoute)
    }

    /**
     * Navigate to the favorites screen
     */
    fun navigateToFavorites() {
        navigateToTopLevelDestination(FavoritesRoute)
    }

    /**
     * Navigate to the profile screen
     */
    fun navigateToProfile() {
        navigateToTopLevelDestination(ProfileRoute)
    }

    /**
     * Navigate to the order list screen
     */
    fun navigateToOrderList() {
        navigateToTopLevelDestination(OrderListRoute)
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
        navigateToTopLevelDestination(screen.route)
    }

    /**
     * Switch to a top-level (bottom-nav) destination, saving the state of the
     * tab being left and restoring any previously saved state of the target —
     * otherwise every tab switch loses scroll position and rememberSaveable
     * state and re-triggers each screen's LaunchedEffect(Unit) loads.
     */
    private fun navigateToTopLevelDestination(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
