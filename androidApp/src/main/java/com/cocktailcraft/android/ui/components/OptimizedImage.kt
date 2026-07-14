package com.cocktailcraft.android.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.util.ImageLoaderSingleton
import com.cocktailcraft.android.util.ImageUtils

/**
 * An optimized image component that uses our custom image loading system.
 *
 * Built on [AsyncImage]: subcomposition-based loading is measurably more
 * expensive per Coil's guidance, and this component sits in every scrolling
 * list and grid. Load/error UI is drawn as overlays driven by onState. For
 * a single hero image where slot-based loading UI is worth it, use
 * [HeroOptimizedImage].
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

    var state by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            imageLoader = imageLoader,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale,
            onState = { state = it }
        )

        when {
            state is AsyncImagePainter.State.Loading && showLoadingIndicator ->
                CircularProgressIndicator(
                    color = AppColors.Primary,
                    modifier = Modifier.size(24.dp)
                )

            state is AsyncImagePainter.State.Error ->
                Icon(
                    imageVector = Icons.Default.LocalBar,
                    contentDescription = stringResource(R.string.common_image_not_available),
                    tint = AppColors.Gray,
                    modifier = Modifier.size(48.dp)
                )
        }
    }
}

/**
 * Hero-image variant of [OptimizedImage] that keeps Coil's slot API
 * ([SubcomposeAsyncImage]). Only use this for a single large image per
 * screen (e.g. the detail header) — never inside scrolling lists.
 */
@Composable
fun HeroOptimizedImage(
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

    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AppColors.Primary,
                    modifier = Modifier.size(24.dp)
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
                    contentDescription = stringResource(R.string.common_image_not_available),
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
