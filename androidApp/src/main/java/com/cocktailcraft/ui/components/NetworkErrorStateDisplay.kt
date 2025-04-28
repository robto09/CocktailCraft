package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable component for displaying network and offline error states.
 *
 * @param errorMessage The error message to display
 * @param isOfflineMode Whether the app is in offline mode
 * @param isNetworkAvailable Whether the network is available
 * @param hasContent Whether there is cached content available
 * @param onRetry Callback when the retry button is clicked
 * @param onEnableOfflineMode Callback when the enable offline mode button is clicked
 * @param onGoOnline Callback when the go online button is clicked
 * @param modifier The modifier for the component
 * @param iconSize The size of the error icon
 * @param titleFontSize The font size of the title
 * @param messageFontSize The font size of the message
 * @param primaryButtonColor The color of the primary button
 * @param secondaryButtonColor The color of the secondary button
 */
@Composable
fun NetworkErrorStateDisplay(
    errorMessage: String,
    isOfflineMode: Boolean,
    isNetworkAvailable: Boolean,
    hasContent: Boolean,
    onRetry: () -> Unit,
    onEnableOfflineMode: () -> Unit,
    onGoOnline: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 48,
    titleFontSize: Int = 18,
    messageFontSize: Int = 14,
    primaryButtonColor: Color = AppColors.Primary,
    secondaryButtonColor: Color = AppColors.Secondary
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // Error icon based on state
            val icon: ImageVector
            val iconTint: Color
            val title: String
            val message: String

            if (!isNetworkAvailable || isOfflineMode) {
                icon = Icons.Default.WifiOff
                iconTint = AppColors.Primary
                title = when {
                    isOfflineMode -> "Offline Mode Active"
                    else -> "Network Unavailable"
                }
                message = when {
                    isOfflineMode && !hasContent ->
                        "No cached cocktails available. Connect to the internet to download cocktails."
                    !isNetworkAvailable ->
                        "You're currently offline. Enable Offline Mode to browse cached cocktails."
                    else -> errorMessage
                }
            } else {
                icon = Icons.Default.Error
                iconTint = Color.Red
                title = "Unable to load cocktails"
                message = errorMessage
            }

            Icon(
                imageVector = icon,
                contentDescription = "Error",
                tint = iconTint,
                modifier = Modifier.size(iconSize.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = iconTint,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = titleFontSize.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                fontSize = messageFontSize.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary action button (always shown)
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryButtonColor
                    )
                ) {
                    Text("Retry")
                }

                // Conditional secondary action button
                when {
                    // If network is unavailable and offline mode is not enabled
                    !isNetworkAvailable && !isOfflineMode -> {
                        Button(
                            onClick = onEnableOfflineMode,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = secondaryButtonColor
                            )
                        ) {
                            Text("Enable Offline Mode")
                        }
                    }
                    // If offline mode is enabled but we want to go back online
                    isOfflineMode && isNetworkAvailable -> {
                        Button(
                            onClick = onGoOnline,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = secondaryButtonColor
                            )
                        ) {
                            Text("Go Online")
                        }
                    }
                }
            }
        }
    }
}
