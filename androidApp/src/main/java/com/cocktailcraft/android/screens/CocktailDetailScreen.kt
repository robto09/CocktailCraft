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
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.cocktailcraft.android.ui.components.CocktailDetailShimmer
import com.cocktailcraft.android.ui.components.DetailHeaderImage
import com.cocktailcraft.android.ui.components.ErrorStateView
import com.cocktailcraft.android.ui.components.ErrorToastEffect
import com.cocktailcraft.android.ui.components.LocalToastHostState
import com.cocktailcraft.android.ui.components.ToastSnackbarHost
import com.cocktailcraft.android.ui.components.UiEventToasts
import com.cocktailcraft.android.ui.components.WriteReviewSheet
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.android.util.rememberHapticHandler
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.android.util.toFavoriteIdSet
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.domain.model.Review
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.detail_not_found_message
import com.cocktailcraft.resources.detail_not_found_title
import com.cocktailcraft.resources.detail_share_cocktail
import com.cocktailcraft.resources.detail_title_fallback
import com.cocktailcraft.resources.favorite_toggle_a11y
import com.cocktailcraft.util.ErrorHandler
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
    favoritesViewModel: SharedFavoritesViewModel,
    navigationManager: NavigationManager,
    onBackClick: () -> Unit
) {
    // Nav-entry-scoped shared ViewModels — cleared automatically when the
    // destination leaves the back stack
    val detailViewModel = koinViewModel<SharedCocktailDetailViewModel>()
    val reviewViewModel = koinViewModel<SharedReviewViewModel>()
    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val detailError by detailViewModel.error.collectAsStateWithLifecycle()
    // Distinguishes "load not started yet" from "loaded but not found"
    // (same as iOS)
    var hasLoaded by remember(cocktailId) { mutableStateOf(false) }
    LaunchedEffect(cocktailId) {
        // The ViewModel is nav-entry scoped and survives rotation: don't
        // refetch a cocktail it already holds
        if (detailViewModel.uiState.value.cocktail?.id != cocktailId) {
            detailViewModel.loadCocktail(cocktailId)
        }
        hasLoaded = true
    }
    val coroutineScope = rememberCoroutineScope()

    // Own toast host: this screen has no tab chrome (see MainScreen)
    val toastHostState = remember { SnackbarHostState() }

    // Load reviews for this cocktail
    LaunchedEffect(cocktailId) {
        reviewViewModel.loadReviewsForCocktail(cocktailId)
    }

    // Properly collect reviews as a state
    val reviewState by reviewViewModel.uiState.collectAsStateWithLifecycle()
    // Review rejections (comment rules, save failures) shown in the sheet
    val reviewError by reviewViewModel.error.collectAsStateWithLifecycle()
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()
    val favorites = favoritesState.favorites
    // O(1) membership instead of a scan on every screen recomposition (AN-3)
    val favoriteIds = remember(favorites) { favorites.toFavoriteIdSet() }
    val isFavorite = detailState.cocktail?.let { it.id in favoriteIds } ?: false

    CompositionLocalProvider(LocalToastHostState provides toastHostState) {
        // Add-to-cart outcomes and the Undo after unfavoriting
        UiEventToasts(detailViewModel.events)

        // With the cocktail on screen, a failed refresh or favorite toggle is a
        // toast; a failed initial load is the full-screen error state instead
        // (iOS does the same with its alert).
        ErrorToastEffect(
            error = detailError.takeIf { detailState.cocktail != null },
            onConsumed = { detailViewModel.clearError() }
        )

        CocktailDetailContent(
            detailState = detailState,
            reviews = reviewState.currentCocktailReviews,
            isFavorite = isFavorite,
            hasLoaded = hasLoaded,
            error = detailError,
            toastHostState = toastHostState,
            onRetry = {
                coroutineScope.launch { detailViewModel.loadCocktail(cocktailId) }
            },
            onBackClick = onBackClick,
            // Through the detail ViewModel (as on iOS), so a failed toggle is
            // reported on this screen; the heart reads the shared favorites flow
            onToggleFavorite = { detailViewModel.toggleFavorite() },
            // Force-refresh from the network and update the UI
            onRefreshDetails = { detailViewModel.refreshCocktail() },
            // Confirmed (or reported) by a toast from the detail events
            onAddToCart = { detailViewModel.addToCart() },
            onRecommendationClick = { recommendation ->
                navigationManager.navigateToCocktailDetail(recommendation.id)
            },
            reviewError = reviewError?.message?.resolve(),
            onClearReviewError = { reviewViewModel.clearError() },
            onSubmitReview = { userName, rating, comment ->
                reviewViewModel.submitReview(
                    cocktailId = cocktailId,
                    rating = rating,
                    comment = comment,
                    userName = userName
                )
            }
        )
    }
}

