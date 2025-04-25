package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A banner that indicates when the app is in offline mode.
 * 
 * @param isOffline Whether the app is currently offline
 * @param isOfflineModeEnabled Whether offline mode is enabled by user preference
 * @param onClick Optional callback when the banner is clicked
 */
@Composable
fun OfflineModeIndicator(
    isOffline: Boolean,
    isOfflineModeEnabled: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isOfflineModeEnabled) AppColors.Primary.copy(alpha = 0.8f)
                    else Color(0xFFE57373) // Light red for network offline
                )
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isOfflineModeEnabled) Icons.Default.CloudOff else Icons.Default.WifiOff,
                    contentDescription = "Offline",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                Text(
                    text = if (isOfflineModeEnabled) "Offline Mode Enabled" else "You are offline",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
