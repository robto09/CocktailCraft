package com.cocktailcraft.android.ui.main

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.cocktailcraft.android.navigation.CartRoute
import com.cocktailcraft.android.navigation.CocktailDeepLink
import com.cocktailcraft.android.navigation.CocktailDetailRoute
import com.cocktailcraft.android.navigation.FavoritesRoute
import com.cocktailcraft.android.navigation.HomeRoute
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.navigation.OrderDetailRoute
import com.cocktailcraft.android.navigation.OrderListRoute
import com.cocktailcraft.android.navigation.ProfileRoute
import com.cocktailcraft.android.navigation.Screen
import com.cocktailcraft.android.navigation.isValidCocktailId
import com.cocktailcraft.android.screens.CartScreen
import com.cocktailcraft.android.screens.CocktailDetailScreen
import com.cocktailcraft.android.screens.FavoritesScreen
import com.cocktailcraft.android.screens.HomeScreen
import com.cocktailcraft.android.screens.OrderDetailScreen
import com.cocktailcraft.android.screens.OrderListScreen
import com.cocktailcraft.android.screens.ProfileScreen
import com.cocktailcraft.android.ui.components.AppTopBar
import com.cocktailcraft.android.ui.components.LocalToastHostState
import com.cocktailcraft.android.ui.components.OfflineModeIndicator
import com.cocktailcraft.android.ui.components.ToastSnackbarHost
import com.cocktailcraft.android.ui.components.showUiEvent
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.pluralStringResource
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.app_title
import com.cocktailcraft.resources.cart_badge_a11y
import com.cocktailcraft.util.NetworkMonitor
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
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
    // NavHost itself, after MainActivity has filtered it the same way).
    // Only a well-formed cocktail deep link gets through, rebuilt without
    // extras or launch flags (see CocktailDeepLink.navigationIntentOrNull):
    // the activity is exported, and handleDeepLink would otherwise act on
    // Navigation's internal extras from any app's intent.
    val activity = LocalActivity.current as? ComponentActivity
    DisposableEffect(activity, navController) {
        val listener = Consumer<Intent> { intent ->
            CocktailDeepLink.navigationIntentOrNull(intent)?.let(navController::handleDeepLink)
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
    // VMs are resolved with koinViewModel inside their destinations). Only
    // the ones the chrome needs on every tab are resolved here: cart (badge,
    // add-to-cart, toasts) and favorites (toasts). Home and Orders are
    // resolved by the destinations that use them — the same singles, just
    // not created before the first screen that needs them.
    val sharedCartViewModel: SharedCartViewModel = koinInject()
    val sharedFavoritesViewModel: SharedFavoritesViewModel = koinInject()
    val scope = rememberCoroutineScope()

    // Connectivity drives the offline banner directly — "offline" is purely
    // "the network is down"; there is no user-facing offline toggle.
    val networkMonitor: NetworkMonitor = koinInject()
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()

    // Cart state drives the bottom-nav badge (parity with the iOS tab badge)
    val cartUiState by sharedCartViewModel.uiState.collectAsStateWithLifecycle()

    // Toasts from every tab land in this host (LocalToastHostState below).
    val snackbarHostState = remember { SnackbarHostState() }

    // Add-to-cart (Home, Favorites) keeps the user in place and toasts its
    // outcome — "<name> added to cart", or the failure — on the tab it came
    // from (previously Android yanked the user to the Cart tab on every
    // add, and then confirmed even a failed add).
    val context = LocalContext.current
    val addToCart: (Cocktail) -> Unit = { cocktail ->
        scope.launch {
            snackbarHostState.showUiEvent(sharedCartViewModel.addToCart(cocktail), context)
        }
    }

    // Each tab draws its own bars through TabScaffold instead of one outer
    // Scaffold wrapping the NavHost. With shared outer bars, opening the
    // detail screen dropped the bars (and their padding) the moment the route
    // changed, so the outgoing tab was re-laid out without its chrome and
    // flashed behind the incoming detail screen. Now the detail destination
    // simply has no bars and the outgoing tab keeps its own until it is gone.
    val tabScaffold: @Composable (Screen, @Composable () -> Unit) -> Unit = { tab, content ->
        TabScaffold(
            tab = tab,
            tabs = items,
            isOnline = isOnline,
            cartItemCount = cartUiState.itemCount,
            snackbarHostState = snackbarHostState,
            onTabClick = { navigationManager.navigateToBottomNavDestination(it) },
            content = content
        )
    }

    CompositionLocalProvider(LocalToastHostState provides snackbarHostState) {
        // Every screen toasts its own ViewModel's events (Cart, Favorites,
        // Profile, the detail screens); cross-screen actions like add-to-cart
        // return their outcome to the caller instead.
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            composable<HomeRoute> {
                tabScaffold(Screen.Home) {
                    HomeScreen(
                        viewModel = koinInject(),
                        onAddToCart = addToCart,
                        onCocktailClick = { cocktail ->
                            // Navigate to cocktail detail screen
                            navigationManager.navigateToCocktailDetail(cocktail)
                        }
                    )
                }
            }
            composable<CartRoute> {
                tabScaffold(Screen.Cart) {
                    CartScreen(
                        viewModel = sharedCartViewModel,
                        onStartShopping = {
                            navigationManager.navigateToHome()
                        },
                        navigationManager = navigationManager,
                        orderViewModel = koinInject(),
                        favoritesViewModel = sharedFavoritesViewModel
                    )
                }
            }
            composable<ProfileRoute> {
                tabScaffold(Screen.Profile) {
                    ProfileScreen(
                        navigationManager = navigationManager
                    )
                }
            }
            composable<FavoritesRoute> {
                tabScaffold(Screen.Favorites) {
                    FavoritesScreen(
                        favoritesViewModel = sharedFavoritesViewModel,
                        onBrowseProducts = {
                            navigationManager.navigateToHome()
                        },
                        onAddToCart = addToCart,
                        onCocktailClick = { cocktail ->
                            navigationManager.navigateToCocktailDetail(cocktail)
                        }
                    )
                }
            }
            composable<OrderListRoute> {
                tabScaffold(Screen.OrderList) {
                    OrderListScreen(
                        orderViewModel = koinInject(),
                        navigationManager = navigationManager
                    )
                }
            }
            // Like the cocktail detail: its own Scaffold + AppTopBar, no tab chrome
            composable<OrderDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<OrderDetailRoute>()
                OrderDetailScreen(
                    orderId = route.orderId,
                    onBackClick = { navigationManager.navigateBack() }
                )
            }
            // No TabScaffold: the detail screen runs its own Scaffold + AppTopBar,
            // which own the system-bar insets.
            composable<CocktailDetailRoute>(
                deepLinks = listOf(
                    navDeepLink<CocktailDetailRoute>(basePath = CocktailDeepLink.BASE_PATH)
                )
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<CocktailDetailRoute>()
                if (!isValidCocktailId(route.cocktailId)) {
                    // Malformed deep link (any app can fire the VIEW intent):
                    // never hand the raw string to the detail screen. Popping
                    // the entry lands on Home for a cold launch, or back where
                    // the user was for a running app.
                    LaunchedEffect(backStackEntry) { navigationManager.navigateBack() }
                    return@composable
                }
                CocktailDetailScreen(
                    cocktailId = route.cocktailId,
                    favoritesViewModel = sharedFavoritesViewModel,
                    navigationManager = navigationManager,
                    onBackClick = { navigationManager.navigateBack() }
                )
            }
        }
    }
}

