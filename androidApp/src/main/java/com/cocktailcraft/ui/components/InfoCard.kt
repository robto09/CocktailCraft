package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable info card component that displays an icon, title, and content.
 *
 * @param title The title text of the card
 * @param icon The icon to display next to the title
 * @param modifier The modifier for the component
 * @param content The content to display in the card
 * @param iconTint The tint color for the icon
 * @param titleFontSize The font size of the title
 * @param titleFontWeight The font weight of the title
 * @param titleColor The color of the title text
 * @param backgroundColor The background color of the card
 * @param elevation The elevation of the card
 * @param cornerRadius The corner radius of the card
 * @param contentPadding The padding for the content
 * @param iconSize The size of the icon
 * @param contentTopPadding The padding between the title and content
 */
@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = AppColors.Primary,
    titleFontSize: Int = 18,
    titleFontWeight: FontWeight = FontWeight.Bold,
    titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
    backgroundColor: androidx.compose.ui.graphics.Color = AppColors.Surface,
    elevation: Int = 2,
    cornerRadius: Int = 12,
    contentPadding: Int = 16,
    iconSize: Int = 24,
    contentTopPadding: Int = 16
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(cornerRadius.dp)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize.dp)
                )

                Text(
                    text = title,
                    fontSize = titleFontSize.sp,
                    fontWeight = titleFontWeight,
                    color = titleColor,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(contentTopPadding.dp))
            
            content()
        }
    }
}
