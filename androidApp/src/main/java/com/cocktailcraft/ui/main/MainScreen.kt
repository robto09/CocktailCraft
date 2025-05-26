package com.cocktailcraft.ui.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cocktailcraft.navigation.NavigationManager
import com.cocktailcraft.navigation.Screen
import com.cocktailcraft.screens.CartScreen
import com.cocktailcraft.screens.CocktailDetailScreen
import com.cocktailcraft.screens.FavoritesScreen
import com.cocktailcraft.screens.HomeScreen
import com.cocktailcraft.screens.OfflineModeScreen
import com.cocktailcraft.screens.OrderListScreen
import com.cocktailcraft.screens.ProfileScreen
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.FavoritesViewModel
import com.cocktailcraft.viewmodel.HomeViewModel
import com.cocktailcraft.viewmodel.OfflineModeViewModel
import com.cocktailcraft.viewmodel.OrderViewModel
import com.cocktailcraft.viewmodel.ReviewViewModel
import com.cocktailcraft.viewmodel.ThemeViewModel
import com.cocktailcraft.ui.components.OfflineModeIndicator
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    // Create the navigation manager
    val navigationManager = remember { NavigationManager(navController) }

    val items = listOf(
        Screen.Home,
        Screen.Cart,
        Screen.Favorites,
        Screen.OrderList,
        Screen.Profile
    )

    // Create shared ViewModels for the entire app
    val sharedOrderViewModel: OrderViewModel = viewModel()
    val sharedCartViewModel: CartViewModel = viewModel()
    val sharedReviewViewModel: ReviewViewModel = viewModel()
    val sharedHomeViewModel: HomeViewModel = viewModel()
    val sharedFavoritesViewModel: FavoritesViewModel = viewModel()
    val sharedThemeViewModel: ThemeViewModel = viewModel()
    val sharedOfflineModeViewModel: OfflineModeViewModel = viewModel()

    // Get the current route for conditional rendering
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDetailScreen = currentRoute?.startsWith("cocktail_detail") == true

    // Get offline mode state
    val isOfflineModeEnabled by sharedOfflineModeViewModel.isOfflineModeEnabled.collectAsState()
    val isNetworkAvailable by sharedOfflineModeViewModel.isNetworkAvailable.collectAsState()

    Scaffold(
        topBar = {
            // Only show the main top bar if we're NOT on the detail screen
            if (!isDetailScreen) {
                Column {
                    // Show offline mode indicator if offline and not already on the offline mode screen
                    if (currentRoute != Screen.OfflineMode.route) {
                        OfflineModeIndicator(
                            isOffline = !isNetworkAvailable || isOfflineModeEnabled,
                            isOfflineModeEnabled = isOfflineModeEnabled,
                            onClick = {
                                // Navigate to offline mode settings
                                navigationManager.navigateToOfflineMode()
                            }
                        )
                    }

                    TopAppBar(
                        title = {
                            // Normal title without search functionality
                            Text(
                                text = when (currentRoute) {
                                    Screen.Home.route -> "My Bar"
                                    Screen.Cart.route -> "Cart"
                                    Screen.Favorites.route -> "Favorites"
                                    Screen.OrderList.route -> "Recipes"
                                    Screen.Profile.route -> "Profile"
                                    Screen.OfflineMode.route -> "Offline Mode"
                                    else -> "Cocktail Bar"
                                },
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            // Show back button only for the OfflineMode screen
                            if (currentRoute == Screen.OfflineMode.route) {
                                IconButton(onClick = { navigationManager.navigateBack() }) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        actions = {
                            // Removed search button/functionality
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AppColors.Primary,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )

                    // Add a divider to create separation between top bar and content
                    Divider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }
        },
        bottomBar = {
            // Only show the bottom navigation bar if we're NOT on the detail screen or offline mode screen
            val currentScreenRoute = navController.currentDestination?.route
            if (!isDetailScreen && currentScreenRoute != Screen.OfflineMode.route) {
                NavigationBar(
                    containerColor = AppColors.Surface,
                    contentColor = AppColors.Primary,
                    tonalElevation = 8.dp
                ) {
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(screen.icon, contentDescription = screen.title)
                            },
                            label = {
                                Text(screen.title, fontSize = 12.sp)
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = sharedHomeViewModel,
                    favoritesViewModel = sharedFavoritesViewModel,
                    onAddToCart = { cocktail ->
                        // Add to cart and then navigate to cart
                        sharedCartViewModel.addToCart(cocktail)
                        navigationManager.navigateToCart()
                    },
                    onCocktailClick = { cocktail ->
                        // Navigate to cocktail detail screen
                        navigationManager.navigateToCocktailDetail(cocktail)
                    }
                )
            }
            composable(Screen.Cart.route) {
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
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navigationManager = navigationManager,
                    themeViewModel = sharedThemeViewModel
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    cartViewModel = sharedCartViewModel,
                    favoritesViewModel = sharedFavoritesViewModel,
                    onBrowseProducts = {
                        navigationManager.navigateToHome()
                    },
                    onAddToCart = { cocktail ->
                        // Add to cart and then navigate to cart
                        sharedCartViewModel.addToCart(cocktail)
                        navigationManager.navigateToCart()
                    }
                )
            }
            composable(Screen.OrderList.route) {
                OrderListScreen(
                    orderViewModel = sharedOrderViewModel,
                    navigationManager = navigationManager
                )
            }
            composable(Screen.OfflineMode.route) {
                OfflineModeScreen(
                    viewModel = sharedOfflineModeViewModel,
                    onBackClick = { navigationManager.navigateBack() },
                    onCocktailClick = { cocktail ->
                        navigationManager.navigateToCocktailDetail(cocktail)
                    }
                )
            }

            composable(
                route = Screen.CocktailDetail.route,
                arguments = listOf(navArgument("cocktailId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cocktailId = backStackEntry.arguments?.getString("cocktailId") ?: ""
                CocktailDetailScreen(
                    cocktailId = cocktailId,
                    homeViewModel = sharedHomeViewModel,
                    cartViewModel = sharedCartViewModel,
                    reviewViewModel = sharedReviewViewModel,
                    favoritesViewModel = sharedFavoritesViewModel,
                    navigationManager = navigationManager,
                    onBackClick = { navigationManager.navigateBack() },
                    onAddToCart = { cocktailToAdd ->
                        sharedCartViewModel.addToCart(cocktailToAdd)
                        navigationManager.navigateToCart()
                    }
                )
            }
        }
    }
}