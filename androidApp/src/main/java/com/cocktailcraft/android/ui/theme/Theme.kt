package com.cocktailcraft.android.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.cocktailcraft.designsystem.AccentColorTokens
import com.cocktailcraft.designsystem.ColorTokens

// CocktailCraft color palette, mapped 1:1 from the cross-platform design
// tokens in shared/designsystem/DesignTokens.kt — the single source of truth
// for BOTH Android and iOS. res/values/colors.xml (pre-Compose window +
// widget assets) still mirrors these values by hand; keep it in sync when
// changing anything in the shared tokens.
//
// AN-5 status: the dead CocktailBarTheme (and its deprecated
// window.statusBarColor handling) was deleted — AnimatedCocktailBarTheme in
// AnimatedTheme.kt is the app's one theme. The remaining follow-up is the
// incremental migration of AppColors.* reads (~320 call sites) onto
// MaterialTheme.colorScheme, after which the mutable isDarkTheme global goes
// away and dynamic color can be considered.
object AppColors {
    // Light Theme Colors
    val PrimaryLight = Color(ColorTokens.primaryLight) // Coral/Orange for main elements
    val SecondaryLight = Color(ColorTokens.secondaryLight) // Yellow/Gold for wave and accents
    val BackgroundLight = Color(ColorTokens.backgroundLight) // Light background
    val SurfaceLight = Color(ColorTokens.surfaceLight) // White surface
    val TextPrimaryLight = Color(ColorTokens.textPrimaryLight) // Black for primary text
    val TextSecondaryLight = Color(ColorTokens.textSecondaryLight) // Gray for secondary text

    // Dark Theme Colors
    val PrimaryDark = Color(ColorTokens.primaryDark) // Lighter coral for dark theme
    val SecondaryDark = Color(ColorTokens.secondaryDark) // Lighter gold for dark theme
    val BackgroundDark = Color(ColorTokens.backgroundDark) // Dark background
    val SurfaceDark = Color(ColorTokens.surfaceDark) // Dark surface
    val TextPrimaryDark = Color(ColorTokens.textPrimaryDark) // White for primary text in dark mode
    val TextSecondaryDark = Color(ColorTokens.textSecondaryDark) // Light gray secondary text in dark mode

    // Common Colors (same in both themes)
    val Success = Color(ColorTokens.success) // Green for success states
    val Error = Color(ColorTokens.error) // Red for errors
    val Warning = Color(ColorTokens.warning) // Orange for warnings and stars
    val Gray = Color(ColorTokens.gray) // Gray for secondary text
    val DarkGray = Color(ColorTokens.darkGray) // Dark gray for stronger text
    val LightGray = Color(ColorTokens.lightGray) // Light gray for backgrounds
    val ChipBackground = Color(ColorTokens.chipBackground) // Brown for category chips like "Vodka"

    // Dynamic colors based on theme. Snapshot-backed so any composable that
    // reads AppColors.* subscribes and recomposes when the theme flips —
    // a plain var here silently served stale colors after theme changes.
    // All three are written only by AnimatedCocktailBarTheme from the shared
    // theme state.
    var isDarkTheme by mutableStateOf(false)
    var accentName by mutableStateOf(AccentColorTokens.DEFAULT)
    var isHighContrast by mutableStateOf(false)

    // Primary follows the user-selected accent ("coral" = brand default)
    val Primary get() = Color(
        if (isDarkTheme) AccentColorTokens.dark(accentName)
        else AccentColorTokens.light(accentName)
    )
    val Secondary get() = if (isDarkTheme) SecondaryDark else SecondaryLight
    val Background get() = if (isDarkTheme) BackgroundDark else BackgroundLight
    val Surface get() = if (isDarkTheme) SurfaceDark else SurfaceLight
    val TextPrimary get() = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight

    // High contrast pushes secondary text further from the background
    val TextSecondary get() = when {
        isHighContrast && isDarkTheme -> LightGray
        isHighContrast -> DarkGray
        isDarkTheme -> TextSecondaryDark
        else -> TextSecondaryLight
    }
}
