package com.cocktailcraft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * A reusable component for displaying a header image with a gradient overlay.
 *
 * @param imageUrl The URL of the image to display
 * @param contentDescription The content description for the image
 * @param modifier The modifier for the component
 * @param height The height of the image
 * @param targetSize The target size for image optimization
 * @param gradientColors The colors for the gradient overlay
 * @param gradientStartY The starting Y position for the gradient (0f to 1f)
 */
@Composable
fun DetailHeaderImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Int = 250,
    targetSize: Int = 800,
    gradientColors: List<Color> = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.5f)
    ),
    gradientStartY: Float = 150f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        OptimizedImage(
            url = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            targetSize = targetSize
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = gradientStartY
                    )
                )
        )
    }
}
