package com.cocktailcraft.android.ui.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.cocktailcraft.android.R
import com.cocktailcraft.android.navigation.COCKTAIL_DEEP_LINK_BASE_PATH
import com.cocktailcraft.android.navigation.CartRoute
import com.cocktailcraft.android.navigation.CocktailDetailRoute
import com.cocktailcraft.android.navigation.FavoritesRoute
import com.cocktailcraft.android.navigation.HomeRoute
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.navigation.OfflineModeRoute
import com.cocktailcraft.android.navigation.OrderListRoute
import com.cocktailcraft.android.navigation.ProfileRoute
import com.cocktailcraft.android.navigation.Screen
import com.cocktailcraft.android.screens.CartScreen
import com.cocktailcraft.android.screens.CocktailDetailScreen
import com.cocktailcraft.android.screens.FavoritesScreen
import com.cocktailcraft.android.screens.HomeScreen
import com.cocktailcraft.android.screens.OfflineModeScreen
import com.cocktailcraft.android.screens.OrderListScreen
import com.cocktailcraft.android.screens.ProfileScreen
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.components.AppTopBar
import com.cocktailcraft.android.ui.components.OfflineModeIndicator
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedOfflineModeViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // Create the navigation manager
    val navigationManager = remember { NavigationManager(navController) }

    // MainActivity is singleTop, so a widget deep link while the app is
    // already running arrives via onNewIntent instead of a fresh launch —
    // forward it to the NavController (the launch intent is handled by
    // NavHost itself). Widget PendingIntents are delivered with a
    // framework-forced FLAG_ACTIVITY_NEW_TASK, which makes handleDeepLink
    // finish and relaunch the activity instead of navigating in place —
    // strip launch flags first, we are already in the right task.
    val activity = LocalActivity.current as? ComponentActivity
    DisposableEffect(activity, navController) {
        val listener = Consumer<Intent> { intent ->
            navController.handleDeepLink(Intent(intent).setFlags(0))
        }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }

    val items = listOf(
        Screen.Home,
        Screen.Cart,
        Screen.Favorites,
        Screen.OrderList,
        Screen.Profile
    )

    // Shared KMP ViewModels (Koin singles — app-scoped by design; screen-scoped
    // VMs are resolved with koinViewModel inside their destinations)
    val sharedOrderViewModel: SharedOrderViewModel = koinInject()
    val sharedCartViewModel: SharedCartViewModel = koinInject()
    val sharedHomeViewModel: SharedHomeViewModel = koinInject()
    val sharedFavoritesViewModel: SharedFavoritesViewModel = koinInject()
    val sharedOfflineModeViewModel: SharedOfflineModeViewModel = koinInject()
    val scope = rememberCoroutineScope()

    // Current destination for conditional rendering (type-safe checks)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isDetailScreen = currentDestination?.hasRoute<CocktailDetailRoute>() == true
    val isOfflineModeScreen = currentDestination?.hasRoute<OfflineModeRoute>() == true

    // Get offline mode state
    val offlineState by sharedOfflineModeViewModel.uiState.collectAsStateWithLifecycle()
    val isOfflineModeEnabled = offlineState.isOfflineModeEnabled
    val isNetworkAvailable = offlineState.isNetworkAvailable

    // Cart state drives the bottom-nav badge (parity with the iOS tab badge)
    val cartUiState by sharedCartViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // Only show the main top bar if we're NOT on the detail screen
            if (!isDetailScreen) {
                // Edge-to-edge inset ownership (AN-6): this Column owns the
                // status-bar inset exactly once — background first so the
                // app-bar color still paints behind the transparent status
                // bar, then padding so the indicator/title start below it.
                Column(
                    modifier = Modifier
                        .background(AppColors.Primary)
                        .statusBarsPadding()
                ) {
                    // Show offline mode indicator if offline and not already on the offline mode screen
                    if (!isOfflineModeScreen) {
                        OfflineModeIndicator(
                            isOffline = !isNetworkAvailable || isOfflineModeEnabled,
                            isOfflineModeEnabled = isOfflineModeEnabled,
                            onClick = {
                                // Navigate to offline mode settings
                                navigationManager.navigateToOfflineMode()
                            }
                        )
                    }

                    AppTopBar(
                        title = when {
                            // Home leads with the brand, same as iOS
                            currentDestination?.hasRoute<HomeRoute>() == true -> stringResource(R.string.app_name)
                            currentDestination?.hasRoute<CartRoute>() == true -> stringResource(R.string.cart)
                            currentDestination?.hasRoute<FavoritesRoute>() == true -> stringResource(R.string.favorites)
                            currentDestination?.hasRoute<OrderListRoute>() == true -> stringResource(R.string.orders)
                            currentDestination?.hasRoute<ProfileRoute>() == true -> stringResource(R.string.profile)
                            isOfflineModeScreen -> stringResource(R.string.offline_mode)
                            else -> stringResource(R.string.app_name)
                        },
                        // Back button only for the OfflineMode screen
                        showBackButton = isOfflineModeScreen,
                        onBackClick = { navigationManager.navigateBack() },
                        // The wrapping Column already consumed the status-bar
                        // inset; keep only the horizontal/cutout parts here so
                        // the top inset isn't applied twice (AN-6).
                        windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.statusBars)
                    )
                }
            }
        },
        bottomBar = {
            // Only show the bottom navigation bar if we're NOT on the detail screen or offline mode screen
            if (!isDetailScreen && !isOfflineModeScreen) {
                NavigationBar(
                    containerColor = AppColors.Surface,
                    contentColor = AppColors.Primary,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            modifier = Modifier.testTag(screen.tag),
                            icon = {
                                // Cart shows an item-count badge, same as iOS
                                if (screen == Screen.Cart && cartUiState.itemCount > 0) {
                                    BadgedBox(badge = {
                                        Badge { Text(cartUiState.itemCount.toString()) }
                                    }) {
                                        Icon(screen.icon, contentDescription = stringResource(screen.titleRes))
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = stringResource(screen.titleRes))
                                }
                            },
                            label = {
                                Text(
                                    stringResource(screen.titleRes),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(screen.route::class) } == true,
                            onClick = {
                                navigationManager.navigateToBottomNavDestination(screen)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.Primary,
                                selectedTextColor = AppColors.Primary,
                                indicatorColor = AppColors.Surface,
                                unselectedIconColor = AppColors.Gray,
                                unselectedTextColor = AppColors.Gray
                            )
                        )
                    }
                }
            }
        },
        containerColor = AppColors.Background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    viewModel = sharedHomeViewModel,
                    favoritesViewModel = sharedFavoritesViewModel,
                    onAddToCart = { cocktail ->
                        // Add to cart and then navigate to cart
                        scope.launch { sharedCartViewModel.addToCart(cocktail) }
                        navigationManager.navigateToCart()
                    },
                    onCocktailClick = { cocktail ->
                        // Navigate to cocktail detail screen
                        navigationManager.navigateToCocktailDetail(cocktail)
                    }
                )
            }
            composable<CartRoute> {
                CartScreen(
                    viewModel = sharedCartViewModel,
                    onStartShopping = {
                        navigationManager.navigateToHome()
                    },
                    navigationManager = navigationManager,
                    orderViewModel = sharedOrderViewModel,
                    favoritesViewModel = sharedFavoritesViewModel
                )
            }
            composable<ProfileRoute> {
                ProfileScreen(
                    navigationManager = navigationManager
                )
            }
            composable<FavoritesRoute> {
                FavoritesScreen(
                    cartViewModel = sharedCartViewModel,
                    favoritesViewModel = sharedFavoritesViewModel,
                    onBrowseProducts = {
                        navigationManager.navigateToHome()
                    },
                    onAddToCart = { cocktail ->
                        // Add to cart and then navigate to cart
                        scope.launch { sharedCartViewModel.addToCart(cocktail) }
                        navigationManager.navigateToCart()
                    }
                )
            }
            composable<OrderListRoute> {
                OrderListScreen(
                    orderViewModel = sharedOrderViewModel,
                    navigationManager = navigationManager
                )
            }
            composable<OfflineModeRoute> {
                OfflineModeScreen(
                    viewModel = sharedOfflineModeViewModel,
                    onBackClick = { navigationManager.navigateBack() },
                    onCocktailClick = { cocktail ->
                        navigationManager.navigateToCocktailDetail(cocktail)
                    }
                )
            }

            composable<CocktailDetailRoute>(
                deepLinks = listOf(
                    navDeepLink<CocktailDetailRoute>(basePath = COCKTAIL_DEEP_LINK_BASE_PATH)
                )
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<CocktailDetailRoute>()
                CocktailDetailScreen(
                    cocktailId = route.cocktailId,
                    cartViewModel = sharedCartViewModel,
                    favoritesViewModel = sharedFavoritesViewModel,
                    navigationManager = navigationManager,
                    onBackClick = { navigationManager.navigateBack() },
                    onAddToCart = { cocktailToAdd ->
                        scope.launch { sharedCartViewModel.addToCart(cocktailToAdd) }
                        navigationManager.navigateToCart()
                    }
                )
            }
        }
    }
}