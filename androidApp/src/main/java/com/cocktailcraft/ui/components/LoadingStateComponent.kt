package com.cocktailcraft.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable loading state component that displays a circular progress indicator.
 *
 * @param isLoading Whether the loading state is active
 * @param modifier The modifier for the component
 * @param contentAlignment The alignment of the progress indicator
 * @param indicatorSize The size of the progress indicator
 * @param indicatorColor The color of the progress indicator
 * @param paddingValues Optional padding values
 */
@Composable
fun LoadingStateComponent(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    indicatorSize: Int = 40,
    indicatorColor: androidx.compose.ui.graphics.Color = AppColors.Primary,
    paddingValues: androidx.compose.foundation.layout.PaddingValues? = null
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .let {
                    if (paddingValues != null) {
                        it.padding(paddingValues)
                    } else {
                        it
                    }
                },
            contentAlignment = contentAlignment
        ) {
            CircularProgressIndicator(
                color = indicatorColor,
                modifier = Modifier.size(indicatorSize.dp)
            )
        }
    }
}
