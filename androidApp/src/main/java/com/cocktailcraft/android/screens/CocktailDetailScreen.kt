package com.cocktailcraft.android.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocktailcraft.android.navigation.NavigationManager
import com.cocktailcraft.android.screens.detail.DetailChipsSection
import com.cocktailcraft.android.screens.detail.DetailIngredientsSection
import com.cocktailcraft.android.screens.detail.DetailMainInfoCard
import com.cocktailcraft.android.screens.detail.DetailRecommendationsSection
import com.cocktailcraft.android.screens.detail.DetailReviewsSection
import com.cocktailcraft.android.ui.components.AppTopBar
import com.cocktailcraft.android.ui.components.DetailHeaderImage
import com.cocktailcraft.android.ui.components.LoadingStateComponent
import com.cocktailcraft.android.ui.components.WriteReviewDialog
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.rememberHapticHandler
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.android.util.toFavoriteIdSet
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.added_to_cart
import com.cocktailcraft.resources.detail_share_cocktail
import com.cocktailcraft.resources.detail_title_fallback
import com.cocktailcraft.resources.favorite_toggle_a11y
import com.cocktailcraft.viewmodel.SharedCartViewModel
import com.cocktailcraft.viewmodel.SharedCocktailDetailViewModel
import com.cocktailcraft.viewmodel.SharedFavoritesViewModel
import com.cocktailcraft.viewmodel.SharedReviewViewModel
import com.cocktailcraft.viewmodel.state.DetailUiState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

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

    // Load reviews for this cocktail
    LaunchedEffect(cocktailId) {
        reviewViewModel.loadReviewsForCocktail(cocktailId)
    }

    // Properly collect reviews as a state
    val reviewState by reviewViewModel.uiState.collectAsStateWithLifecycle()
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val favorites = favoritesState.favorites
    // O(1) membership instead of a scan on every screen recomposition (AN-3)
    val favoriteIds = remember(favorites) { favorites.toFavoriteIdSet() }
    val isFavorite = detailState.cocktail?.let { it.id in favoriteIds } ?: false

    // Check if the cocktail is in cart
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val cartItems = cartState.cartItems
    val isInCart = detailState.cocktail?.let { c -> cartItems.any { it.cocktail.id == c.id } } ?: false

    val coroutineScope = rememberCoroutineScope()

    CocktailDetailContent(
        detailState = detailState,
        reviews = reviewState.currentCocktailReviews,
        isFavorite = isFavorite,
        isInCart = isInCart,
        onBackClick = onBackClick,
        onToggleFavorite = { cocktailData ->
            coroutineScope.launch {
                favoritesViewModel.toggleFavorite(cocktailData)
            }
        },
        // Only call onAddToCart, which will handle adding to cart
        onAddToCart = onAddToCart,
        onRefreshDetails = {
            // Force-refresh from the network and update the UI
            coroutineScope.launch {
                detailViewModel.refreshCocktail()
            }
        },
        onRecommendationClick = { recommendation ->
            navigationManager.navigateToCocktailDetail(recommendation.id)
        },
        onSubmitReview = { userName, rating, comment ->
            coroutineScope.launch {
                reviewViewModel.submitReview(
                    cocktailId = cocktailId,
                    rating = rating,
                    comment = comment,
                    userName = userName
                )
            }
        }
    )
}

/**
 * Stateless detail content: renders the detail/review state and reports
 * every intent upward, so it can be previewed/screenshot-tested without
 * ViewModels. Owns only UI-local state (review dialog, snackbar, haptics).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CocktailDetailContent(
    detailState: DetailUiState,
    reviews: List<Review>,
    isFavorite: Boolean,
    isInCart: Boolean,
    onBackClick: () -> Unit,
    onToggleFavorite: (Cocktail) -> Unit,
    onAddToCart: (Cocktail) -> Unit,
    onRefreshDetails: () -> Unit,
    onRecommendationClick: (Cocktail) -> Unit,
    onSubmitReview: (userName: String, rating: Float, comment: String) -> Unit
) {
    // Single source of truth for the cocktail. The full-screen spinner is
    // driven only by its absence: loadCocktail() publishes the cocktail before
    // its slow, network-bound related-cocktails fetch completes, so gating on
    // detailState.isLoading too would hide already-loaded content for the
    // whole extra fetch (and starve the recommendations shimmer, which needs
    // the content composed while isLoading is still true).
    val cocktail = detailState.cocktail
    val isLoading = cocktail == null

    // Haptic feedback handler
    val hapticHandler = rememberHapticHandler()

    // Add snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showReviewDialog by remember { mutableStateOf(false) }

    WriteReviewDialog(
        showDialog = showReviewDialog,
        onDismiss = { showReviewDialog = false },
        onSubmit = { userName, rating, comment ->
            onSubmitReview(userName, rating, comment)
            showReviewDialog = false
        }
    )

    Scaffold(
        modifier = Modifier.testTag("cocktail_detail_screen"),
        topBar = {
            CocktailDetailTopBar(
                cocktailName = cocktail?.name,
                shareText = detailState.shareableText,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                // Null until the cocktail loads: no actions on the spinner
                onToggleFavorite = cocktail?.let { cocktailData ->
                    {
                        hapticHandler.performToggleFavorite(isFavorite)
                        onToggleFavorite(cocktailData)
                    }
                }
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
                        val addedToCartMessage = stringResource(MR.strings.added_to_cart, cocktailData.name)
                        DetailMainInfoCard(
                            cocktail = cocktailData,
                            isInCart = isInCart,
                            onAddToCartClick = {
                                onAddToCart(cocktailData)
                                // Show snackbar confirmation
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = addedToCartMessage,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onRefreshDetails = onRefreshDetails
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
                            onRecommendationClick = onRecommendationClick
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

// Branded top bar with back navigation and, once the cocktail has loaded,
// Share and Favorite actions in white, matching the iOS detail toolbar
@Composable
private fun CocktailDetailTopBar(
    cocktailName: String?,
    shareText: String,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onToggleFavorite: (() -> Unit)?
) {
    val context = LocalContext.current
    AppTopBar(
        title = cocktailName ?: stringResource(MR.strings.detail_title_fallback),
        showBackButton = true,
        onBackClick = onBackClick,
        actions = {
            if (onToggleFavorite != null) {
                IconButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(send, null))
                }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = stringResource(MR.strings.detail_share_cocktail),
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.semantics { selected = isFavorite }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(MR.strings.favorite_toggle_a11y),
                        tint = Color.White
                    )
                }
            }
        }
    )
}
