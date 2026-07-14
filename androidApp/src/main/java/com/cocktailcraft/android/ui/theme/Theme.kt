package com.cocktailcraft.android.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Cocktail Bar color palette — the single source of truth. res/values/colors.xml
// (pre-Compose window + widget assets) mirrors these values by hand; keep it in
// sync when changing anything here.
//
// AN-5 status: the dead CocktailBarTheme (and its deprecated
// window.statusBarColor handling) was deleted — AnimatedCocktailBarTheme in
// AnimatedTheme.kt is the app's one theme. The remaining follow-up is the
// incremental migration of AppColors.* reads (~320 call sites) onto
// MaterialTheme.colorScheme, after which the mutable isDarkTheme global goes
// away and dynamic color can be considered.
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

    // Dynamic colors based on theme. Snapshot-backed so any composable that
    // reads AppColors.* subscribes and recomposes when the theme flips —
    // a plain var here silently served stale colors after theme changes.
    var isDarkTheme by mutableStateOf(false)

    val Primary get() = if (isDarkTheme) PrimaryDark else PrimaryLight
    val Secondary get() = if (isDarkTheme) SecondaryDark else SecondaryLight
    val Background get() = if (isDarkTheme) BackgroundDark else BackgroundLight
    val Surface get() = if (isDarkTheme) SurfaceDark else SurfaceLight
    val TextPrimary get() = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
    val TextSecondary get() = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
}
