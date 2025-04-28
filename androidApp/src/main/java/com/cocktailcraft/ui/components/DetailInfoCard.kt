package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable detail info card component.
 *
 * @param title The title text of the card
 * @param modifier The modifier for the component
 * @param content The content to display in the card
 * @param titleFontSize The font size of the title
 * @param titleFontWeight The font weight of the title
 * @param titleColor The color of the title text
 * @param backgroundColor The background color of the card
 * @param elevation The elevation of the card
 * @param cornerRadius The corner radius of the card
 * @param contentPadding The padding for the content
 */
@Composable
fun DetailInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    titleFontSize: Int = 18,
    titleFontWeight: FontWeight = FontWeight.Bold,
    titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
    backgroundColor: androidx.compose.ui.graphics.Color = AppColors.Surface,
    elevation: Int = 2,
    cornerRadius: Int = 12,
    contentPadding: Int = 16
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(cornerRadius.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding.dp)
        ) {
            Text(
                text = title,
                fontSize = titleFontSize.sp,
                fontWeight = titleFontWeight,
                color = titleColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            content()
        }
    }
}
