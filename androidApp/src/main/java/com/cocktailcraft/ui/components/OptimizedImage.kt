package com.cocktailcraft.ui.components

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.util.ImageLoaderSingleton
import com.cocktailcraft.util.ImageUtils

/**
 * An optimized image component that uses our custom image loading system.
 *
 * @param url The image URL to load
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier for the image
 * @param contentScale Content scaling mode
 * @param showLoadingIndicator Whether to show a loading indicator
 * @param targetSize Target size for image resizing (null for no resizing)
 */
@Composable
fun OptimizedImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showLoadingIndicator: Boolean = true,
    targetSize: Int? = null
) {
    val context = LocalContext.current
    val imageLoader = ImageLoaderSingleton.getImageLoader(context)

    // Create placeholder and error drawables
    val placeholderColor = AppColors.LightGray.toArgb()
    val placeholder = ColorDrawable(placeholderColor)

    // Build the optimized image request
    val request = ImageUtils.buildOptimizedImageRequest(
        url = url,
        size = targetSize,
        placeholder = placeholder,
        error = placeholder
    )

    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            if (showLoadingIndicator) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.LightGray)
                )
            }
        },
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalBar,
                    contentDescription = "Image not available",
                    tint = AppColors.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    )
}

/**
 * A lightweight optimized image component without loading indicators.
 * Use this for smaller images or when loading indicators aren't needed.
 */
@Composable
fun LightweightOptimizedImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    targetSize: Int? = null
) {
    val context = LocalContext.current
    val imageLoader = ImageLoaderSingleton.getImageLoader(context)

    // Create placeholder and error drawables
    val placeholderColor = AppColors.LightGray.toArgb()
    val placeholder = ColorDrawable(placeholderColor)

    // Build the optimized image request
    val request = ImageUtils.buildOptimizedImageRequest(
        url = url,
        size = targetSize,
        placeholder = placeholder,
        error = placeholder
    )

    // Use AsyncImage instead of rememberAsyncImagePainter for simplicity
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale
    )
}
