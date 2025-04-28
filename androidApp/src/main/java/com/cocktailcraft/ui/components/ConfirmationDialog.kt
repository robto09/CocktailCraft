package com.cocktailcraft.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocktailcraft.ui.theme.AppColors

/**
 * A reusable confirmation dialog component.
 *
 * @param showDialog Whether to show the dialog
 * @param title The title text of the dialog
 * @param message The message text of the dialog
 * @param confirmButtonText The text for the confirm button
 * @param dismissButtonText The text for the dismiss button
 * @param onConfirm The callback for when the confirm button is clicked
 * @param onDismiss The callback for when the dialog is dismissed
 * @param titleFontSize The font size of the title
 * @param messageFontSize The font size of the message
 * @param confirmButtonColor The color of the confirm button
 * @param dismissButtonColor The color of the dismiss button text
 */
@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    titleFontSize: Int = 18,
    messageFontSize: Int = 16,
    confirmButtonColor: androidx.compose.ui.graphics.Color = AppColors.Primary,
    dismissButtonColor: androidx.compose.ui.graphics.Color = AppColors.Primary
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    fontSize = titleFontSize.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = messageFontSize.sp,
                    lineHeight = (messageFontSize + 6).sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = confirmButtonText,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, dismissButtonColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = dismissButtonColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = dismissButtonText,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = AppColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
