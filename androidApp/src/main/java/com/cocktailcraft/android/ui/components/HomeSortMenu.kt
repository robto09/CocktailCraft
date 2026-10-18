package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.home_sort_a11y
import com.cocktailcraft.resources.home_sort_current_a11y
import com.cocktailcraft.resources.home_sort_menu_title
import com.cocktailcraft.viewmodel.state.HomeSortOption

/**
 * Home's sort control: a 48dp icon button styled like the advanced-search
 * button beside it (solid while a non-default sort is applied), opening a
 * dropdown of every [HomeSortOption] with the selected one checked.
 * TalkBack reads "Sort cocktails, Sorted by Price: low to high".
 */
@Composable
fun HomeSortButton(
    selected: HomeSortOption,
    onSelect: (HomeSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val isHighlighted = selected != HomeSortOption.DEFAULT
    val currentLabel = stringResource(selected.label)
    val stateLabel = stringResource(MR.strings.home_sort_current_a11y, currentLabel)
    val shape = RoundedCornerShape(8.dp)
    val containerColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primary
    } else {
        CocktailCraftTheme.extendedColors.surfaceRaised
    }
    val outline = if (isHighlighted) {
        Modifier
    } else {
        Modifier.border(1.dp, CocktailCraftTheme.extendedColors.neutralSubtle, shape)
    }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .semantics { stateDescription = stateLabel }
                .background(color = containerColor, shape = shape)
                .then(outline)
                .size(48.dp)
                .testTag("home_sort_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(MR.strings.home_sort_a11y),
                tint = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Text(
                text = stringResource(MR.strings.home_sort_menu_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .semantics { heading() }
            )
            HomeSortOption.entries.forEach { option ->
                val isSelected = option == selected
                // DropdownMenuItem keeps the 48dp minimum height; each row is
                // a radio-style choice whose selection is spoken
                DropdownMenuItem(
                    text = { Text(stringResource(option.label)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            // The selected state carries the meaning
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                    },
                    modifier = Modifier
                        .semantics {
                            role = Role.RadioButton
                            this.selected = isSelected
                        }
                        .testTag("home_sort_option_${option.name}")
                )
            }
        }
    }
}
