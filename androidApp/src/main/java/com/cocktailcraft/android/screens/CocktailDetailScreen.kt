package com.cocktailcraft.android.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.components.DetailHeaderImage
import com.cocktailcraft.android.ui.components.DetailInfoCard
import com.cocktailcraft.android.ui.components.LoadingStateComponent
import com.cocktailcraft.android.ui.components.OptimizedImage
import com.cocktailcraft.android.ui.components.RatingBar
import com.cocktailcraft.android.ui.components.RatingDisplay
import com.cocktailcraft.android.ui.components.SectionHeader
import com.cocktailcraft.android.ui.components.WriteReviewDialog
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedHomeViewModel
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import com.cocktailcraft.android.navigation.NavigationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import androidx.compose.animation.core.*
import com.cocktailcraft.android.ui.components.shimmerEffect
import com.cocktailcraft.android.util.rememberHapticHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailDetailScreen(
    cocktailId: String,
    homeViewModel: SharedHomeViewModel,
    cartViewModel: SharedCartViewModel,
    favoritesViewModel: SharedFavoritesViewModel,
    navigationManager: NavigationManager,
    onBackClick: () -> Unit,
    onAddToCart: (Cocktail) -> Unit
) {
    // Add a loading state to track when data is being fetched
    var isLoading by remember { mutableStateOf(true) }

    // Nav-entry-scoped shared ViewModels — cleared automatically when the
    // destination leaves the back stack
    val detailViewModel = koinViewModel<SharedCocktailDetailViewModel>()
    val reviewViewModel = koinViewModel<SharedReviewViewModel>()
    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(cocktailId) {
        detailViewModel.loadCocktail(cocktailId)
    }

    // Load the cocktail via the suspend getCocktailById
    val cocktail by produceState<Cocktail?>(initialValue = null, cocktailId) {
        value = homeViewModel.getCocktailById(cocktailId)
    }

    // Load reviews for this cocktail
    LaunchedEffect(cocktailId) {
        reviewViewModel.loadReviewsForCocktail(cocktailId)
    }

    // Properly collect reviews as a state
    val reviewState by reviewViewModel.uiState.collectAsStateWithLifecycle()
    val reviews = reviewState.currentCocktailReviews
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val favorites = favoritesState.favorites
    val isFavorite = cocktail?.let { c -> favorites.any { fav -> fav.id == c.id } } ?: false

    // Haptic feedback handler
    val hapticHandler = rememberHapticHandler()

    // Check if the cocktail is in cart
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val cartItems = cartState.cartItems
    val isInCart = cocktail?.let { c -> cartItems.any { it.cocktail.id == c.id } } ?: false

    // Add snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Update loading state when cocktail data changes
    LaunchedEffect(cocktail) {
        if (cocktail != null) {
            isLoading = false
        }
    }

    var showReviewDialog by remember { mutableStateOf(false) }

    WriteReviewDialog(
        showDialog = showReviewDialog,
        onDismiss = { showReviewDialog = false },
        onSubmit = { userName, rating, comment ->
            try {
                coroutineScope.launch {
                    reviewViewModel.submitReview(
                        cocktailId = cocktailId,
                        rating = rating,
                        comment = comment,
                        userName = userName
                    )
                }
                showReviewDialog = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = cocktail?.name ?: "Cocktail Detail",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = AppColors.Primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )

                // Add a divider to create separation between top bar and content
                Divider(
                    color = Color.White.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Show loading indicator with animation to prevent flashing
        LoadingStateComponent(
            isLoading = isLoading,
            paddingValues = paddingValues
        )

        // Show content only when cocktail is loaded
        AnimatedVisibility(
            visible = !isLoading && cocktail != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Only proceed if cocktail is not null
            cocktail?.let { cocktailData ->
                val imageUrl = cocktailData.imageUrl ?: ""

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Cocktail image
                    item {
                        DetailHeaderImage(
                            imageUrl = imageUrl,
                            contentDescription = cocktailData.name,
                            height = 250,
                            targetSize = 800 // Higher resolution for detail view
                        )
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
                                // Price and favorite row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$${String.format("%.2f", cocktailData.price)}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary
                                    )

                                    // Favorite button with haptic feedback
                                    IconButton(
                                        onClick = {
                                            hapticHandler.performToggleFavorite(isFavorite)
                                            coroutineScope.launch {
                                                favoritesViewModel.toggleFavorite(cocktailData)
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                            tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Category and alcoholic info subtitle
                                Text(
                                    text = buildString {
                                        append(cocktailData.alcoholic ?: "Unknown")
                                        cocktailData.category?.let {
                                            append(" • ")
                                            append(it)
                                        }
                                    },
                                    fontSize = 16.sp,
                                    color = AppColors.TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

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

                                Spacer(modifier = Modifier.height(24.dp))

                                // Add to cart button
                                val addedToCartMessage = stringResource(R.string.added_to_cart, cocktailData.name)
                                Button(
                                    onClick = {
                                        cocktailData.let {
                                            // Only call onAddToCart, which will handle adding to cart
                                            onAddToCart(it)
                                            // Show snackbar confirmation
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = addedToCartMessage,
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
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
                                        text = if (isInCart) "Update Cart" else "Add to Cart",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Instructions
                                DetailInfoCard(
                                    title = stringResource(R.string.how_to_prepare),
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    content = {
                                    // Add debug print to console to verify instructions are available
                                    val instructionsText = cocktailData.instructions ?: ""

                                    if (instructionsText.isNotBlank()) {
                                        Text(
                                            text = instructionsText,
                                            fontSize = 15.sp,
                                            color = AppColors.TextPrimary,
                                            lineHeight = 24.sp
                                        )
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "No instructions available for this cocktail.",
                                                fontSize = 15.sp,
                                                color = AppColors.TextSecondary,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedButton(
                                                onClick = {
                                                    // Attempt to reload the data
                                                    coroutineScope.launch {
                                                        homeViewModel.refreshCocktailDetails(cocktailId)
                                                    }
                                                },
                                                border = BorderStroke(1.dp, AppColors.Primary),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Refresh Details",
                                                    color = AppColors.Primary
                                                )
                                            }
                                        }
                                    }
                                },
                                    backgroundColor = AppColors.Surface.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Ingredients section
                    item {
                        DetailInfoCard(
                            title = stringResource(R.string.ingredients),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            content = {

                                // Handle different types of ingredients list
                                    (cocktailData.ingredients as? List<CocktailIngredient>)?.forEach { ingredient ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Ingredient bullet point
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(AppColors.Primary, CircleShape)
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            // Ingredient name and measure
                                            Text(
                                                text = "${ingredient.name} ${ingredient.measure}",
                                                fontSize = 16.sp,
                                                color = AppColors.TextSecondary
                                            )
                                        }
                                        }

                            },
                            elevation = 0
                        )
                    }

                    // Details chips section
                    item {
                        DetailInfoCard(
                            title = stringResource(R.string.details),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            content = {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                    // Category chip
                                    cocktailData.category?.let {
                                        item {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(stringResource(R.string.category_label, it)) },
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
                                                label = { Text(stringResource(R.string.glass_label, it)) },
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
                                            label = { Text(if (cocktailData.alcoholic == "Alcoholic") stringResource(R.string.alcoholic) else stringResource(R.string.non_alcoholic)) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = AppColors.ChipBackground.copy(alpha = 0.2f),
                                                labelColor = AppColors.TextPrimary
                                            )
                                        )
                                    }
                                }
                            },
                            elevation = 0
                        )
                    }

                    // Recommendations come from the shared Detail ViewModel state
                    item {
                        val similarCocktails = detailState.relatedCocktails
                        val isLoadingRecommendations = detailState.isLoading && similarCocktails.isEmpty()

                        // Always show the recommendations section - we'll have fallback data if needed
                        // Add a spacer before recommendations
                        Spacer(modifier = Modifier.height(16.dp))

                        // Display recommendations with a Card wrapper for consistent styling
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
                                    // Section title with category if available
                                    val titleText = if (cocktailData.category != null) {
                                        "More ${cocktailData.category} Cocktails"
                                    } else {
                                        "You might also like"
                                    }

                                    Text(
                                        text = titleText,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = AppColors.TextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Show loading state or recommendations
                                    if (isLoadingRecommendations) {
                                        // Show loading shimmer
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 0.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(3) {
                                                // Loading shimmer
                                                Box(
                                                    modifier = Modifier
                                                        .width(140.dp)
                                                        .height(200.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .shimmerEffect()
                                                )
                                            }
                                        }
                                    } else if (similarCocktails.isEmpty()) {
                                        // Show a message if no recommendations
                                        Text(
                                            text = "Loading recommendations...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AppColors.TextSecondary,
                                            modifier = Modifier.padding(vertical = 16.dp)
                                        )
                                    } else {
                                        // Show recommendations
                                        LazyRow(
                                            contentPadding = PaddingValues(horizontal = 0.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(similarCocktails) { recommendation ->
                                                // Simple recommendation card
                                                Card(
                                                modifier = Modifier
                                                    .width(140.dp)
                                                    .clickable {
                                                        navigationManager.navigateToCocktailDetail(recommendation.id)
                                                    },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = AppColors.Surface
                                                ),
                                                elevation = CardDefaults.cardElevation(
                                                    defaultElevation = 2.dp
                                                )
                                            ) {
                                                Column {
                                                    // Cocktail image with fallback
                                                    Box(
                                                        modifier = Modifier
                                                            .height(140.dp)
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                                            .background(AppColors.LightGray),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        // Show a placeholder icon if image URL is empty
                                                        if (recommendation.imageUrl.isNullOrEmpty()) {
                                                            Icon(
                                                                imageVector = Icons.Default.LocalBar,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(48.dp),
                                                                tint = AppColors.Gray
                                                            )
                                                        } else {
                                                            // Use our optimized image component
                                                            OptimizedImage(
                                                                url = recommendation.imageUrl,
                                                                contentDescription = recommendation.name,
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentScale = ContentScale.Crop,
                                                                targetSize = 200,
                                                                showLoadingIndicator = true
                                                            )
                                                        }
                                                    }

                                                    // Cocktail details
                                                    Column(
                                                        modifier = Modifier.padding(8.dp)
                                                    ) {
                                                        // Cocktail name
                                                        Text(
                                                            text = recommendation.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = AppColors.TextPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        // Price
                                                        Text(
                                                            text = "$${String.format("%.2f", recommendation.price)}",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = AppColors.Primary
                                                        )
                                                    }
                                                }
                                            }
                                            }
                                        }
                                    }
                                }
                            }
                    }
                    
                    // Reviews section
                    item {
                        DetailInfoCard(
                            title = "",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            content = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SectionHeader(
                                        title = stringResource(R.string.reviews_title, reviews.size),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 18
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
                                        Text(stringResource(R.string.write_review))
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
                                    reviews.take(3).forEachIndexed { index, review ->
                                        CocktailReviewItem(review = review)

                                        if (index < reviews.take(3).size - 1) {
                                            Divider(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                color = AppColors.LightGray.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            },
                            elevation = 0
                        )
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

/**
 * Extension function to create a shimmer loading effect
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha = transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer alpha"
    ).value

    return this.then(
        Modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.LightGray.copy(alpha = alpha),
                    Color.LightGray.copy(alpha = 0.3f),
                    Color.LightGray.copy(alpha = alpha)
                )
            )
        )
    )
}

@Composable
fun CocktailReviewItem(review: Review) {
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