package com.cocktailcraft.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cocktailcraft.navigation.Screen
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.ui.components.CocktailItem
import com.cocktailcraft.ui.components.FilterChip
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.viewmodel.CartViewModel
import com.cocktailcraft.viewmodel.FavoritesViewModel
import com.cocktailcraft.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel,
    onAddToCart: (Cocktail) -> Unit,
    onCocktailClick: (Cocktail) -> Unit
) {
    val cocktails by viewModel.cocktails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by favoritesViewModel.favorites.collectAsState()
    
    // Add state for selected category
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    // Add pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.retry() }
    )
    
    // Effect to load cocktails by category when selected category changes
    LaunchedEffect(selectedCategory) {
        if (!isSearchActive) {
            viewModel.loadCocktailsByCategory(selectedCategory)
        }
    }
    
    // Define common cocktail categories
    val categories = listOf(
        "All", "Cocktail", "Ordinary Drink", "Shot", "Coffee / Tea", 
        "Punch / Party Drink", "Homemade Liqueur", "Beer", "Soft Drink"
    )
    
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
        
        // Add Category Filter Chips - only shown when not searching
        if (!isSearchActive) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category || (category == "All" && selectedCategory == null),
                        onClick = { 
                            selectedCategory = if (category == "All") null else category
                        },
                        label = category
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Main content wrapped in pull-to-refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            if (isLoading && cocktails.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else if (error.isNotBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Unable to load cocktails",
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show error message or a fallback
                        Text(
                            text = error,
                            color = AppColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else if (cocktails.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSearchActive) 
                            "No cocktails found matching \"$searchQuery\"" 
                        else if (selectedCategory != null) 
                            "No cocktails found in category \"$selectedCategory\""
                        else 
                            "No cocktails found",
                        color = AppColors.TextPrimary,
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
                    // Only show featured section when not searching and not filtering
                    if (!isSearchActive && selectedCategory == null) {
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
                                    onClick = { 
                                        navController.navigate(Screen.CocktailDetail.createRoute(featuredCocktail.id))
                                        onCocktailClick(featuredCocktail) 
                                    },
                                    onAddToCart = { cocktail -> 
                                        cartViewModel.addToCart(cocktail)
                                        onAddToCart(cocktail)
                                    },
                                    isFavorite = favorites.any { it.id == featuredCocktail.id },
                                    onToggleFavorite = { cocktail -> 
                                        favoritesViewModel.toggleFavorite(cocktail)
                                    }
                                )
                            }
                        }
                    }
                    
                    // All cocktails list or search results or filtered by category
                    item {
                        Text(
                            text = when {
                                isSearchActive -> "Search Results"
                                selectedCategory != null -> "$selectedCategory Cocktails"
                                else -> "All Cocktails"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    itemsIndexed(cocktails) { _, cocktail ->
                        CocktailItem(
                            cocktail = cocktail,
                            onClick = { 
                                navController.navigate(Screen.CocktailDetail.createRoute(cocktail.id))
                                onCocktailClick(cocktail) 
                            },
                            onAddToCart = { 
                                cartViewModel.addToCart(it)
                                onAddToCart(it)
                            },
                            isFavorite = favorites.any { it.id == cocktail.id },
                            onToggleFavorite = { cocktailToToggle -> 
                                favoritesViewModel.toggleFavorite(cocktailToToggle)
                            }
                        )
                    }
                }
            }
            
            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = AppColors.Primary
            )
        }
    }
} 