package com.cocktailcraft.android.ui.main

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.cocktailcraft.android.navigation.COCKTAIL_DEEP_LINK_BASE_PATH
import com.cocktailcraft.android.navigation.CartRoute
import com.cocktailcraft.android.navigation.CocktailDetailRoute
import com.cocktailcraft.android.navigation.FavoritesRoute
import com.cocktailcraft.android.navigation.HomeRoute
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.navigation.OrderListRoute
import com.cocktailcraft.android.navigation.ProfileRoute
import com.cocktailcraft.android.navigation.Screen
import com.cocktailcraft.android.screens.CartScreen
import com.cocktailcraft.android.screens.CocktailDetailScreen
import com.cocktailcraft.android.screens.FavoritesScreen
import com.cocktailcraft.android.screens.HomeScreen
import com.cocktailcraft.android.screens.OrderListScreen
import com.cocktailcraft.android.screens.ProfileScreen
import com.cocktailcraft.android.ui.components.AppTopBar
import com.cocktailcraft.android.ui.components.OfflineModeIndicator
import com.cocktailcraft.android.util.getString
import com.cocktailcraft.android.util.pluralStringResource
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.added_to_cart
import com.cocktailcraft.resources.app_title
import com.cocktailcraft.resources.cart
import com.cocktailcraft.resources.cart_badge_a11y
import com.cocktailcraft.resources.favorites
import com.cocktailcraft.resources.orders
import com.cocktailcraft.resources.profile
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedOrderViewModel
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
    val scope = rememberCoroutineScope()

    // Current destination for conditional rendering (type-safe checks)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isDetailScreen = currentDestination?.hasRoute<CocktailDetailRoute>() == true

    // Connectivity drives the offline banner directly — "offline" is purely
    // "the network is down"; there is no user-facing offline toggle.
    val networkMonitor: NetworkMonitor = koinInject()
    val isOnline by networkMonitor.isOnline.collectAsState()

    // Cart state drives the bottom-nav badge (parity with the iOS tab badge)
    val cartUiState by sharedCartViewModel.uiState.collectAsStateWithLifecycle()

    // Add-to-cart keeps the user in place and confirms with a snackbar,
    // matching the iOS toast behavior (previously Android yanked the user to
    // the Cart tab on every add).
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val addToCartAndConfirm: (Cocktail) -> Unit = { cocktail ->
        scope.launch {
            sharedCartViewModel.addToCart(cocktail)
            snackbarHostState.showSnackbar(
                message = context.getString(MR.strings.added_to_cart, cocktail.name),
                duration = SnackbarDuration.Short
            )
        }
    }

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
                        .background(MaterialTheme.colorScheme.primary)
                        .statusBarsPadding()
                ) {
                    // Banner shown while the device has no connection
                    OfflineModeIndicator(isOffline = !isOnline)

                    AppTopBar(
                        title = when {
                            // Home leads with the brand, same as iOS
                            currentDestination?.hasRoute<HomeRoute>() == true -> stringResource(MR.strings.app_title)
                            currentDestination?.hasRoute<CartRoute>() == true -> stringResource(MR.strings.cart)
                            currentDestination?.hasRoute<FavoritesRoute>() == true -> stringResource(MR.strings.favorites)
                            currentDestination?.hasRoute<OrderListRoute>() == true -> stringResource(MR.strings.orders)
                            currentDestination?.hasRoute<ProfileRoute>() == true -> stringResource(MR.strings.profile)
                            else -> stringResource(MR.strings.app_title)
                        },
                        // The wrapping Column already consumed the status-bar
                        // inset; keep only the horizontal/cutout parts here so
                        // the top inset isn't applied twice (AN-6).
                        windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.statusBars)
                    )
                }
            }
        },
        bottomBar = {
            // Only show the bottom navigation bar if we're NOT on the detail screen
            if (!isDetailScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            modifier = Modifier.testTag(screen.tag),
                            icon = {
                                // Icons are decorative: the visible label names
                                // the tab. Cart shows an item-count badge, same
                                // as iOS, spoken as "2 items in cart".
                                if (screen == Screen.Cart && cartUiState.itemCount > 0) {
                                    val badgeDescription = pluralStringResource(
                                        MR.plurals.cart_badge_a11y,
                                        cartUiState.itemCount,
                                        cartUiState.itemCount
                                    )
                                    BadgedBox(badge = {
                                        Badge(
                                            modifier = Modifier.clearAndSetSemantics {
                                                contentDescription = badgeDescription
                                            }
                                        ) { Text(cartUiState.itemCount.toString()) }
                                    }) {
                                        Icon(screen.icon, contentDescription = null)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = null)
                                }
                            },
                            label = {
                                Text(
                                    stringResource(screen.title),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(screen.route::class) } == true,
                            onClick = {
                                navigationManager.navigateToBottomNavDestination(screen)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surface,
                                unselectedIconColor = MaterialTheme.colorScheme.outline,
                                unselectedTextColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
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
                    onAddToCart = addToCartAndConfirm,
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
                    onAddToCart = addToCartAndConfirm
                )
            }
            composable<OrderListRoute> {
                OrderListScreen(
                    orderViewModel = sharedOrderViewModel,
                    navigationManager = navigationManager
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
                    // The detail screen shows its own snackbar confirmation,
                    // so only the cart mutation happens here.
                    onAddToCart = { cocktailToAdd ->
                        scope.launch { sharedCartViewModel.addToCart(cocktailToAdd) }
                    }
                )
            }
        }
    }
}