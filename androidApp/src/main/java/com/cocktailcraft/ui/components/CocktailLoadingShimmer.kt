package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * A reusable component for displaying a shimmer loading effect for cocktails.
 *
 * @param itemCount The number of shimmer items to display
 * @param modifier The modifier for the component
 * @param headerHeight The height of the shimmer header
 * @param headerWidthFraction The width fraction of the shimmer header
 * @param itemSpacing The spacing between shimmer items
 * @param contentPadding The padding for the content
 */
@Composable
fun CocktailLoadingShimmer(
    itemCount: Int = 5,
    modifier: Modifier = Modifier,
    headerHeight: Int = 24,
    headerWidthFraction: Float = 0.5f,
    itemSpacing: Int = 16,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(itemSpacing.dp),
        contentPadding = contentPadding
    ) {
        // Shimmer header
        item(key = "shimmer_header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth(headerWidthFraction)
                    .height(headerHeight.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }

        // Shimmer items
        items(List(itemCount) { it }) { _ ->
            CocktailItemShimmer()
        }
    }
}
