package com.cocktailcraft.android.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.cocktailcraft.designsystem.AccentColorTokens
import com.cocktailcraft.designsystem.ColorTokens

// CocktailCraft colors, mapped 1:1 from the cross-platform design tokens in
// core/designsystem/src/commonMain/kotlin/com/cocktailcraft/designsystem/DesignTokens.kt
// — the single source of truth for BOTH Android and iOS.
// res/values/colors.xml (pre-Compose window + widget assets) and
// widget/WidgetTheme.kt (Glance) mirror these for surfaces that can't read the
// Compose theme; keep them in sync when changing the shared tokens.
//
// UI code reads colors only through the theme (AN-5):
// - MaterialTheme.colorScheme for every role Material 3 defines, and
// - CocktailCraftTheme.extendedColors for brand/semantic colors it doesn't.
//
// Token → role mapping used across the app:
//   primary (+ accent)  → colorScheme.primary
//   secondary (gold)    → colorScheme.secondary
//   chipBackground      → colorScheme.tertiary
//   background/surface  → colorScheme.background / surface
//   surfaceRaised       → extendedColors.surfaceRaised
//   textPrimary         → colorScheme.onSurface (== onBackground)
//   textSecondary       → colorScheme.onSurfaceVariant (high-contrast aware)
//   gray                → colorScheme.outline (high-contrast aware, light)
//   lightGray / subtleDark → extendedColors.neutralSubtle
//   error               → colorScheme.error
//   success / warning   → extendedColors.success / warning
//   rating stars        → extendedColors.rating
//   top bar             → extendedColors.topBar (coral light, surface dark)
// neutralSubtle is NOT outlineVariant and surfaceRaised is NOT
// surfaceContainerHigh: those roles feed Material defaults (chip and
// outlined-button borders, default dividers, dialogs), so remapping them would
// restyle those components.

/**
 * Builds the brand Material 3 color scheme from the shared tokens, honouring
 * the user's accent color (primary) and high-contrast setting (secondary text
 * pushed further from the background; in light theme also the gray outline
 * role, which de-emphasized labels, inactive icons and borders use — gray
 * already clears 4.5:1 on the dark surfaces). AnimatedCocktailBarTheme
 * animates the theme-dependent roles of this scheme.
 */
internal fun cocktailColorScheme(
    darkTheme: Boolean,
    accentColor: String = AccentColorTokens.DEFAULT,
    highContrast: Boolean = false
): ColorScheme {
    val gray = Color(ColorTokens.gray)
    val lightGray = Color(ColorTokens.lightGray)
    val darkGray = Color(ColorTokens.darkGray)
    val chip = Color(ColorTokens.chipBackground)
    val error = Color(ColorTokens.error)
    return if (darkTheme) {
        darkColorScheme(
            primary = Color(AccentColorTokens.dark(accentColor)),
            secondary = Color(ColorTokens.secondaryDark),
            tertiary = Color(ColorTokens.chipBackgroundDark),
            background = Color(ColorTokens.backgroundDark),
            surface = Color(ColorTokens.surfaceDark),
            // Neutral elevation: the default primary tint turns elevated
            // dark surfaces (nav bar, dialogs) brown
            surfaceTint = Color(ColorTokens.surfaceDark),
            surfaceVariant = darkGray,
            error = error,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = Color(ColorTokens.textPrimaryDark),
            onSurface = Color(ColorTokens.textPrimaryDark),
            onSurfaceVariant = if (highContrast) lightGray else Color(ColorTokens.textSecondaryDark),
            outline = gray, // already >= 4.5:1 on the dark surfaces
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = Color(AccentColorTokens.light(accentColor)),
            secondary = Color(ColorTokens.secondaryLight),
            tertiary = chip,
            background = Color(ColorTokens.backgroundLight),
            surface = Color(ColorTokens.surfaceLight),
            // Neutral elevation, as in dark: the default primary tint turns
            // the nav bar peach and cards coral-tinged (iOS stays white)
            surfaceTint = Color(ColorTokens.surfaceLight),
            surfaceVariant = lightGray,
            error = error,
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.White,
            onBackground = Color(ColorTokens.textPrimaryLight),
            onSurface = Color(ColorTokens.textPrimaryLight),
            onSurfaceVariant = if (highContrast) darkGray else Color(ColorTokens.textSecondaryLight),
            outline = if (highContrast) darkGray else gray,
            onError = Color.White
        )
    }
}

/**
 * Brand/semantic colors with no Material 3 role (AN-5). Provided by
 * AnimatedCocktailBarTheme; read via [CocktailCraftTheme.extendedColors].
 * Deliberately independent of the Material scheme so they keep their brand
 * meaning even if the scheme ever comes from dynamic color.
 */
@Immutable
data class ExtendedColors(
    /** Positive status: in stock, completed, online. */
    val success: Color,
    /** Cautionary status: in progress, server trouble. */
    val warning: Color,
    /** Filled rating stars (brand gold, lighter in dark theme). */
    val rating: Color,
    /**
     * Subtle neutral for dividers, unfocused borders, empty stars, and image
     * placeholder fills (brand lightGray; subtleDark in dark theme).
     */
    val neutralSubtle: Color,
    /** Fill for controls raised on a surface: search field, unselected chips. */
    val surfaceRaised: Color,
    /** Top bar and the status-bar strip behind it: brand coral, surface in dark. */
    val topBar: Color
)

internal fun cocktailExtendedColors(
    darkTheme: Boolean,
    accentColor: String = AccentColorTokens.DEFAULT
): ExtendedColors = ExtendedColors(
    success = Color(ColorTokens.success),
    warning = Color(ColorTokens.warning),
    rating = Color(if (darkTheme) ColorTokens.secondaryDark else ColorTokens.secondaryLight),
    neutralSubtle = Color(if (darkTheme) ColorTokens.subtleDark else ColorTokens.lightGray),
    surfaceRaised = Color(if (darkTheme) ColorTokens.surfaceRaisedDark else ColorTokens.surfaceRaisedLight),
    topBar = Color(if (darkTheme) ColorTokens.surfaceDark else AccentColorTokens.light(accentColor))
)

internal val LocalExtendedColors = staticCompositionLocalOf { cocktailExtendedColors(darkTheme = false) }

internal val LocalDarkTheme = staticCompositionLocalOf { false }

/**
 * Theme accessors beyond MaterialTheme, mirroring its `MaterialTheme.x` shape.
 */
object CocktailCraftTheme {
    /** Brand/semantic colors with no Material role (success, warning, rating, ...). */
    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current

    /** Whether the effective theme is dark (for theme-specific assets). */
    val isDarkTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalDarkTheme.current
}
