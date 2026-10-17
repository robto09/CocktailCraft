package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.filters_active_a11y
import com.cocktailcraft.resources.search_advanced_search
import com.cocktailcraft.resources.search_clear_search
import com.cocktailcraft.resources.search_cocktails_placeholder

/**
 * A reusable search bar component with an advanced search button.
 *
 * @param searchQuery The current search query
 * @param isAdvancedSearchActive Whether advanced search is currently active
 * @param hasActiveFilters Whether there are any active filters
 * @param onSearchQueryChange Callback when the search query changes
 * @param onClearSearch Callback when the search is cleared
 * @param onToggleAdvancedSearch Callback when the advanced search button is clicked and filters are active
 * @param modifier The modifier for the component
 * @param placeholder The placeholder text for the search field
 * @param searchIconTint The tint color for the search icon
 * @param clearIconTint The tint color for the clear icon
 * @param activeFilterButtonColor The background color for the filter button when active
 * @param inactiveFilterButtonColor The background color for the filter button when inactive
 * @param activeFilterIconTint The tint color for the filter icon when active
 * @param inactiveFilterIconTint The tint color for the filter icon when inactive
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailSearchBar(
    searchQuery: String,
    isAdvancedSearchActive: Boolean,
    hasActiveFilters: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onToggleAdvancedSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(MR.strings.search_cocktails_placeholder),
    searchIconTint: Color = MaterialTheme.colorScheme.outline,
    clearIconTint: Color = MaterialTheme.colorScheme.outline,
    activeFilterButtonColor: Color = MaterialTheme.colorScheme.primary,
    // At rest: white with an orange funnel and a light border, as on iOS;
    // solid orange with a white funnel while filters are applied
    inactiveFilterButtonColor: Color = CocktailCraftTheme.extendedColors.surfaceRaised,
    activeFilterIconTint: Color = MaterialTheme.colorScheme.onPrimary,
    inactiveFilterIconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 48dp field (OutlinedTextField can't go below 56dp), built from the
        // same Material pieces: white, a thin light border that turns orange
        // while typing, 8dp corners. Matches the filter button beside it and
        // the iOS search field.
        val interactionSource = remember { MutableInteractionSource() }
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = CocktailCraftTheme.extendedColors.neutralSubtle,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = searchIconTint,
            focusedContainerColor = CocktailCraftTheme.extendedColors.surfaceRaised,
            unfocusedContainerColor = CocktailCraftTheme.extendedColors.surfaceRaised
        )
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("home_search_field"),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = searchQuery,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = { Text(placeholder) },
                    leadingIcon = {
                        // Decorative: the field's placeholder/label names it
                        Icon(Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(MR.strings.search_clear_search),
                                    tint = clearIconTint
                                )
                            }
                        }
                    },
                    colors = fieldColors,
                    contentPadding = PaddingValues(vertical = 0.dp),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = fieldColors,
                            shape = RoundedCornerShape(8.dp),
                            focusedBorderThickness = 1.dp,
                            unfocusedBorderThickness = 1.dp
                        )
                    }
                )
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Advanced search button; the highlighted (filters active) state is
        // also spoken
        val filtersActiveLabel = stringResource(MR.strings.filters_active_a11y)
        val isHighlighted = isAdvancedSearchActive || hasActiveFilters
        IconButton(
            onClick = { onToggleAdvancedSearch() },
            modifier = Modifier
                .semantics {
                    if (hasActiveFilters) stateDescription = filtersActiveLabel
                }
                .background(
                    color = if (isHighlighted) activeFilterButtonColor else inactiveFilterButtonColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .then(
                    if (isHighlighted) Modifier
                    else Modifier.border(1.dp, CocktailCraftTheme.extendedColors.neutralSubtle, RoundedCornerShape(8.dp))
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = stringResource(MR.strings.search_advanced_search),
                tint = if (isHighlighted) activeFilterIconTint else inactiveFilterIconTint
            )
        }
    }
}
