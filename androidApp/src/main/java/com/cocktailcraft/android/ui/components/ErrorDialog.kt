package com.cocktailcraft.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cocktailcraft.android.util.ErrorUtils
import com.cocktailcraft.android.util.resolve
import com.cocktailcraft.android.util.stringResource
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.dialog_dismiss
import com.cocktailcraft.resources.dialog_try_again
import com.cocktailcraft.resources.error
import com.cocktailcraft.util.ErrorHandler

/**
 * A reusable error dialog component that displays user-friendly error messages
 * with appropriate icons and recovery actions.
 */
@Composable
fun ErrorDialog(
    error: ErrorHandler.UserFriendlyError,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    showRecoveryAction: Boolean = true
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error icon based on category
                Icon(
                    imageVector = ErrorUtils.getErrorIcon(error.category),
                    contentDescription = stringResource(MR.strings.error),
                    tint = ErrorUtils.getErrorColor(error.category),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error title
                Text(
                    text = error.title.resolve(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Error message
                Text(
                    text = error.message.resolve(),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Dismiss button
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(MR.strings.dialog_dismiss))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Recovery action button
                    if (showRecoveryAction) {
                        error.recoveryAction?.let { recoveryAction ->
                            Button(
                                onClick = {
                                    recoveryAction.action.invoke()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(recoveryAction.actionLabel.resolve())
                            }
                        } ?: run {
                            if (onRetry != null) {
                                Button(
                                onClick = {
                                    onRetry()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(stringResource(MR.strings.dialog_try_again))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
