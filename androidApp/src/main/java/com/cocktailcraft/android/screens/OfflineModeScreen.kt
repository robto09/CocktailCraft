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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.components.CocktailItem
import com.cocktailcraft.android.ui.theme.AppColors
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
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text(stringResource(R.string.clear_offline_cache_title)) },
            text = { Text(stringResource(R.string.clear_offline_cache_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { viewModel.clearCache() }
                        showClearCacheDialog = false
                    }
                ) {
                    Text(stringResource(R.string.clear_cache))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCacheDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
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
            // Network status card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isNetworkAvailable) Color(0xFF4CAF50) else Color(0xFFE57373)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (state.isNetworkAvailable)
                            Icons.Default.Wifi
                        else
                            Icons.Default.WifiOff,
                        contentDescription = if (state.isNetworkAvailable) "Online" else "Offline",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (state.isNetworkAvailable) "Network Available" else "Network Unavailable",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 80.dp  // Add extra padding at the bottom to avoid system navigation bar
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Offline mode toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.Surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
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
                                        contentDescription = "Offline Mode",
                                        tint = if (state.isOfflineModeEnabled) AppColors.Primary else AppColors.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Text(
                                        text = "Offline Mode",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }

                                Switch(
                                    checked = state.isOfflineModeEnabled,
                                    onCheckedChange = { scope.launch { viewModel.setOfflineMode(it) } },
                                    thumbContent = if (state.isOfflineModeEnabled) {
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

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "When enabled, the app will only use cached data and won't make network requests.",
                                fontSize = 14.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }

                // Cache info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.Surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Cache Info",
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Text(
                                    text = "Cached Cocktails",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Cocktails available offline:",
                                    fontSize = 14.sp,
                                    color = AppColors.TextSecondary
                                )

                                Text(
                                    text = "${state.recentlyViewedCocktails.size}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showClearCacheDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE57373)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Cache",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(stringResource(R.string.clear_cache))
                            }
                        }
                    }
                }

                // Recently viewed section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Recently Viewed",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = "Recently Viewed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                // Recently viewed cocktails
                if (state.recentlyViewedCocktails.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No cocktails",
                                    tint = AppColors.Gray,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No cached cocktails",
                                    fontSize = 16.sp,
                                    color = AppColors.TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Browse cocktails in the Home screen to cache them for offline viewing",
                                    fontSize = 14.sp,
                                    color = AppColors.TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { onBackClick() },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Go to Home",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(stringResource(R.string.go_to_home))
                                }
                            }
                        }
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
