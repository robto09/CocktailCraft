package com.cocktailcraft.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.alcoholic
import com.cocktailcraft.resources.category_label
import com.cocktailcraft.resources.filter_active_filters
import com.cocktailcraft.resources.filter_alcoholic_filter
import com.cocktailcraft.resources.filter_clear_all
import com.cocktailcraft.resources.filter_ingredient_label
import com.cocktailcraft.resources.filter_remove_action_a11y
import com.cocktailcraft.resources.non_alcoholic

/**
 * Component to display active search filters as chips
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchFilterChips(
    filters: SearchFilters,
    onClearFilter: (String) -> Unit,
    onClearAllFilters: () -> Unit
) {
    // Only show if we have active filters
    AnimatedVisibility(
        visible = filters.hasActiveFilters(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                // 4dp + the neighbors' own 8dp = a 12dp gap above and below,
                // matching the iOS header's 12pt rhythm
                .padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Header with filter icon and clear all button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null, // decorative: the heading follows
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = stringResource(MR.strings.filter_active_filters),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = stringResource(MR.strings.filter_clear_all),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        // Compose extends the touch (and accessibility) bounds of
                        // this small clickable to the 48dp minimum
                        modifier = Modifier.clickable(role = Role.Button) { onClearAllFilters() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category filter
                    if (filters.category != null) {
                        ActiveFilterChip(
                            label = stringResource(MR.strings.category_label, filters.category!!),
                            onClear = { onClearFilter("category") }
                        )
                    }

                    // Ingredient filter
                    if (filters.ingredient != null) {
                        ActiveFilterChip(
                            label = stringResource(MR.strings.filter_ingredient_label, filters.ingredient!!),
                            onClear = { onClearFilter("ingredient") }
                        )
                    }

                    // Alcoholic filter
                    if (filters.alcoholic != null) {
                        val alcoholicLabel = when (filters.alcoholic) {
                            true -> stringResource(MR.strings.alcoholic)
                            false -> stringResource(MR.strings.non_alcoholic)
                            else -> stringResource(MR.strings.filter_alcoholic_filter)
                        }
                        ActiveFilterChip(
                            label = alcoholicLabel,
                            onClear = { onClearFilter("alcoholic") }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Active-filter chip built on Material 3's InputChip: tapping anywhere on
 * the chip removes the filter (the trailing X is the visual affordance),
 * with the ripple and full-size touch target the old hand-rolled chip lacked.
 * TalkBack reads the filter and "double-tap to remove filter".
 */
@Composable
fun ActiveFilterChip(
    label: String,
    onClear: () -> Unit
) {
    val removeLabel = stringResource(MR.strings.filter_remove_action_a11y)
    InputChip(
        selected = false,
        onClick = onClear,
        modifier = Modifier.semantics {
            onClick(label = removeLabel) {
                onClear()
                true
            }
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        shape = RoundedCornerShape(16.dp),
        border = null,
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            labelColor = MaterialTheme.colorScheme.primary,
            trailingIconColor = MaterialTheme.colorScheme.primary
        ),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null, // the chip's click label says "remove filter"
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

