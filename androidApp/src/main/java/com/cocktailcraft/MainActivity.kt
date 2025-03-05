package com.cocktailcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavHostController

import androidx.navigation.compose.*
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.cocktailcraft.viewmodel.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor

import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailCartItem
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.FavoritesViewModel
import com.cocktailcraft.viewmodel.HomeViewModel
import com.cocktailcraft.viewmodel.ProfileViewModel
import com.cocktailcraft.viewmodel.OrderViewModel
import com.cocktailcraft.viewmodel.ReviewViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cocktailcraft.domain.model.Order
import com.cocktailcraft.domain.model.OrderItem
import com.cocktailcraft.domain.model.Review
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.FavoriteBorder

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import com.cocktailcraft.ui.theme.CocktailBarTheme
import kotlin.random.Random
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.screens.CartScreen

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CocktailBarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CocktailCraft()
                }
            }
        }
    }
}

data class BreadProduct(
    val name: String,
    val description: String,
    val price: Double,
    val imageResId: Int
)

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Cart : Screen("cart", "Cart", Icons.Filled.ShoppingCart)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Favorites : Screen("favorites", "Favorites", Icons.Filled.Favorite)
    object OrderList : Screen("orders", "Orders", Icons.AutoMirrored.Filled.List)
    object CocktailDetail : Screen("cocktail_detail/{cocktailId}", "Cocktail Detail", Icons.Filled.Home) {
        fun createRoute(cocktailId: String) = "cocktail_detail/$cocktailId"
    }
}

