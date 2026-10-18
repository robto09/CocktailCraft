package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.cocktailcraft.android.util.ErrorUtils
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_retry
import com.cocktailcraft.util.ErrorHandler

/**
 * The app's full-screen error state — icon, title, message and a retry
 * button — for a load that failed with nothing to show instead (the
 * counterpart of iOS's ErrorView). Errors over content that is still on
 * screen are toasts instead ([ErrorToastEffect]).
 *
 * The retry button runs the error's own recovery action when it carries one,
 * else [onRetry]; with neither there is no button.
 */
@Composable
fun ErrorStateView(
    error: ErrorHandler.UserFriendlyError,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val recovery = error.recoveryAction
    ErrorStateView(
        title = error.title.resolve(),
        message = error.message.resolve(),
        icon = ErrorUtils.getErrorIcon(error.category),
        iconTint = ErrorUtils.getErrorColor(error.category),
        onRetry = recovery?.action ?: onRetry,
        retryLabel = recovery?.actionLabel?.resolve() ?: stringResource(MR.strings.common_retry),
        modifier = modifier
    )
}

/** [ErrorStateView] with explicit copy, for states without a shared error. */
@Composable
fun ErrorStateView(
    title: String,
    message: String,
    icon: ImageVector,
    iconTint: Color,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
    retryLabel: String = stringResource(MR.strings.common_retry),
    iconSize: Int = 48,
    titleFontSize: Int = 18,
    messageFontSize: Int = 14,
    retryButtonColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Polite live region: the error replaces the content without moving
        // focus, so TalkBack announces it
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        ) {
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

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = retryButtonColor
                    )
                ) {
                    Text(retryLabel)
                }
            }
        }
    }
}
