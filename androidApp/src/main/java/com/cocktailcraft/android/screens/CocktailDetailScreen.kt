package com.cocktailcraft.android.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.util.toFavoriteIdSet
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.CocktailIngredient
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.preview.PreviewData
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.ui.components.AppTopBar
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
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import com.cocktailcraft.android.navigation.NavigationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.cocktailcraft.android.ui.components.rememberShimmerTranslate
import com.cocktailcraft.android.ui.components.shimmerEffect
import com.cocktailcraft.android.util.rememberHapticHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailDetailScreen(
    cocktailId: String,
    cartViewModel: SharedCartViewModel,
    favoritesViewModel: SharedFavoritesViewModel,
    navigationManager: NavigationManager,
    onBackClick: () -> Unit,
    onAddToCart: (Cocktail) -> Unit
) {
    // Nav-entry-scoped shared ViewModels — cleared automatically when the
    // destination leaves the back stack
    val detailViewModel = koinViewModel<SharedCocktailDetailViewModel>()
    val reviewViewModel = koinViewModel<SharedReviewViewModel>()
    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(cocktailId) {
        detailViewModel.loadCocktail(cocktailId)
    }

    // Single source of truth for the cocktail. The full-screen spinner is
    // driven only by its absence: loadCocktail() publishes the cocktail before
    // its slow, network-bound related-cocktails fetch completes, so gating on
    // detailState.isLoading too would hide already-loaded content for the
    // whole extra fetch (and starve the recommendations shimmer, which needs
    // the content composed while isLoading is still true).
    val cocktail = detailState.cocktail
    val isLoading = cocktail == null

    // Load reviews for this cocktail
    LaunchedEffect(cocktailId) {
        reviewViewModel.loadReviewsForCocktail(cocktailId)
    }

    // Properly collect reviews as a state
    val reviewState by reviewViewModel.uiState.collectAsStateWithLifecycle()
    val reviews = reviewState.currentCocktailReviews
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val favorites = favoritesState.favorites
    // O(1) membership instead of a scan on every screen recomposition (AN-3)
    val favoriteIds = remember(favorites) { favorites.toFavoriteIdSet() }
    val isFavorite = cocktail?.let { it.id in favoriteIds } ?: false

    // Haptic feedback handler
    val hapticHandler = rememberHapticHandler()

    // Check if the cocktail is in cart
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val cartItems = cartState.cartItems
    val isInCart = cocktail?.let { c -> cartItems.any { it.cocktail.id == c.id } } ?: false

    // Add snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
        modifier = Modifier.testTag("cocktail_detail_screen"),
        topBar = {
            CocktailDetailTopBar(
                cocktailName = cocktail?.name,
                onBackClick = onBackClick
            )
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
                        val addedToCartMessage = stringResource(R.string.added_to_cart, cocktailData.name)
                        DetailMainInfoCard(
                            cocktail = cocktailData,
                            isFavorite = isFavorite,
                            isInCart = isInCart,
                            onToggleFavorite = {
                                hapticHandler.performToggleFavorite(isFavorite)
                                coroutineScope.launch {
                                    favoritesViewModel.toggleFavorite(cocktailData)
                                }
                            },
                            onAddToCartClick = {
                                // Only call onAddToCart, which will handle adding to cart
                                onAddToCart(cocktailData)
                                // Show snackbar confirmation
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = addedToCartMessage,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onRefreshDetails = {
                                // Force-refresh from the network and update the UI
                                coroutineScope.launch {
                                    detailViewModel.refreshCocktail()
                                }
                            }
                        )
                    }

                    // Ingredients section
                    item {
                        DetailIngredientsSection(ingredients = cocktailData.ingredients)
                    }

                    // Details chips section
                    item {
                        DetailChipsSection(
                            category = cocktailData.category,
                            glass = cocktailData.glass,
                            alcoholic = cocktailData.alcoholic
                        )
                    }

                    // Recommendations come from the shared Detail ViewModel state
                    item {
                        val similarCocktails = detailState.relatedCocktails
                        val isLoadingRecommendations = detailState.isLoading && similarCocktails.isEmpty()

                        DetailRecommendationsSection(
                            category = cocktailData.category,
                            similarCocktails = similarCocktails,
                            isLoadingRecommendations = isLoadingRecommendations,
                            onRecommendationClick = { recommendation ->
                                navigationManager.navigateToCocktailDetail(recommendation.id)
                            }
                        )
                    }
                    
                    // Reviews section
                    item {
                        DetailReviewsSection(
                            reviews = reviews,
                            onWriteReviewClick = { showReviewDialog = true }
                        )
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(Spacing.xxl))
                    }
                }
            }
        }
    }
}

