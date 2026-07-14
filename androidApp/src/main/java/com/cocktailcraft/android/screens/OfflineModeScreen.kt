package com.cocktailcraft.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.components.NetworkStatusCard
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
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

// Confirmation dialog shown before clearing the offline cache
@Composable
private fun ClearCacheDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.clear_offline_cache_title)) },
        text = { Text(stringResource(R.string.clear_offline_cache_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.clear_cache))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// Card with the offline mode switch and explanatory text
@Composable
private fun OfflineModeToggleCard(
    isOfflineModeEnabled: Boolean,
    onOfflineModeChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AirplanemodeActive,
                        contentDescription = stringResource(R.string.offline_mode),
                        tint = if (isOfflineModeEnabled) AppColors.Primary else AppColors.Gray,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = stringResource(R.string.offline_mode),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = Spacing.lg)
                    )
                }

                Switch(
                    checked = isOfflineModeEnabled,
                    onCheckedChange = onOfflineModeChange,
                    thumbContent = if (isOfflineModeEnabled) {
                        {
                            Icon(
                                imageVector = Icons.Default.AirplanemodeActive,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = stringResource(R.string.offline_mode_toggle_description),
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

// Card showing the cached cocktail count and the clear cache button
@Composable
private fun CacheInfoCard(
    cachedCocktailCount: Int,
    onClearCacheClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = stringResource(R.string.offline_cache_info),
                    tint = AppColors.Primary,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = stringResource(R.string.offline_cached_cocktails),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = Spacing.lg)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.offline_cocktails_available_label),
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )

                Text(
                    text = "$cachedCocktailCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = onClearCacheClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.clear_cache),
                    modifier = Modifier.padding(end = Spacing.sm)
                )
                Text(stringResource(R.string.clear_cache))
            }
        }
    }
}

// Header row for the recently viewed section
@Composable
private fun RecentlyViewedHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = stringResource(R.string.offline_recently_viewed),
            tint = AppColors.Primary,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = stringResource(R.string.offline_recently_viewed),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = Spacing.lg)
        )
    }
}

// Empty state shown when there are no cached cocktails
@Composable
private fun EmptyCacheMessage(
    onGoToHomeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.xxxl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.offline_no_cocktails),
                tint = AppColors.Gray,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.offline_no_cached_cocktails),
                fontSize = 16.sp,
                color = AppColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = stringResource(R.string.offline_empty_cache_hint),
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = { onGoToHomeClick() },
                modifier = Modifier.padding(top = Spacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(R.string.go_to_home),
                    modifier = Modifier.padding(end = Spacing.sm)
                )
                Text(stringResource(R.string.go_to_home))
            }
        }
    }
}

// ---- Design-time previews ----

@Preview(name = "Offline toggle — on", showBackground = true)
@Composable
private fun OfflineModeToggleCardOnPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        OfflineModeToggleCard(
            isOfflineModeEnabled = true,
            onOfflineModeChange = {}
        )
    }
}

@Preview(name = "Offline toggle — on (dark)", showBackground = true)
@Composable
private fun OfflineModeToggleCardOnDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        OfflineModeToggleCard(
            isOfflineModeEnabled = true,
            onOfflineModeChange = {}
        )
    }
}

@Preview(name = "Offline toggle — off", showBackground = true)
@Composable
private fun OfflineModeToggleCardOffPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        OfflineModeToggleCard(
            isOfflineModeEnabled = false,
            onOfflineModeChange = {}
        )
    }
}

@Preview(name = "Offline toggle — off (dark)", showBackground = true)
@Composable
private fun OfflineModeToggleCardOffDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        OfflineModeToggleCard(
            isOfflineModeEnabled = false,
            onOfflineModeChange = {}
        )
    }
}

@Preview(name = "Cache info", showBackground = true)
@Composable
private fun CacheInfoCardPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        CacheInfoCard(
            cachedCocktailCount = 12,
            onClearCacheClick = {}
        )
    }
}

@Preview(name = "Cache info — large font", showBackground = true, fontScale = 1.5f)
@Composable
private fun CacheInfoCardLargeFontPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        CacheInfoCard(
            cachedCocktailCount = 12,
            onClearCacheClick = {}
        )
    }
}

@Preview(name = "Recently viewed header", showBackground = true)
@Composable
private fun RecentlyViewedHeaderPreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        RecentlyViewedHeader()
    }
}

@Preview(name = "Empty cache", showBackground = true)
@Composable
private fun EmptyCacheMessagePreview() {
    AnimatedCocktailBarTheme(darkTheme = false) {
        EmptyCacheMessage(onGoToHomeClick = {})
    }
}

@Preview(name = "Empty cache (dark)", showBackground = true)
@Composable
private fun EmptyCacheMessageDarkPreview() {
    AnimatedCocktailBarTheme(darkTheme = true) {
        EmptyCacheMessage(onGoToHomeClick = {})
    }
}