// Define SortOption enum at the top level, outside of any function
enum class SortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    PRICE_ASC("Price (Low to High)"),
    PRICE_DESC("Price (High to Low)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailCraft() {
    val navController = rememberNavController()
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
    
    // Search state management
    val isSearchActive = sharedHomeViewModel.isSearchActive.collectAsState()
    val searchQuery = sharedHomeViewModel.searchQuery.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Normal title without search functionality
                    Text(
                        text = when (navController.currentBackStackEntryAsState().value?.destination?.route) {
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.Surface,
                contentColor = AppColors.Primary,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
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
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
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
                    cartViewModel = sharedCartViewModel,
                    onAddToCart = { cocktail ->
                        // Add to cart and then navigate to cart
                        sharedCartViewModel.addToCart(cocktail)
                        navController.navigate(Screen.Cart.route)
                    },
                    onCocktailClick = { cocktail ->
                        // Navigate to cocktail detail screen
                        navController.navigate(Screen.CocktailDetail.createRoute(cocktail.id))
                    }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = sharedCartViewModel,
                    onStartShopping = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    },
                    navController = navController,
                    orderViewModel = sharedOrderViewModel
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    cartViewModel = sharedCartViewModel,
                    favoritesViewModel = viewModel(),
                    onBrowseProducts = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    },
                    onAddToCart = { cocktail ->
                        // Add to cart and then navigate to cart
                        sharedCartViewModel.addToCart(cocktail)
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }
            composable(Screen.OrderList.route) {
                OrderListScreen(
                    orderViewModel = sharedOrderViewModel,
                    navController = navController
                )
            }
            composable(
                route = Screen.CocktailDetail.route,
                arguments = listOf(navArgument("cocktailId") { type = NavType.StringType })
            ) { backStackEntry ->
                val cocktailId = backStackEntry.arguments?.getString("cocktailId") ?: ""
                CocktailDetailScreen(
                    cocktailId = cocktailId,
                    homeViewModel = viewModel(),
                    cartViewModel = sharedCartViewModel,
                    reviewViewModel = sharedReviewViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddToCart = { cocktailToAdd ->
                        sharedCartViewModel.addToCart(cocktailToAdd) // Pass the entire cocktail object
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveHeader(
    title: String,
    showSearchIcon: Boolean = true,
    onSearchClick: () -> Unit = {},
    elevation: Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        // Main background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(AppColors.Primary)
                .shadow(elevation)
        )
        
        // Header content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (showSearchIcon) {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    cartViewModel: CartViewModel,
    onAddToCart: (Cocktail) -> Unit = {},
    onCocktailClick: (Cocktail) -> Unit = {}
) {
    val cocktails by viewModel.cocktails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchCocktails(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search cocktails...") },
            leadingIcon = { 
                Icon(
                    Icons.Filled.Search, 
                    contentDescription = "Search",
                    tint = AppColors.Gray
                ) 
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.toggleSearchMode(false) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = AppColors.Gray
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                cursorColor = AppColors.Primary,
                focusedIndicatorColor = AppColors.Primary,
                focusedLeadingIconColor = AppColors.Primary
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else if (error?.isNotEmpty() == true) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "",
                    color = AppColors.Error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Main content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                // Only show featured section when not searching
                if (!isSearchActive) {
                    // Featured section (random cocktail)
                    val featuredCocktail = cocktails.randomOrNull()
                    if (featuredCocktail != null) {
                        item {
                            Text(
                                text = "Featured Cocktail",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            CocktailItem(
                                cocktail = featuredCocktail,
                                onClick = { onCocktailClick(featuredCocktail) },
                                onAddToCart = onAddToCart,
                                homeViewModel = viewModel,
                                isFavorite = favorites.any { it.id == featuredCocktail.id },
                                onToggleFavorite = { cocktail -> 
                                    viewModel.toggleFavorite(cocktail)
                                }
                            )
                        }
                    }
                }
                
                // All cocktails list or search results
                item {
                    Text(
                        text = if (isSearchActive) "Search Results" else "All Cocktails",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
                
                itemsIndexed(cocktails) { _, cocktail ->
                    CocktailItem(
                        cocktail = cocktail,
                        onClick = { onCocktailClick(cocktail) },
                        onAddToCart = onAddToCart,
                        homeViewModel = viewModel,
                        isFavorite = favorites.any { it.id == cocktail.id },
                        onToggleFavorite = { cocktail -> 
                            viewModel.toggleFavorite(cocktail)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: Color = AppColors.Primary,
    unselectedColor: Color = AppColors.ChipBackground
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) selectedColor else unselectedColor,
        contentColor = if (selected) Color.White else AppColors.TextPrimary,
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    stars: Int = 5,
    starsColor: Color = AppColors.Secondary
) {
    Row(modifier = modifier) {
        repeat(stars) { index ->
            val starAlpha = when {
                index < rating.toInt() -> 1f
                index == rating.toInt() && rating % 1 != 0f -> rating % 1
                else -> 0.3f
            }
            
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = starsColor.copy(alpha = starAlpha),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

@Composable
fun OrderListScreen(
    orderViewModel: OrderViewModel,
    navController: NavController
) {
    val orders by orderViewModel.orders.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val error by orderViewModel.error.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else if (error?.isNotEmpty() == true) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "",
                    color = AppColors.Error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (orders.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = AppColors.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No orders yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Your order history will appear here",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { navController.navigate(Screen.Home.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Browse Cocktails")
                    }
                }
            }
        } else {
            // Order list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Your Orders",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                itemsIndexed(orders) { _, order ->
                    OrderItem(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id.takeLast(5)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                
                Text(
                    text = order.date,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Order items
            Column {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.name}",
                            fontSize = 14.sp,
                            color = AppColors.TextPrimary
                        )
                        
                        Text(
                            text = "$${String.format("%.2f", item.price * item.quantity)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextPrimary
                        )
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = AppColors.LightGray
            )
            
            // Total row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                
                Text(
                    text = "$${String.format("%.2f", order.total)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Order status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            when (order.status) {
                                "Completed" -> Color(0xFF4CAF50)
                                "In Progress" -> Color(0xFFFFA000)
                                else -> AppColors.Gray
                            },
                            CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = order.status,
                    fontSize = 14.sp,
                    color = when (order.status) {
                        "Completed" -> Color(0xFF4CAF50)
                        "In Progress" -> Color(0xFFFFA000)
                        else -> AppColors.Gray
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailDetailScreen(
    cocktailId: String,
    homeViewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    reviewViewModel: ReviewViewModel,
    onBackClick: () -> Unit,
    onAddToCart: (Cocktail) -> Unit
) {
    // Add a loading state to track when data is being fetched
    var isLoading by remember { mutableStateOf(true) }
    
    // Use remember to prevent unnecessary recompositions
    val cocktailState = remember { homeViewModel.getCocktailById(cocktailId) }
    val cocktail by cocktailState.collectAsState(initial = null)
    
    // Properly collect reviews as a state
    val reviewsMap by reviewViewModel.reviews.collectAsState()
    // Safely get reviews for this cocktail
    val reviews = reviewsMap[cocktailId] ?: emptyList()
    val favorites by homeViewModel.favorites.collectAsState()
    val isFavorite = cocktail?.let { favorites.any { fav -> fav.id == it.id } } ?: false
    
    // Update loading state when cocktail data changes
    LaunchedEffect(cocktail) {
        if (cocktail != null) {
            isLoading = false
        }
    }
    
    var showReviewDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(0f) }
    var userComment by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write a Review") },
            text = {
                Column {
                    // Name input
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = AppColors.LightGray
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Rating selector
                    Text(
                        text = "Your Rating",
                        fontSize = 16.sp,
                        color = AppColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { userRating = index + 1f },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rate ${index + 1}",
                                    tint = if (index < userRating) AppColors.Secondary else AppColors.LightGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    
                    // Review text
                    OutlinedTextField(
                        value = userComment,
                        onValueChange = { userComment = it },
                        label = { Text("Your Review") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = AppColors.LightGray
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userName.isNotBlank() && userRating > 0) {
                            try {
                                // Use the safer method instead of creating Review directly
                                reviewViewModel.createAndAddReview(
                                    cocktailId = cocktailId,
                                    userName = userName,
                                    rating = userRating,
                                    comment = userComment
                                )
                                
                                showReviewDialog = false
                                
                                // Reset fields
                                userName = ""
                                userRating = 0f
                                userComment = ""
                            } catch (e: Exception) {
                                // Handle any exceptions that might still occur
                                e.printStackTrace()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showReviewDialog = false },
                    border = BorderStroke(1.dp, AppColors.Primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = AppColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = cocktail?.name ?: "Cocktail Details",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // Show loading indicator with animation to prevent flashing
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        }
        
        // Show content only when cocktail is loaded
        AnimatedVisibility(
            visible = !isLoading && cocktail != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Only proceed if cocktail is not null
            cocktail?.let { cocktailData ->
                val imageUrl = cocktailData.imageUrl ?: ""
                val averageRating = if (reviews.isNotEmpty()) {
                    reviews.map { it.rating }.average().toFloat()
                } else {
                    0f
                }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Cocktail image
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = cocktailData.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Gradient overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.5f)
                                            ),
                                            startY = 150f
                                        )
                                    )
                            )
                            
                            // Cocktail category chip - updated to match top bar color
                            cocktailData.category?.let { category ->
                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .absoluteOffset(x = 0.dp, y = 0.dp)
                                ) {
                                    Surface(
                                        color = AppColors.Primary.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Cocktail details
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-20).dp)
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Name and price
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cocktailData.name,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Text(
                                        text = "$${String.format("%.2f", cocktailData.price)}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Add favorite button and stock status in the same row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Stock status
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val inStock = cocktailData.stockCount > 0
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    if (inStock) Color(0xFF4CAF50) else Color(0xFFE57373),
                                                    CircleShape
                                                )
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = if (inStock) "In Stock (${cocktailData.stockCount} available)" else "Out of Stock",
                                            fontSize = 14.sp,
                                            color = if (inStock) Color(0xFF4CAF50) else Color(0xFFE57373),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    // Favorite button
                                    IconButton(
                                        onClick = { cocktailData?.let { homeViewModel.toggleFavorite(it) } },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                            tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Add to cart button
                                Button(
                                    onClick = { onAddToCart(cocktailData) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Primary,
                                        disabledContainerColor = AppColors.Gray
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = cocktailData.stockCount > 0
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Add to Cart",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Instructions
                                Text(
                                    text = "Instructions",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = cocktailData.instructions ?: "No instructions available.",
                                    fontSize = 15.sp,
                                    color = AppColors.TextPrimary,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                    
                    // Ingredients section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Ingredients",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                cocktailData.ingredients.forEach { ingredient ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(AppColors.Secondary, CircleShape)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Text(
                                            text = "${ingredient.measure} ${ingredient.name}",
                                            fontSize = 15.sp,
                                            color = AppColors.TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Details chips section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Details",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Category chip
                                    cocktailData.category?.let {
                                        item {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("Category: $it") },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
                                                    labelColor = AppColors.TextPrimary
                                                )
                                            )
                                        }
                                    }
                                    
                                    // Glass type chip
                                    cocktailData.glass?.let {
                                        item {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("Glass: $it") },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
                                                    labelColor = AppColors.TextPrimary
                                                )
                                            )
                                        }
                                    }
                                    
                                    // Alcoholic chip
                                    item {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(if (cocktailData.alcoholic == "Alcoholic") "Alcoholic" else "Non-Alcoholic") },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
                                                labelColor = AppColors.TextPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Reviews section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Reviews (${reviews.size})",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )
                                    
                                    TextButton(
                                        onClick = { showReviewDialog = true },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = AppColors.Primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Write a Review")
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (reviews.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Be the first to review this cocktail",
                                            color = AppColors.Gray,
                                            fontSize = 16.sp
                                        )
                                    }
                                } else {
                                    // Use forEachIndexed instead of a for loop for better safety
                                    reviews.take(3).forEachIndexed { index, review ->
                                        ReviewItem(review = review)
                                        
                                        if (index < reviews.take(3).size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                color = AppColors.LightGray.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                    
                                    if (reviews.size > 3) {
                                        TextButton(
                                            onClick = {},
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = AppColors.Primary
                                            )
                                        ) {
                                            Text("View All Reviews (${reviews.size})")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User initial in circle - safely handle potential null/empty username
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AppColors.Primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (review.userName.takeIf { it.isNotBlank() } ?: "A").take(1).uppercase(),
                    color = AppColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = review.userName.takeIf { it.isNotBlank() } ?: "Anonymous",
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBar(
                        rating = review.rating.coerceIn(0f, 5f),
                        modifier = Modifier.padding(end = 8.dp, top = 4.dp),
                        stars = 5,
                        starsColor = AppColors.Secondary
                    )
                    
                    Text(
                        text = review.date.takeIf { it.isNotBlank() } ?: "Unknown date",
                        fontSize = 12.sp,
                        color = AppColors.Gray
                    )
                }
            }
        }
        
        if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                modifier = Modifier.padding(start = 52.dp, top = 8.dp),
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun FavoritesScreen(
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel,
    onBrowseProducts: () -> Unit,
    onAddToCart: (Cocktail) -> Unit
) {
    val favorites by favoritesViewModel.favorites.collectAsState()
    val isLoading by favoritesViewModel.isLoading.collectAsState()
    val error by favoritesViewModel.error.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else if (error?.isNotEmpty() == true) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error ?: "",
                    color = AppColors.Error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (favorites.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = AppColors.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No favorites yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Add cocktails to your favorites to see them here",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onBrowseProducts,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Browse Cocktails")
                    }
                }
            }
        } else {
            // Favorites list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Your Favorite Cocktails",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                itemsIndexed(favorites) { _, cocktail ->
                    CocktailItem(
                        cocktail = cocktail,
                        onClick = { /* Navigate to detail */ },
                        onAddToCart = onAddToCart,
                        homeViewModel = null,
                        isFavorite = true,
                        onToggleFavorite = { cocktail -> 
                            favoritesViewModel.toggleFavorite(cocktail)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    
    // Sample profile data (in a real app, this would come from a ViewModel)
    val userName = remember { "Guest User" }
    val email = remember { "guest@example.com" }
    val memberSince = remember { "January 2023" }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile header with avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(AppColors.Primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = email,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
        
        // Profile info sections
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.Surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Account Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileInfoItem(
                    icon = Icons.Default.Person,
                    title = "Name",
                    value = userName
                )
                
                ProfileInfoItem(
                    icon = Icons.Default.Email,
                    title = "Email",
                    value = email
                )
                
                ProfileInfoItem(
                    icon = Icons.Default.DateRange,
                    title = "Member Since",
                    value = memberSince
                )
            }
        }
        
        // Order history section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.Surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    onClick = { /* Handle notification settings */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy",
                    onClick = { /* Handle privacy settings */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    onClick = { /* Handle help & support */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Logout",
                    onClick = { /* Handle logout */ },
                    textColor = AppColors.Error
                )
            }
        }
        
        // App information
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "My Bar App v1.0.0",
                fontSize = 12.sp,
                color = AppColors.Gray
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )
            
            Text(
                text = value,
                fontSize = 16.sp,
                color = AppColors.TextPrimary
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    textColor: Color = AppColors.TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                fontSize = 16.sp,
                color = textColor
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AppColors.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CocktailItem(
    cocktail: Cocktail,
    onClick: () -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    homeViewModel: HomeViewModel?,
    isFavorite: Boolean = false,
    onToggleFavorite: (Cocktail) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cocktail image with placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.LightGray)
            ) {
                // Use safe call operator for the imageUrl which might be null
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cocktail.imageUrl ?: "")
                        .crossfade(true)
                        .build(),
                    contentDescription = cocktail.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Stock badge for out of stock items
                if (cocktail.stockCount <= 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Out of Stock",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                    
                    // Category chip - updated to match the app's primary color
                    cocktail.category?.let { category ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .absoluteOffset(x = 0.dp, y = 0.dp)
                        ) {
                            Surface(
                                color = AppColors.Primary.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Cocktail details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cocktail.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Using safe operator for alcoholic field that might be null
                Text(
                    text = cocktail.alcoholic ?: "Unknown",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Use safe call for ingredients that might be null or empty
                Text(
                    text = if (cocktail.ingredients.isNotEmpty()) {
                        // Join first 2 ingredients with safe operators
                        cocktail.ingredients.take(2).joinToString(", ") { it.name }
                            .let { if (cocktail.ingredients.size > 2) "$it..." else it }
                    } else {
                        "No ingredients listed"
                    },
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Price
                    Text(
                        text = "$${String.format("%.2f", cocktail.price)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AppColors.Primary
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Favorite button with actual functionality
                    IconButton(
                        onClick = { onToggleFavorite(cocktail) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Add to cart button
                    IconButton(
                        onClick = { onAddToCart(cocktail) },
                        modifier = Modifier.size(32.dp),
                        enabled = cocktail.stockCount > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Add to Cart",
                            tint = if (cocktail.stockCount > 0) AppColors.Primary else AppColors.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
