package com.cocktailcraft.android.screens.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing

// Card with the offline mode switch and explanatory text
@Composable
internal fun OfflineModeToggleCard(
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
internal fun CacheInfoCard(
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
                    containerColor = AppColors.Error
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
internal fun RecentlyViewedHeader() {
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
internal fun EmptyCacheMessage(
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