/**
 * Stateless detail content: renders the detail/review state and reports
 * every intent upward, so it can be previewed/screenshot-tested without
 * ViewModels. Owns only UI-local state (review sheet, haptics).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CocktailDetailContent(
    detailState: DetailUiState,
    reviews: List<Review>,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onToggleFavorite: (Cocktail) -> Unit,
    onRefreshDetails: () -> Unit,
    onRecommendationClick: (Cocktail) -> Unit,
    reviewError: String?,
    onClearReviewError: () -> Unit,
    // Returns whether the review was saved; the sheet stays open otherwise
    onSubmitReview: suspend (userName: String, rating: Float, comment: String) -> Boolean,
    // False until the first load has finished, so "not found" can't flash
    // before the load starts
    hasLoaded: Boolean = true,
    error: ErrorHandler.UserFriendlyError? = null,
    onRetry: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    toastHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    // Single source of truth for the cocktail; the branches mirror iOS's
    // CocktailDetailView. The cocktail branch wins: loadCocktail() publishes
    // the cocktail before its slow, network-bound related-cocktails fetch
    // completes, so gating on detailState.isLoading too would hide
    // already-loaded content for the whole extra fetch (and starve the
    // recommendations shimmer, which needs the content composed while
    // isLoading is still true). Without a cocktail: the skeleton until the
    // load finishes, then the failed load's error (with Retry), else
    // "not found" — previously a failed or empty load left the skeleton up
    // forever.
    val cocktail = detailState.cocktail
    val isLoading = cocktail == null && (detailState.isLoading || !hasLoaded)
    val loadError = error.takeIf { cocktail == null && !isLoading }
    val isNotFound = cocktail == null && !isLoading && loadError == null

    // Haptic feedback handler
    val hapticHandler = rememberHapticHandler()

    var showReviewDialog by rememberSaveable { mutableStateOf(false) }

    if (showReviewDialog) {
        WriteReviewSheet(
            submissionError = reviewError,
            onDismiss = {
                showReviewDialog = false
                onClearReviewError()
            },
            onClearError = onClearReviewError,
            onSubmit = onSubmitReview
        )
    }

    Scaffold(
        modifier = Modifier.testTag("cocktail_detail_screen"),
        topBar = {
            CocktailDetailTopBar(
                cocktailName = cocktail?.name,
                shareText = detailState.shareableText,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                // Null until the cocktail loads: no actions on the skeleton
                onToggleFavorite = cocktail?.let { cocktailData ->
                    {
                        hapticHandler.performToggleFavorite(isFavorite)
                        onToggleFavorite(cocktailData)
                    }
                }
            )
        },
        snackbarHost = { ToastSnackbarHost(toastHostState) }
    ) { paddingValues ->
        // Skeleton of the detail layout, cross-faded with the content
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CocktailDetailShimmer(modifier = Modifier.padding(paddingValues))
        }

        loadError?.let { failure ->
            ErrorStateView(
                error = failure,
                onRetry = onRetry,
                modifier = Modifier.padding(paddingValues)
            )
        }

        if (isNotFound) {
            ErrorStateView(
                title = stringResource(MR.strings.detail_not_found_title),
                message = stringResource(MR.strings.detail_not_found_message),
                icon = Icons.Filled.LocalBar,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                onRetry = null,
                modifier = Modifier.padding(paddingValues)
            )
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
                        DetailMainInfoCard(
                            cocktail = cocktailData,
                            onRefreshDetails = onRefreshDetails,
                            onAddToCart = {
                                hapticHandler.performClick()
                                onAddToCart()
                            },
                            isAddingToCart = detailState.isAddingToCart
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
                            alcoholic = cocktailData.knownAlcoholic,
                            iba = cocktailData.iba
                        )
                    }

                    // Recommendations come from the shared Detail ViewModel state
                    item {
                        val similarCocktails = detailState.relatedCocktails
                        val isLoadingRecommendations = detailState.isLoading && similarCocktails.isEmpty()

                        DetailRecommendationsSection(
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
