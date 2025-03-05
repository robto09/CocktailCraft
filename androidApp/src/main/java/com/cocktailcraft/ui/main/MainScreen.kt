package com.cocktailcraft.ui.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.cocktailcraft.screens.OrderListScreen
import com.cocktailcraft.screens.ProfileScreen
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.FavoritesViewModel
import com.cocktailcraft.viewmodel.HomeViewModel
import com.cocktailcraft.viewmodel.OrderViewModel
import com.cocktailcraft.viewmodel.ReviewViewModel

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
    
    // Get the current route for conditional rendering
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDetailScreen = currentRoute?.startsWith("cocktail_detail") == true
    
    Scaffold(
        topBar = {
            // Only show the main top bar if we're NOT on the detail screen
            if (!isDetailScreen) {
                Column {
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
                                    else -> "Cocktail Bar"
                                },
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }
        },
        bottomBar = {
            // Only show the bottom navigation bar if we're NOT on the detail screen
            if (!isDetailScreen) {
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
                    navController = navController,
                    viewModel = sharedHomeViewModel,
                    cartViewModel = sharedCartViewModel,
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
                ProfileScreen(navigationManager = navigationManager)
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
                    onBackClick = { navigationManager.navigateBack() },
                    onAddToCart = { cocktailToAdd ->
                        sharedCartViewModel.addToCart(cocktailToAdd) // Pass the entire cocktail object
                        navigationManager.navigateToCart() // Navigate to cart screen after adding item
                    }
                )
            }
        }
    }
} 