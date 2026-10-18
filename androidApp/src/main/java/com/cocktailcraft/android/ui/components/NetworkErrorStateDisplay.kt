package com.cocktailcraft.android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_network_unavailable
import com.cocktailcraft.resources.error_no_cached_cocktails
import com.cocktailcraft.resources.error_unable_to_load_cocktails

/**
 * Home's full-screen error state for load failures, shown only when there is
 * no content to render — the shared [ErrorStateView] with Home's copy. With no
 * connection it shows the no-internet state with a Retry action; auto-retry
 * reloads on its own once the network returns.
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
    if (!isNetworkAvailable) {
        ErrorStateView(
            title = stringResource(MR.strings.common_network_unavailable),
            message = stringResource(MR.strings.error_no_cached_cocktails),
            icon = Icons.Default.WifiOff,
            iconTint = MaterialTheme.colorScheme.primary,
            onRetry = onRetry,
            modifier = modifier,
            iconSize = iconSize,
            titleFontSize = titleFontSize,
            messageFontSize = messageFontSize,
            retryButtonColor = retryButtonColor
        )
    } else {
        ErrorStateView(
            title = stringResource(MR.strings.error_unable_to_load_cocktails),
            message = errorMessage,
            icon = Icons.Default.Error,
            iconTint = Color.Red,
            onRetry = onRetry,
            modifier = modifier,
            iconSize = iconSize,
            titleFontSize = titleFontSize,
            messageFontSize = messageFontSize,
            retryButtonColor = retryButtonColor
        )
    }
}
