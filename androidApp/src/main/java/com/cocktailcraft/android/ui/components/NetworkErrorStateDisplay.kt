package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_network_unavailable
import com.cocktailcraft.resources.common_retry
import com.cocktailcraft.resources.error_no_cached_cocktails
import com.cocktailcraft.resources.error_unable_to_load_cocktails

/**
 * A full-screen error state for load failures, shown only when there is no
 * content to render. With no connection it shows the no-internet state with
 * a Retry action; auto-retry reloads on its own once the network returns.
 *
 * @param errorMessage The error message to display
 * @param isNetworkAvailable Whether the network is available
 * @param onRetry Callback when the retry button is clicked
 * @param modifier The modifier for the component
 * @param iconSize The size of the error icon
 * @param titleFontSize The font size of the title
 * @param messageFontSize The font size of the message
 * @param retryButtonColor The color of the retry button
 */
@Composable
fun NetworkErrorStateDisplay(
    errorMessage: String,
    isNetworkAvailable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 48,
    titleFontSize: Int = 18,
    messageFontSize: Int = 14,
    retryButtonColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Polite live region: the error replaces the list without moving
        // focus, so TalkBack announces it
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        ) {
            // Error icon based on state
            val icon: ImageVector
            val iconTint: Color
            val title: String
            val message: String

            if (!isNetworkAvailable) {
                icon = Icons.Default.WifiOff
                iconTint = MaterialTheme.colorScheme.primary
                title = stringResource(MR.strings.common_network_unavailable)
                message = stringResource(MR.strings.error_no_cached_cocktails)
            } else {
                icon = Icons.Default.Error
                iconTint = Color.Red
                title = stringResource(MR.strings.error_unable_to_load_cocktails)
                message = errorMessage
            }

            Icon(
                imageVector = icon,
                contentDescription = null, // decorative: the title follows
                tint = iconTint,
                modifier = Modifier.size(iconSize.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = iconTint,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = titleFontSize.sp,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = messageFontSize.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = retryButtonColor
                )
            ) {
                Text(stringResource(MR.strings.common_retry))
            }
        }
    }
}
