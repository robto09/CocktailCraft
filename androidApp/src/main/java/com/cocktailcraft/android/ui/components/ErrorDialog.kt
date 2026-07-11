package com.cocktailcraft.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cocktailcraft.android.R
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.android.util.ErrorUtils

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
                containerColor = Color.White
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
                    contentDescription = stringResource(R.string.error),
                    tint = ErrorUtils.getErrorColor(error.category),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error title
                Text(
                    text = error.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Error message
                Text(
                    text = error.message,
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary,
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
                        Text(stringResource(R.string.dialog_dismiss))
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
                                    containerColor = AppColors.Primary
                                )
                            ) {
                                Text(recoveryAction.actionLabel)
                            }
                        } ?: run {
                            if (onRetry != null) {
                                Button(
                                onClick = {
                                    onRetry()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Primary
                                )
                            ) {
                                Text(stringResource(R.string.dialog_try_again))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A non-modal error banner that appears at the top of the screen
 */
@Composable
fun ErrorBanner(
    error: ErrorHandler.UserFriendlyError?,
    onDismiss: () -> Unit,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = error != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(300))
    ) {
        error?.let {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = getErrorBackgroundColor(it.category)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ErrorUtils.getErrorIcon(it.category),
                        contentDescription = stringResource(R.string.error),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = it.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = it.message,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }

                    it.recoveryAction?.let { recoveryAction ->
                        if (onAction != null) {
                            TextButton(
                                onClick = {
                                    recoveryAction.action.invoke()
                                    onAction()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = recoveryAction.actionLabel,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Get an appropriate background color for the error banner
 */
@Composable
private fun getErrorBackgroundColor(category: ErrorHandler.ErrorCategory): Color {
    // Use the ErrorUtils.getErrorColor function and apply alpha
    return ErrorUtils.getErrorColor(category).copy(alpha = 0.9f)
}
