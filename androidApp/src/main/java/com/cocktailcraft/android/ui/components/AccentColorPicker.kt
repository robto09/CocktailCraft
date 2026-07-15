package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.android.ui.theme.Spacing
import com.cocktailcraft.designsystem.AccentColorTokens

/**
 * Row of accent color swatches for the user-selectable app accent.
 * The selected swatch shows a check mark and a ring in the current
 * text color so it works in both light and dark themes.
 */
@Composable
fun AccentColorPicker(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        options.forEach { name ->
            val swatchColor = Color(
                if (AppColors.isDarkTheme) AccentColorTokens.dark(name)
                else AccentColorTokens.light(name)
            )
            val isSelected = name.equals(selected, ignoreCase = true)

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, AppColors.TextPrimary, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .padding(4.dp)
                    .background(swatchColor, CircleShape)
                    .clickable { onSelect(name) }
                    .semantics { contentDescription = name },
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
