package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable component for displaying an end of list message with animation.
 *
 * @param visible Whether the message is visible
 * @param message The message to display
 * @param modifier The modifier for the component
 * @param fontSize The font size of the message
 * @param animationDuration The duration of the animation in milliseconds
 * @param animationDelay The delay before starting the animation in milliseconds
 */
@Composable
fun EndOfListMessage(
    visible: Boolean,
    message: String = "You've reached the end of the list",
    modifier: Modifier = Modifier,
    fontSize: Int = 14,
    animationDuration: Int = 500,
    animationDelay: Int = 300
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = animationDuration,
                delayMillis = animationDelay
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = animationDuration
            )
        )
    ) {
        // Animate offset for entry
        val animatedOffset by animateFloatAsState(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = animationDuration,
                delayMillis = animationDelay,
                easing = FastOutSlowInEasing
            ),
            label = "end_of_list_offset"
        )

        Text(
            text = message,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .offset(y = animatedOffset.dp),
            textAlign = TextAlign.Center,
            color = AppColors.TextSecondary,
            fontSize = fontSize.sp
        )
    }
}
