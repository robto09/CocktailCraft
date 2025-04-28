package com.cocktailcraft.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors
import com.cocktailcraft.ui.theme.ThemeAssets

/**
 * A reusable empty state component that can be used across different screens.
 *
 * @param title The title text to display
 * @param message The message text to display
 * @param actionButtonText The text for the action button (null to hide button)
 * @param onActionButtonClick The callback for when the action button is clicked
 * @param modifier The modifier for the component
 * @param icon Optional icon to display (will be used if painter is null)
 * @param painter Optional painter to display (takes precedence over icon)
 * @param iconSize The size of the icon or image
 * @param iconTint The tint color for the icon (not applied to painter)
 */
@Composable
fun EmptyStateComponent(
    title: String,
    message: String,
    actionButtonText: String? = null,
    onActionButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    painter: Painter? = ThemeAssets.emptyStateIllustration(),
    iconSize: Int = 120,
    iconTint: androidx.compose.ui.graphics.Color = AppColors.Gray
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display either the painter or the icon
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = title,
                modifier = Modifier.size(iconSize.dp)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(iconSize.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        if (actionButtonText != null) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onActionButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = actionButtonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
