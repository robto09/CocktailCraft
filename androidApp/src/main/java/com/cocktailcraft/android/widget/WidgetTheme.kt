package com.cocktailcraft.android.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProvider
import androidx.glance.material3.ColorProviders
import com.cocktailcraft.designsystem.ColorTokens

/**
 * Widget color scheme, mapped from the cross-platform design tokens
 * (shared/designsystem/DesignTokens.kt) so home screen widgets can never
 * drift from the app palette. Supports both light and dark themes.
 */
object WidgetColors {
    // Light Theme Colors
    val PrimaryLight = Color(ColorTokens.primaryLight) // Coral/Orange for main elements
    val SecondaryLight = Color(ColorTokens.secondaryLight) // Yellow/Gold for wave and accents
    val BackgroundLight = Color(ColorTokens.backgroundLight) // Light background
    val SurfaceLight = Color(ColorTokens.surfaceLight) // White surface
    val TextPrimaryLight = Color(ColorTokens.textPrimaryLight) // Black for primary text
    val TextSecondaryLight = Color(ColorTokens.textSecondaryLight) // Gray for secondary text
    val OnPrimaryLight = Color.White

    // Dark Theme Colors
    val PrimaryDark = Color(ColorTokens.primaryDark) // Lighter coral for dark theme
    val SecondaryDark = Color(ColorTokens.secondaryDark) // Lighter gold for dark theme
    val BackgroundDark = Color(ColorTokens.backgroundDark) // Dark background
    val SurfaceDark = Color(ColorTokens.surfaceDark) // Dark surface
    val TextPrimaryDark = Color(ColorTokens.textPrimaryDark) // White for primary text in dark mode
    val TextSecondaryDark = Color(ColorTokens.textSecondaryDark) // Light gray for secondary text
    val OnPrimaryDark = Color.Black

    // Common Colors
    val Error = Color(ColorTokens.error)
    val Success = Color(ColorTokens.success)
    val Warning = Color(ColorTokens.warning)
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

