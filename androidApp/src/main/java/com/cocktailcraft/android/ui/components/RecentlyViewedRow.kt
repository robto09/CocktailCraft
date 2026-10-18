package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.Cocktail
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.home_recently_viewed
import com.cocktailcraft.resources.list_view_details_label

/**
 * Home's "Recently viewed" row: a heading and a horizontally scrolling row
 * of compact cards (image + name), newest first. Each card is one TalkBack
 * stop that reads the cocktail's name and "double-tap to View … details".
 *
 * @param horizontalBleed How far the cards may scroll past the row's own
 * bounds on each side — the parent's horizontal padding, so they run to the
 * screen edges while the first card lines up with the heading.
 */
@Composable
fun RecentlyViewedRow(
    cocktails: List<Cocktail>,
    onCocktailClick: (Cocktail) -> Unit,
    modifier: Modifier = Modifier,
    horizontalBleed: Dp = 0.dp
) {
    Column(modifier = modifier.fillMaxWidth().testTag("home_recently_viewed")) {
        Text(
            text = stringResource(MR.strings.home_recently_viewed),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .semantics { heading() }
        )
        LazyRow(
            modifier = Modifier.layout { measurable, constraints ->
                val bleed = horizontalBleed.roundToPx()
                val placeable = measurable.measure(constraints.offset(horizontal = 2 * bleed))
                layout(placeable.width - 2 * bleed, placeable.height) { placeable.place(-bleed, 0) }
            },
            contentPadding = PaddingValues(horizontal = horizontalBleed),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cocktails, key = { "recent_${it.id}" }) { cocktail ->
                RecentlyViewedCard(cocktail = cocktail, onClick = { onCocktailClick(cocktail) })
            }
        }
    }
}

@Composable
private fun RecentlyViewedCard(cocktail: Cocktail, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(CARD_WIDTH)
            .clickable(
                onClick = onClick,
                onClickLabel = stringResource(MR.strings.list_view_details_label, cocktail.name)
            )
            .testTag("recently_viewed_item"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .size(CARD_WIDTH - 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CocktailCraftTheme.extendedColors.neutralSubtle)
            ) {
                // Decorative: the name below labels the card
                OptimizedImage(
                    url = cocktail.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    targetSize = 200,
                    showLoadingIndicator = false
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = cocktail.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val CARD_WIDTH = 112.dp
