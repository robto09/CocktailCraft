package com.cocktailcraft.android.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProvider
import androidx.glance.material3.ColorProviders

/**
 * Widget color scheme matching the app's design system.
 * Supports both light and dark themes for home screen widgets.
 */
object WidgetColors {
    // Light Theme Colors
    val PrimaryLight = Color(0xFFEB6A43) // Coral/Orange for main elements
    val SecondaryLight = Color(0xFFFFC84D) // Yellow/Gold for wave and accents
    val BackgroundLight = Color(0xFFFAFAFA) // Light background
    val SurfaceLight = Color.White // White surface
    val TextPrimaryLight = Color(0xFF000000) // Black for primary text
    val TextSecondaryLight = Color(0xFF8E8E93) // Gray for secondary text
    val OnPrimaryLight = Color.White

    // Dark Theme Colors
    val PrimaryDark = Color(0xFFFF8A65) // Lighter coral for dark theme
    val SecondaryDark = Color(0xFFFFD180) // Lighter gold for dark theme
    val BackgroundDark = Color(0xFF121212) // Dark background
    val SurfaceDark = Color(0xFF1E1E1E) // Dark surface
    val TextPrimaryDark = Color.White // White for primary text in dark mode
    val TextSecondaryDark = Color(0xFFB0B0B0) // Light gray for secondary text
    val OnPrimaryDark = Color.Black

    // Common Colors
    val Error = Color(0xFFFF3B30)
    val Success = Color(0xFF34C759)
    val Warning = Color(0xFFFF9500)
}

/**
 * Create Glance color providers for widget theming.
 */
object WidgetColorProviders {

    private val lightScheme = lightColorScheme(
        primary = WidgetColors.PrimaryLight,
        onPrimary = WidgetColors.OnPrimaryLight,
        secondary = WidgetColors.SecondaryLight,
        onSecondary = WidgetColors.TextPrimaryLight,
        background = WidgetColors.BackgroundLight,
        onBackground = WidgetColors.TextPrimaryLight,
        surface = WidgetColors.SurfaceLight,
        onSurface = WidgetColors.TextPrimaryLight,
        error = WidgetColors.Error,
        onError = Color.White
    )

    private val darkScheme = darkColorScheme(
        primary = WidgetColors.PrimaryDark,
        onPrimary = WidgetColors.OnPrimaryDark,
        secondary = WidgetColors.SecondaryDark,
        onSecondary = WidgetColors.TextPrimaryDark,
        background = WidgetColors.BackgroundDark,
        onBackground = WidgetColors.TextPrimaryDark,
        surface = WidgetColors.SurfaceDark,
        onSurface = WidgetColors.TextPrimaryDark,
        error = WidgetColors.Error,
        onError = Color.White
    )

    val colors = ColorProviders(
        light = lightScheme,
        dark = darkScheme
    )

    // Color providers for individual colors (for use in TextStyle)
    val textSecondary = ColorProvider(
        day = WidgetColors.TextSecondaryLight,
        night = WidgetColors.TextSecondaryDark
    )

    val primary = ColorProvider(
        day = WidgetColors.PrimaryLight,
        night = WidgetColors.PrimaryDark
    )
}

/**
 * Wrapper composable for widget theme.
 * Applies the app's color scheme to widget content.
 */
@Composable
fun CocktailWidgetTheme(
    content: @Composable () -> Unit
) {
    GlanceTheme(
        colors = WidgetColorProviders.colors,
        content = content
    )
}

