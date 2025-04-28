package com.cocktailcraft.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
 * A reusable toggle setting item component that displays a title, description, icon, and toggle switch.
 *
 * @param title The title text of the setting
 * @param description The description text of the setting
 * @param icon The icon to display next to the title
 * @param isEnabled Whether the toggle is enabled
 * @param onToggle The callback for when the toggle is clicked
 * @param modifier The modifier for the component
 * @param enabled Whether the toggle is interactive
 * @param iconTint The tint color for the icon
 * @param titleFontSize The font size of the title
 * @param descriptionFontSize The font size of the description
 * @param titleColor The color of the title text
 * @param descriptionColor The color of the description text
 * @param iconSize The size of the icon
 * @param thumbContent Optional content for the switch thumb
 */
@Composable
fun ToggleSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconTint: androidx.compose.ui.graphics.Color = if (isEnabled) AppColors.Primary else AppColors.Gray,
    titleFontSize: Int = 18,
    descriptionFontSize: Int = 14,
    titleColor: androidx.compose.ui.graphics.Color = AppColors.TextPrimary,
    descriptionColor: androidx.compose.ui.graphics.Color = AppColors.TextSecondary,
    iconSize: Int = 24,
    thumbContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
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
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = descriptionFontSize.sp,
                color = descriptionColor
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            thumbContent = thumbContent
        )
    }
}
