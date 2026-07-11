package com.cocktailcraft.ui.theme

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
    // Light Theme Colors
    val PrimaryLight = Color(0xFFEB6A43) // Coral/Orange for main elements
    val SecondaryLight = Color(0xFFFFC84D) // Yellow/Gold for wave and accents
    val BackgroundLight = Color(0xFFFAFAFA) // Light background
    val SurfaceLight = Color.White // White surface
    val TextPrimaryLight = Color(0xFF000000) // Black for primary text
    val TextSecondaryLight = Color(0xFF8E8E93) // Gray for secondary text

    // Dark Theme Colors
    val PrimaryDark = Color(0xFFFF8A65) // Lighter coral for dark theme
    val SecondaryDark = Color(0xFFFFD180) // Lighter gold for dark theme
    val BackgroundDark = Color(0xFF121212) // Dark background
    val SurfaceDark = Color(0xFF1E1E1E) // Dark surface
    val TextPrimaryDark = Color.White // White for primary text in dark mode
    val TextSecondaryDark = Color(0xFFB0B0B0) // Light gray for secondary text in dark mode

    // Common Colors (same in both themes)
    val Success = Color(0xFF34C759) // Green for success states
    val Error = Color(0xFFFF3B30) // Red for errors
    val Warning = Color(0xFFFF9500) // Orange for warnings and stars
    val Gray = Color(0xFF8E8E93) // Gray for secondary text
    val DarkGray = Color(0xFF636366) // Dark gray for stronger text
    val LightGray = Color(0xFFE5E5EA) // Light gray for backgrounds
    val ChipBackground = Color(0xFF9C5C38) // Brown for category chips like "Vodka"

    // Dynamic colors based on theme - these will be set by the theme
    var isDarkTheme = false

    val Primary get() = if (isDarkTheme) PrimaryDark else PrimaryLight
    val Secondary get() = if (isDarkTheme) SecondaryDark else SecondaryLight
    val Background get() = if (isDarkTheme) BackgroundDark else BackgroundLight
    val Surface get() = if (isDarkTheme) SurfaceDark else SurfaceLight
    val TextPrimary get() = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val TextSecondary get() = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.PrimaryLight,
    secondary = AppColors.SecondaryLight,
    background = AppColors.BackgroundLight,
    surface = AppColors.SurfaceLight,
    error = AppColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = AppColors.TextPrimaryLight,
    onSurface = AppColors.TextPrimaryLight,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryDark,
    secondary = AppColors.SecondaryDark,
    background = AppColors.BackgroundDark,
    surface = AppColors.SurfaceDark,
    error = AppColors.Error,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = AppColors.TextPrimaryDark,
    onSurface = AppColors.TextPrimaryDark,
    onError = Color.White
)

@Composable
fun CocktailBarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Update the AppColors isDarkTheme value to match current theme
    AppColors.isDarkTheme = darkTheme

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Set status bar color based on theme
            window.statusBarColor = if (darkTheme) {
                AppColors.BackgroundDark.toArgb() // Dark background for status bar in dark mode
            } else {
                AppColors.PrimaryLight.toArgb() // Primary color for status bar in light mode
            }

            // Set status bar icons to be light or dark based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}