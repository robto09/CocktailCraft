package com.cocktailcraft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable search bar component with an advanced search button.
 *
 * @param searchQuery The current search query
 * @param isAdvancedSearchActive Whether advanced search is currently active
 * @param hasActiveFilters Whether there are any active filters
 * @param onSearchQueryChange Callback when the search query changes
 * @param onClearSearch Callback when the search is cleared
 * @param onToggleAdvancedSearch Callback when the advanced search button is clicked and filters are active
 * @param onShowAdvancedSearchDialog Callback when the advanced search button is clicked and no filters are active
 * @param modifier The modifier for the component
 * @param placeholder The placeholder text for the search field
 * @param searchIconTint The tint color for the search icon
 * @param clearIconTint The tint color for the clear icon
 * @param activeFilterButtonColor The background color for the filter button when active
 * @param inactiveFilterButtonColor The background color for the filter button when inactive
 * @param activeFilterIconTint The tint color for the filter icon when active
 * @param inactiveFilterIconTint The tint color for the filter icon when inactive
 */
@Composable
fun CocktailSearchBar(
    searchQuery: String,
    isAdvancedSearchActive: Boolean,
    hasActiveFilters: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onToggleAdvancedSearch: () -> Unit,
    onShowAdvancedSearchDialog: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search cocktails...",
    searchIconTint: Color = AppColors.Gray,
    clearIconTint: Color = AppColors.Gray,
    activeFilterButtonColor: Color = AppColors.Primary,
    inactiveFilterButtonColor: Color = AppColors.LightGray,
    activeFilterIconTint: Color = Color.White,
    inactiveFilterIconTint: Color = AppColors.TextSecondary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = searchIconTint
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = clearIconTint
                        )
                    }
                }
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = AppColors.Primary,
                focusedLeadingIconColor = AppColors.Primary,
                unfocusedLeadingIconColor = Color.Gray,
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Advanced search button
        IconButton(
            onClick = {
                if (isAdvancedSearchActive) {
                    // If already expanded, just collapse it
                    onToggleAdvancedSearch()
                } else {
                    // If not expanded, toggle between dialog and expandable panel
                    if (hasActiveFilters) {
                        // If filters are already applied, just expand the panel
                        onToggleAdvancedSearch()
                    } else {
                        // If no filters applied, show the dialog for better UX
                        onShowAdvancedSearchDialog()
                    }
                }
            },
            modifier = Modifier
                .background(
                    color = if (isAdvancedSearchActive || hasActiveFilters)
                        activeFilterButtonColor
                    else
                        inactiveFilterButtonColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = "Advanced Search",
                tint = if (isAdvancedSearchActive || hasActiveFilters)
                    activeFilterIconTint
                else
                    inactiveFilterIconTint
            )
        }
    }
}
