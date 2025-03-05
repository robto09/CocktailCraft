package com.example.breadapp2.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Cocktail Bar color palette
object AppColors {
    val Primary = Color(0xFFEB6A43) // Coral/Orange for main elements
    val Secondary = Color(0xFFFFC84D) // Yellow/Gold for wave and accents
    val Background = Color(0xFFFAFAFA) // Light background
    val Surface = Color.White // White surface
    val Success = Color(0xFF34C759) // Keep green for success states
    val Error = Color(0xFFFF3B30) // Keep red for errors
    val Warning = Color(0xFFFF9500) // Orange for warnings and stars
    val Gray = Color(0xFF8E8E93) // Gray for secondary text
    val DarkGray = Color(0xFF636366) // Dark gray for stronger text 
    val LightGray = Color(0xFFE5E5EA) // Light gray for backgrounds
    val TextPrimary = Color(0xFF000000) // Black for primary text
    val TextSecondary = Color(0xFF8E8E93) // Gray for secondary text
    val ChipBackground = Color(0xFF9C5C38) // Brown for category chips like "Vodka"
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    secondary = AppColors.Secondary,
    background = AppColors.Background,
    surface = AppColors.Surface,
    error = AppColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    // Dark theme would need customization if implemented
    primary = AppColors.Primary,
    secondary = AppColors.Secondary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = AppColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

@Composable
fun CocktailBarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AppColors.Primary.toArgb() // Set status bar to orange
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Light text for status bar
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 