// Branded top bar with back navigation (shared AppTopBar chrome)
@Composable
private fun CocktailDetailTopBar(
    cocktailName: String?,
    onBackClick: () -> Unit
) {
    AppTopBar(
        title = cocktailName ?: stringResource(R.string.detail_title_fallback),
        showBackButton = true,
        onBackClick = onBackClick
    )
}

// Main info card: price and favorite row, subtitle, stock status,
// add-to-cart button and preparation instructions
@Composable
private fun DetailMainInfoCard(
    cocktail: Cocktail,
    isFavorite: Boolean,
    isInCart: Boolean,
    onToggleFavorite: () -> Unit,
    onAddToCartClick: () -> Unit,
    onRefreshDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-20).dp)
            .padding(horizontal = Spacing.lg),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl)
        ) {
            // Price and favorite row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detail_price_format, cocktail.price),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )

                // Favorite button with haptic feedback —
                // default size keeps the 48 dp touch target
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) stringResource(R.string.detail_remove_from_favorites) else stringResource(R.string.detail_add_to_favorites),
                        tint = if (isFavorite) AppColors.Secondary else AppColors.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Category and alcoholic info subtitle
            val unknownAlcoholic = stringResource(R.string.detail_alcoholic_unknown)
            Text(
                text = buildString {
                    append(cocktail.alcoholic ?: unknownAlcoholic)
                    cocktail.category?.let {
                        append(" • ")
                        append(it)
                    }
                },
                fontSize = 16.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Stock status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val inStock = cocktail.stockCount > 0
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (inStock) AppColors.Success else AppColors.Error,
                            CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                Text(
                    text = if (inStock) stringResource(R.string.detail_in_stock, cocktail.stockCount) else stringResource(R.string.detail_out_of_stock),
                    fontSize = 14.sp,
                    color = if (inStock) AppColors.Success else AppColors.Error,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Add to cart button
            Button(
                onClick = onAddToCartClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    disabledContainerColor = AppColors.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = cocktail.stockCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(Spacing.sm))

                Text(
                    text = if (isInCart) stringResource(R.string.detail_update_cart) else stringResource(R.string.add_to_cart),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Instructions
            DetailInfoCard(
                title = stringResource(R.string.how_to_prepare),
                modifier = Modifier.padding(vertical = Spacing.sm),
                content = {
                    // Add debug print to console to verify instructions are available
                    val instructionsText = cocktail.instructions ?: ""

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
                                text = stringResource(R.string.detail_no_instructions),
                                fontSize = 15.sp,
                                color = AppColors.TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            OutlinedButton(
                                onClick = onRefreshDetails,
                                border = BorderStroke(1.dp, AppColors.Primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.detail_refresh_details),
                                    color = AppColors.Primary
                                )
                            }
                        }
                    }
                },
                backgroundColor = AppColors.Surface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

// Ingredients list rendered inside an info card
@Composable
private fun DetailIngredientsSection(ingredients: List<CocktailIngredient>) {
    DetailInfoCard(
        title = stringResource(R.string.ingredients),
        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        content = {

            // Handle different types of ingredients list
            (ingredients as? List<CocktailIngredient>)?.forEach { ingredient ->
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

                    Spacer(modifier = Modifier.width(Spacing.md))

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

// Category, glass and alcoholic detail chips
@Composable
private fun DetailChipsSection(
    category: String?,
    glass: String?,
    alcoholic: String?
) {
    DetailInfoCard(
        title = stringResource(R.string.details),
        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        content = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                // Category chip
                category?.let {
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
                glass?.let {
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
                        label = { Text(if (alcoholic == "Alcoholic") stringResource(R.string.alcoholic) else stringResource(R.string.non_alcoholic)) },
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

// Recommendations card with shimmer loading, empty and loaded states
@Composable
private fun DetailRecommendationsSection(
    category: String?,
    similarCocktails: List<Cocktail>,
    isLoadingRecommendations: Boolean,
    onRecommendationClick: (Cocktail) -> Unit
) {
    // Always show the recommendations section - we'll have fallback data if needed
    // Add a spacer before recommendations
    Spacer(modifier = Modifier.height(Spacing.lg))

    // Display recommendations with a Card wrapper for consistent styling
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl)
        ) {
            // Section title with category if available
            val titleText = if (category != null) {
                stringResource(R.string.detail_more_category_cocktails, category)
            } else {
                stringResource(R.string.detail_you_might_also_like)
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = AppColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Show loading state or recommendations
            if (isLoadingRecommendations) {
                RecommendationsLoadingRow()
            } else if (similarCocktails.isEmpty()) {
                // Show a message if no recommendations
                Text(
                    text = stringResource(R.string.detail_loading_recommendations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(vertical = Spacing.lg)
                )
            } else {
                // Show recommendations
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(similarCocktails) { recommendation ->
                        // Simple recommendation card
                        RecommendationCard(
                            recommendation = recommendation,
                            onClick = { onRecommendationClick(recommendation) }
                        )
                    }
                }
            }
        }
    }
}

// Shimmer placeholder row shown while recommendations load
@Composable
private fun RecommendationsLoadingRow() {
    // One transition drives all three placeholders
    val shimmerTranslate = rememberShimmerTranslate()
    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        items(3) {
            // Loading shimmer
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerEffect(shimmerTranslate)
            )
        }
    }
}

// Small card for a single recommended cocktail
@Composable
private fun RecommendationCard(
    recommendation: Cocktail,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
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
                modifier = Modifier.padding(Spacing.sm)
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

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Price
                Text(
                    text = stringResource(R.string.detail_price_format, recommendation.price),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.Primary
                )
            }
        }
    }
}

// Reviews header with write-review action and the first few reviews
@Composable
private fun DetailReviewsSection(
    reviews: List<Review>,
    onWriteReviewClick: () -> Unit
) {
    DetailInfoCard(
        title = "",
        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
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
                    onClick = onWriteReviewClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(stringResource(R.string.write_review))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            if (reviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xxl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.detail_be_first_review),
                        color = AppColors.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                reviews.take(3).forEachIndexed { index, review ->
                    CocktailReviewItem(review = review)

                    if (index < reviews.take(3).size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Spacing.md),
                            color = AppColors.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        elevation = 0
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
                    text = (review.userName.takeIf { it.isNotBlank() } ?: stringResource(R.string.detail_review_anonymous_initial)).take(1).uppercase(),
                    color = AppColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column {
                Text(
                    text = review.userName.takeIf { it.isNotBlank() } ?: stringResource(R.string.detail_review_anonymous),
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    fontSize = 16.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingBar(
                        rating = review.rating.coerceIn(0f, 5f),
                        modifier = Modifier.padding(end = Spacing.sm, top = Spacing.xs),
                        stars = 5,
                        starsColor = AppColors.Secondary
                    )

                    Text(
                        text = review.date.takeIf { it.isNotBlank() } ?: stringResource(R.string.detail_unknown_date),
                        fontSize = 12.sp,
                        color = AppColors.Gray
                    )
                }
            }
        }

        if (review.comment.isNotBlank()) {
            Text(
                text = review.comment,
                modifier = Modifier.padding(start = 52.dp, top = Spacing.sm),
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// ---- Design-time previews ----

@Preview(name = "Main info card", showBackground = true)
@Composable
private fun DetailMainInfoCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailMainInfoCard(
            cocktail = PreviewData.cocktail,
            isFavorite = true,
            isInCart = false,
            onToggleFavorite = {},
            onAddToCartClick = {},
            onRefreshDetails = {}
        )
    }
}

@Preview(name = "Main info card (dark)", showBackground = true)
@Composable
private fun DetailMainInfoCardDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        DetailMainInfoCard(
            cocktail = PreviewData.cocktail,
            isFavorite = true,
            isInCart = false,
            onToggleFavorite = {},
            onAddToCartClick = {},
            onRefreshDetails = {}
        )
    }
}

@Preview(name = "Main info card (large font)", showBackground = true, fontScale = 1.5f)
@Composable
private fun DetailMainInfoCardLargeFontPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailMainInfoCard(
            cocktail = PreviewData.cocktail,
            isFavorite = true,
            isInCart = false,
            onToggleFavorite = {},
            onAddToCartClick = {},
            onRefreshDetails = {}
        )
    }
}

@Preview(name = "Ingredients section", showBackground = true)
@Composable
private fun DetailIngredientsSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailIngredientsSection(ingredients = PreviewData.cocktail.ingredients)
    }
}

@Preview(name = "Details chips section", showBackground = true)
@Composable
private fun DetailChipsSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailChipsSection(
            category = PreviewData.cocktail.category,
            glass = PreviewData.cocktail.glass,
            alcoholic = PreviewData.cocktail.alcoholic
        )
    }
}

@Preview(name = "Reviews section", showBackground = true)
@Composable
private fun DetailReviewsSectionPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        DetailReviewsSection(
            reviews = PreviewData.reviews,
            onWriteReviewClick = {}
        )
    }
}

@Preview(name = "Reviews section (dark)", showBackground = true)
@Composable
private fun DetailReviewsSectionDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        DetailReviewsSection(
            reviews = PreviewData.reviews,
            onWriteReviewClick = {}
        )
    }
}

@Preview(name = "Recommendation card", showBackground = true)
@Composable
private fun RecommendationCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        RecommendationCard(
            recommendation = PreviewData.cocktail,
            onClick = {}
        )
    }
}