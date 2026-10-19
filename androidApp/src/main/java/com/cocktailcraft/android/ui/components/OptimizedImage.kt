package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.ImageLoaderSingleton
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.common_image_not_available
import com.revenuecat.placeholder.placeholder

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
 * @param showLoadingIndicator Whether to shimmer the image area while it loads
 * @param targetSize Rough slot size in px, used only to pick the source: up
 *   to 350 loads TheCocktailDB's /medium thumbnail instead of the full-size
 *   photo (see [cocktailThumbnailUrl]). The decode size always comes from
 *   the layout.
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
    val imageLoader = ImageLoaderSingleton.getImageLoader(LocalContext.current)
    // Small list/card slot: the thumbnail variant, not the full-size photo
    val request = rememberCocktailImageRequest(url, targetSize, useThumbnail = true)

    var state by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            imageLoader = imageLoader,
            // Shimmering block while loading; the image fades in over it
            modifier = Modifier
                .matchParentSize()
                .placeholder(
                    enabled = showLoadingIndicator && state is AsyncImagePainter.State.Loading
                ),
            contentScale = contentScale,
            onState = { state = it }
        )

        if (state is AsyncImagePainter.State.Error) {
            Icon(
                imageVector = Icons.Default.LocalBar,
                // Decorative images (null description) stay silent on failure
                contentDescription = contentDescription?.let {
                    stringResource(MR.strings.common_image_not_available)
                },
                tint = MaterialTheme.colorScheme.outline,
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
    val imageLoader = ImageLoaderSingleton.getImageLoader(LocalContext.current)
    // The detail hero keeps the full-size photo
    val request = rememberCocktailImageRequest(url, targetSize, useThumbnail = false)

    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale,
        loading = {
            Box(modifier = Modifier.fillMaxSize().placeholder())
        },
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalBar,
                    // Decorative images (null description) stay silent on failure
                    contentDescription = contentDescription?.let {
                        stringResource(MR.strings.common_image_not_available)
                    },
                    tint = MaterialTheme.colorScheme.outline,
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
    val imageLoader = ImageLoaderSingleton.getImageLoader(LocalContext.current)
    // Small list/card slot: the thumbnail variant, not the full-size photo
    val request = rememberCocktailImageRequest(url, targetSize, useThumbnail = true)

    // Use AsyncImage instead of rememberAsyncImagePainter for simplicity
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = contentScale
    )
}

/**
 * TheCocktailDB serves resized copies of every drink photo next to the
 * original: `<photo url>/small`, `/medium` (~350 px) and `/large`. The
 * originals are often 700 px or more, so a list slot whose [targetSize]
 * (px) fits the medium copy asks for that instead — the same idea as the
 * iOS widget's `/preview` thumbnail. Any other URL is returned unchanged.
 */
internal fun cocktailThumbnailUrl(url: String?, targetSize: Int?): String? {
    if (url == null || targetSize == null || targetSize > MEDIUM_THUMBNAIL_PX) return url
    val isDrinkPhoto = url.startsWith("https://www.thecocktaildb.com/images/media/drink/") &&
        url.substringAfterLast('/').contains('.')
    return if (isDrinkPhoto) "$url/medium" else url
}

private const val MEDIUM_THUMBNAIL_PX = 350

/**
 * The image request for [url], built once per (url, size, placeholder
 * color) instead of on every recomposition — these images sit in scrolling
 * lists, which recompose constantly.
 */
@Composable
private fun rememberCocktailImageRequest(
    url: String?,
    targetSize: Int?,
    useThumbnail: Boolean
): ImageRequest {
    val context = LocalContext.current
    val placeholderColor = CocktailCraftTheme.extendedColors.neutralSubtle.toArgb()
    return remember(context, url, targetSize, useThumbnail, placeholderColor) {
        val data = if (useThumbnail) cocktailThumbnailUrl(url, targetSize) else url
        // Placeholder and error share one drawable
        val placeholder = placeholderColor.toDrawable()
        // No explicit size or cache keys: AsyncImage then decodes at the
        // slot's laid-out size (a fixed pixel size was upscaled on dense
        // screens), and Coil's default memory key includes that size, so the
        // cart thumbnail and the list image of the same photo get their own
        // bitmaps instead of sharing whichever was decoded first.
        // The disk cache is keyed by URL by default.
        ImageRequest.Builder(context)
            .data(data)
            .placeholder(placeholder)
            .error(placeholder)
            .allowHardware(true)
            .crossfade(true)
            .build()
    }
}
