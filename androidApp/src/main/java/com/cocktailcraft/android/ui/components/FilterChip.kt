package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.android.ui.theme.AppColors
import androidx.compose.material3.FilterChip as Material3FilterChip

/**
 * App-styled wrapper around Material 3's FilterChip: pill shape, no outline,
 * brand fill when selected, plain surface otherwise. Delegating to the
 * Material component keeps the ripple, minimum touch target, and selection
 * semantics for free while preserving the app's chip look.
 */
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedColor: Color = AppColors.Primary,
    unselectedColor: Color = Color.White,
    selectedTextColor: Color = Color.White,
    unselectedTextColor: Color = AppColors.Primary,
    trailingIcon: ImageVector? = null,
    selectedIconColor: Color = Color.White
) {
    Material3FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(16.dp),
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = unselectedColor,
            labelColor = unselectedTextColor,
            iconColor = unselectedTextColor,
            selectedContainerColor = selectedColor,
            selectedLabelColor = selectedTextColor,
            selectedTrailingIconColor = selectedIconColor
        ),
        elevation = FilterChipDefaults.filterChipElevation(
            elevation = if (selected) 2.dp else 0.dp
        ),
        trailingIcon = trailingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    )
}
