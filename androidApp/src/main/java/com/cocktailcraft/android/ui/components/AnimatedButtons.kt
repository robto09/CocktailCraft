package com.cocktailcraft.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cocktailcraft.android.ui.animation.AnimationUtils
import com.cocktailcraft.android.ui.theme.LocalReducedMotion
import com.cocktailcraft.android.util.HapticHandler
import com.cocktailcraft.android.util.rememberHapticHandler

/**
 * An animated icon button with scale and rotation effects.
 * Supports optional haptic feedback for enhanced user experience.
 *
 * @param onClick Callback when the button is clicked
 * @param icon The icon to display
 * @param contentDescription Accessibility description for the icon
 * @param modifier Modifier for the button
 * @param tint Color tint for the icon
 * @param enabled Whether the button is enabled
 * @param enableHapticFeedback Whether to trigger haptic feedback on click
 * @param hapticHandler Optional custom haptic handler for specialized feedback
 */
@Composable
fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    enableHapticFeedback: Boolean = false,
    hapticHandler: HapticHandler? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val defaultHapticHandler = if (enableHapticFeedback) rememberHapticHandler() else null

    val scale by animateFloatAsState(
        targetValue = if (isPressed && !LocalReducedMotion.current) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = AnimationUtils.SPRING_DAMPENING_MEDIUM_BOUNCY,
            stiffness = AnimationUtils.SPRING_STIFFNESS_MEDIUM
        ),
        label = "icon_button_scale"
    )

    IconButton(
        onClick = {
            // Trigger haptic feedback if enabled
            if (enableHapticFeedback) {
                (hapticHandler ?: defaultHapticHandler)?.performClick()
            }
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else MaterialTheme.colorScheme.outline,
            modifier = Modifier.scale(scale)
        )
    }
}
