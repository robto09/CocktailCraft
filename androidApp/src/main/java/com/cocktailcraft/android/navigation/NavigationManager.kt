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
     * Navigate to an order's detail screen (pushed above the Orders tab)
     */
    fun navigateToOrderDetail(orderId: String) {
        navController.navigate(OrderDetailRoute(orderId))
    }

    /**
     * Navigate to the home screen
     */
    fun navigateToHome() {
        navigateToTopLevelDestination(HomeRoute)
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
            // Never restoreState for Home: a non-inclusive popUpTo(start) with
            // saveState = true also keys the popped segment to the START
            // destination's id, so navigate(Home) { restoreState = true } would
            // re-push the segment that was just popped (e.g. Cart) and leave
            // Home unreachable. Home is never popped here, so it has no saved
            // state of its own to restore; the live entry keeps its state.
            restoreState = route != HomeRoute
        }
    }
}
