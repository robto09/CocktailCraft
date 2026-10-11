package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.CocktailCraftTheme
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.designsystem.AccentColorTokens

/**
 * Row of accent color swatches for the user-selectable app accent.
 * The selected swatch shows a check mark and a ring in the current
 * text color so it works in both light and dark themes.
 *
 * For TalkBack the swatches form a radio group, each read by its display
 * name ([optionLabel]) with its selected state. They wrap onto a second
 * line rather than being clipped when the row is too narrow.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccentColorPicker(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    optionLabel: (String) -> String = { it }
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        options.forEach { name ->
            val swatchColor = Color(
                if (CocktailCraftTheme.isDarkTheme) AccentColorTokens.dark(name)
                else AccentColorTokens.light(name)
            )
            val isSelected = name.equals(selected, ignoreCase = true)
            val label = optionLabel(name)

            Box(
                modifier = Modifier
                    .size(36.dp)
                    // Compose extends the 36dp swatch's touch bounds to 48dp
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(name) }
                    )
                    .semantics { contentDescription = label }
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .padding(4.dp)
                    .background(swatchColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
