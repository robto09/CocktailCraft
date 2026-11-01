package com.cocktailcraft.android.ui.components

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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.loading
import com.revenuecat.placeholder.PlaceholderSurface
import com.revenuecat.placeholder.placeholder

/**
 * A reusable component for displaying a shimmer loading effect for cocktails.
 *
 * @param itemCount The number of shimmer items to display
 * @param modifier The modifier for the component
 * @param showHeader Whether to draw a section-title placeholder above the items
 * @param headerHeight The height of the shimmer header
 * @param headerWidthFraction The width fraction of the shimmer header
 * @param itemSpacing The spacing between shimmer items
 * @param contentPadding The padding for the content; the default matches
 * AnimatedCocktailList so the skeleton lines up with the cards it stands in for
 */
@Composable
fun CocktailLoadingShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 5,
    showHeader: Boolean = true,
    headerHeight: Int = 24,
    headerWidthFraction: Float = 0.5f,
    itemSpacing: Int = 16,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    val loadingLabel = stringResource(MR.strings.loading)

    // One animation clock shared by the header and every item placeholder
    PlaceholderSurface {
        // The placeholders mean nothing to TalkBack: read "Loading…" instead
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .clearAndSetSemantics { contentDescription = loadingLabel },
            verticalArrangement = Arrangement.spacedBy(itemSpacing.dp),
            contentPadding = contentPadding
        ) {
            // Shimmer header, spaced like AnimatedCocktailList's title
            if (showHeader) {
                item(key = "shimmer_header") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(headerWidthFraction)
                            .padding(bottom = 8.dp)
                            .height(headerHeight.dp)
                            .placeholder(shape = RoundedCornerShape(4.dp))
                    )
                }
            }

            // Shimmer items
            items(List(itemCount) { it }) { _ ->
                CocktailItemShimmer()
            }
        }
    }
}
