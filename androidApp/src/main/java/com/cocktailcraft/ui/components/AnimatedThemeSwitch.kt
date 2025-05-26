package com.cocktailcraft.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A custom animated switch for toggling between light and dark themes.
 *
 * @param isDarkMode Whether the dark mode is currently enabled
 * @param onToggle Callback when the switch is toggled
 * @param modifier Optional modifier for the switch
 * @param enabled Whether the switch is enabled
 */
@Composable
fun AnimatedThemeSwitch(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val transition = updateTransition(targetState = isDarkMode, label = "theme_switch_transition")

    // Animate the track color
    val trackColor by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF3C4043) else Color(0xFFE4E4E4),
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "track_color"
    )

    // Animate the thumb color
    val thumbColor by animateColorAsState(
        targetValue = if (isDarkMode) AppColors.PrimaryDark else AppColors.PrimaryLight,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "thumb_color"
    )

    // Animate the thumb position
    val thumbPosition by transition.animateFloat(
        label = "thumb_position",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ) { if (it) 1f else 0f }

    // Animate the icon scale
    val iconScale by transition.animateFloat(
        label = "icon_scale",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        }
    ) { if (it) 1f else 0.7f }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(trackColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onToggle
            )
            .padding(4.dp)
            .height(32.dp)
            .width(64.dp)
    ) {
        // Thumb with icon
        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(x = (36 * thumbPosition).dp)
                .clip(CircleShape)
                .background(thumbColor)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isDarkMode) "Dark Mode" else "Light Mode",
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .scale(iconScale)
            )
        }
    }
}

/**
 * A custom animated theme toggle row with icon, text, and switch.
 */
@Composable
fun AnimatedThemeToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.5f

    Row(
        modifier = modifier
            .clickable(enabled = enabled) { onToggle() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.TextPrimary,
            modifier = Modifier
                .size(24.dp)
                .alpha(alpha)
        )

        Spacer(modifier = Modifier.width(16.dp))

        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .weight(1f)
                .alpha(alpha)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }

        AnimatedThemeSwitch(
            isDarkMode = isChecked,
            onToggle = onToggle,
            enabled = enabled
        )
    }
}
