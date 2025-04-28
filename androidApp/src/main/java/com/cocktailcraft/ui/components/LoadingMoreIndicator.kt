package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable component for displaying a loading indicator at the bottom of a list.
 *
 * @param isLoading Whether the loading indicator is visible
 * @param modifier The modifier for the component
 * @param indicatorSize The size of the loading indicator
 * @param animationDuration The duration of the animation in milliseconds
 * @param indicatorColor The color of the loading indicator
 */
@Composable
fun LoadingMoreIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    indicatorSize: Int = 32,
    animationDuration: Int = 300,
    indicatorColor: androidx.compose.ui.graphics.Color = AppColors.Primary
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(animationSpec = tween(durationMillis = animationDuration)),
        exit = fadeOut(animationSpec = tween(durationMillis = animationDuration))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Use a simple fade-in effect for the loading indicator
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDuration),
                label = "loading_alpha"
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .size(indicatorSize.dp)
                    .alpha(animatedAlpha),
                color = indicatorColor
            )
        }
    }
}