/**
 * Top bar, bottom navigation and snackbar host shared by the bottom-nav tabs.
 * [tab] is the destination this scaffold belongs to, not the current route,
 * so a tab that is animating out keeps its own title and selected item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabScaffold(
    tab: Screen,
    tabs: List<Screen>,
    isOnline: Boolean,
    cartItemCount: Int,
    snackbarHostState: SnackbarHostState,
    onTabClick: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            // Edge-to-edge inset ownership: this Column owns the
            // status-bar inset exactly once — background first so the
            // app-bar color still paints behind the transparent status
            // bar, then padding so the indicator/title start below it.
            Column(
                modifier = Modifier
                    .background(CocktailCraftTheme.extendedColors.topBar)
                    .statusBarsPadding()
            ) {
                // Banner shown while the device has no connection
                OfflineModeIndicator(isOffline = !isOnline)

                AppTopBar(
                    // Home leads with the brand, same as iOS
                    title = stringResource(if (tab == Screen.Home) MR.strings.app_title else tab.title),
                    // The wrapping Column already consumed the status-bar
                    // inset; keep only the horizontal/cutout parts here so
                    // the top inset isn't applied twice.
                    windowInsets = TopAppBarDefaults.windowInsets.exclude(WindowInsets.statusBars)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        modifier = Modifier.testTag(screen.tag),
                        icon = {
                            // Icons are decorative: the visible label names
                            // the tab. Cart shows an item-count badge, same
                            // as iOS, spoken as "2 items in cart".
                            if (screen == Screen.Cart && cartItemCount > 0) {
                                val badgeDescription = pluralStringResource(
                                    MR.plurals.cart_badge_a11y,
                                    cartItemCount,
                                    cartItemCount
                                )
                                BadgedBox(badge = {
                                    Badge(
                                        modifier = Modifier.clearAndSetSemantics {
                                            contentDescription = badgeDescription
                                        }
                                    ) { Text(cartItemCount.toString()) }
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
                        selected = screen == tab,
                        onClick = { onTabClick(screen) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            // Neutral pill like the iOS tab bar; surfaceRaised
                            // is white in light and vanishes on the untinted bar
                            indicatorColor = CocktailCraftTheme.extendedColors.neutralSubtle,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        },
        snackbarHost = { ToastSnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
