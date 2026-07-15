package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.AppColors

/**
 * The app's standard primary call-to-action button: brand-color container,
 * white label, medium corner radius, optional loading spinner. Mirrors the
 * iOS PrimaryButtonStyle so the main CTA looks the same on both platforms.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = Color.White,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.height(54.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
