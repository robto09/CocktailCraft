package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.app.Activity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.android.screens.offline.CacheInfoCard
import com.cocktailcraft.android.screens.offline.ClearCacheDialog
import com.cocktailcraft.android.screens.offline.EmptyCacheMessage
import com.cocktailcraft.android.screens.offline.OfflineModeToggleCard
import com.cocktailcraft.android.screens.offline.RecentlyViewedHeader
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.NetworkStatusCard
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.viewmodel.SharedOfflineModeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineModeScreen(
    viewModel: SharedOfflineModeViewModel,
    onBackClick: () -> Unit,
    onCocktailClick: (Cocktail) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Hide the system navigation bar (immersive) while this screen is shown
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    // Restore the system navigation bar when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    var showClearCacheDialog by remember { mutableStateOf(false) }

    if (showClearCacheDialog) {
        ClearCacheDialog(
            onConfirm = {
                scope.launch { viewModel.clearCache() }
                showClearCacheDialog = false
            },
            onDismiss = { showClearCacheDialog = false }
        )
    }

    Scaffold(
        // Remove the topBar completely as it's already handled in MainScreen.kt
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.Background)
        ) {
            // Network status card (shared component; defaults match the old
            // inline block exactly)
            NetworkStatusCard(isNetworkAvailable = state.isNetworkAvailable)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.lg,
                    bottom = 80.dp  // Add extra padding at the bottom to avoid system navigation bar
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Offline mode toggle
                item {
                    OfflineModeToggleCard(
                        isOfflineModeEnabled = state.isOfflineModeEnabled,
                        onOfflineModeChange = { scope.launch { viewModel.setOfflineMode(it) } }
                    )
                }

                // Cache info
                item {
                    CacheInfoCard(
                        cachedCocktailCount = state.recentlyViewedCocktails.size,
                        onClearCacheClick = { showClearCacheDialog = true }
                    )
                }

                // Recently viewed section
                item {
                    RecentlyViewedHeader()
                }

                // Recently viewed cocktails
                if (state.recentlyViewedCocktails.isEmpty()) {
                    item {
                        EmptyCacheMessage(onGoToHomeClick = onBackClick)
                    }
                } else {
                    items(state.recentlyViewedCocktails) { cocktail ->
                        CocktailItem(
                            cocktail = cocktail,
                            onClick = { onCocktailClick(cocktail) },
                            onAddToCart = { /* Not needed here */ },
                            isFavorite = false, // We don't have this info here
                            onToggleFavorite = { /* Not needed here */ }
                        )
                    }
                }
            }
        }
    }
}
