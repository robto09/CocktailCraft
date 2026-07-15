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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocktailcraft.domain.model.SearchFilters
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.theme.AppColors

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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = AppColors.Surface,
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
                        contentDescription = stringResource(R.string.filter_filters),
                        tint = AppColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = stringResource(R.string.filter_active_filters),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = stringResource(R.string.filter_clear_all),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Primary,
                        modifier = Modifier.clickable { onClearAllFilters() }
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
                            label = stringResource(R.string.category_label, filters.category!!),
                            onClear = { onClearFilter("category") }
                        )
                    }

                    // Ingredient filter
                    if (filters.ingredient != null) {
                        ActiveFilterChip(
                            label = stringResource(R.string.filter_ingredient_label, filters.ingredient!!),
                            onClear = { onClearFilter("ingredient") }
                        )
                    }

                    // Alcoholic filter
                    if (filters.alcoholic != null) {
                        val alcoholicLabel = when (filters.alcoholic) {
                            true -> stringResource(R.string.alcoholic)
                            false -> stringResource(R.string.non_alcoholic)
                            else -> stringResource(R.string.filter_alcoholic_filter)
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
 */
@Composable
fun ActiveFilterChip(
    label: String,
    onClear: () -> Unit
) {
    InputChip(
        selected = false,
        onClick = onClear,
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
            containerColor = AppColors.Primary.copy(alpha = 0.1f),
            labelColor = AppColors.Primary,
            trailingIconColor = AppColors.Primary
        ),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.filter_clear),
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

