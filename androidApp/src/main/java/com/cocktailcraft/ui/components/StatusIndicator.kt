package com.cocktailcraft.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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

/**
 * A reusable status indicator component that displays a colored dot or icon with status text.
 *
 * @param status The status text to display
 * @param isActive Whether the status is active
 * @param activeColor The color to use when status is active
 * @param inactiveColor The color to use when status is inactive
 * @param icon Optional icon to display instead of a dot
 * @param modifier The modifier for the component
 * @param textSize The font size of the status text
 * @param indicatorSize The size of the dot or icon
 */
@Composable
fun StatusIndicator(
    status: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color = AppColors.Gray,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    textSize: Int = 14,
    indicatorSize: Int = 10
) {
    val color = if (isActive) activeColor else inactiveColor
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = status,
                tint = color,
                modifier = Modifier.size(indicatorSize.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(indicatorSize.dp)
                    .background(color, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = status,
            fontSize = textSize.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
