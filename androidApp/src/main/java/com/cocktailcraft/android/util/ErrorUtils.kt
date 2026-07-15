package com.cocktailcraft.android.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.util.ErrorHandler

/**
 * Compose helpers for presenting errors on Android.
 *
 * Error construction lives in the shared [ErrorHandler] (with
 * [com.cocktailcraft.util.PlatformErrorHandler] for Android-specific
 * exceptions); this object only maps error categories to UI resources.
 */
object ErrorUtils {

    /**
     * Get an appropriate icon for the error category
     */
    @Composable
    fun getErrorIcon(category: ErrorHandler.ErrorCategory): ImageVector {
        return when (category) {
            ErrorHandler.ErrorCategory.NETWORK -> Icons.Default.WifiOff
            ErrorHandler.ErrorCategory.SERVER,
            ErrorHandler.ErrorCategory.CLIENT -> Icons.Default.Warning
            else -> Icons.Default.Error
        }
    }

    /**
     * Get an appropriate color for the error category
     */
    @Composable
    fun getErrorColor(category: ErrorHandler.ErrorCategory): Color {
        return when (category) {
            ErrorHandler.ErrorCategory.NETWORK -> AppColors.Primary
            ErrorHandler.ErrorCategory.SERVER -> AppColors.Warning
            ErrorHandler.ErrorCategory.AUTHENTICATION -> AppColors.Error
            else -> AppColors.Error
        }
    }
}
