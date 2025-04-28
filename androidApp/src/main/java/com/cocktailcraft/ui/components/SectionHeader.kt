package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable section header component with optional action button.
 *
 * @param title The title text to display
 * @param modifier The modifier for the component
 * @param actionText Optional text for the action button (null to hide)
 * @param onActionClick Optional callback for when the action button is clicked
 * @param titleColor The color of the title text
 * @param actionColor The color of the action text
 * @param fontSize The font size of the title
 * @param fontWeight The font weight of the title
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
    actionColor: androidx.compose.ui.graphics.Color = AppColors.Primary,
    fontSize: Int = 18,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = titleColor
        )
        
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = actionColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
