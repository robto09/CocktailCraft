package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.favorite_toggle_a11y

/**
 * The heart toggle used on cards, cart rows and the detail screen. Built on
 * Material's IconToggleButton so TalkBack hears one stable label ("Favorite")
 * plus the checked/not-checked state — and announces the new state on toggle
 * — instead of a label that flips between "Add to" and "Remove from". The
 * default size keeps the 48 dp minimum touch target.
 *
 * @param isFavorite Whether the cocktail is currently a favorite
 * @param onToggle Called on tap (callers add their own haptics)
 * @param iconSize Visual size of the heart icon
 */
@Composable
fun FavoriteToggleButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp
) {
    IconToggleButton(
        checked = isFavorite,
        onCheckedChange = { onToggle() },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(MR.strings.favorite_toggle_a11y),
            tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(iconSize)
        )
    }
}
