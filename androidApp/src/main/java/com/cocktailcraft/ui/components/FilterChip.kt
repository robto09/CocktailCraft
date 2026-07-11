package com.cocktailcraft.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) selectedColor else unselectedColor,
        contentColor = if (selected) selectedTextColor else unselectedTextColor,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) selectedTextColor else unselectedTextColor
            )

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = if (selected) selectedIconColor else unselectedTextColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}