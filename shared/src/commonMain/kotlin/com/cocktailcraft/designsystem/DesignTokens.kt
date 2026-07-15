package com.cocktailcraft.designsystem

/*
 * Cross-platform design tokens: the single source of truth for CocktailCraft's
 * look and feel on both platforms.
 *
 * - Android maps these into AppColors / MaterialTheme (androidApp ui/theme).
 * - iOS maps them into AppColors / AppTheme (iosApp Theme/) through the
 *   shared framework.
 *
 * Colors are packed ARGB Longs (0xAARRGGBB). Sizes are density-independent
 * units (dp/sp on Android, pt on iOS).
 *
 * Two hand-maintained mirrors CANNOT read these values and must be updated
 * alongside any brand color change here:
 * - androidApp res/values/colors.xml (pre-Compose window, cold start)
 * - the iOS widget extension's BrandColorComponents (WidgetBridge.swift),
 *   which does not link the shared framework.
 */

object BrandTokens {
    /** Product name shown in launchers, headers, and empty states. */
    const val APP_NAME: String = "CocktailCraft"
}

object ColorTokens {
    // Brand
    val primaryLight: Long = 0xFFEB6A43   // Coral, main elements
    val primaryDark: Long = 0xFFFF8A65    // Lighter coral for dark theme
    val secondaryLight: Long = 0xFFFFC84D // Gold accents
    val secondaryDark: Long = 0xFFFFD180  // Lighter gold for dark theme

    // Surfaces
    val backgroundLight: Long = 0xFFFAFAFA
    val backgroundDark: Long = 0xFF121212
    val surfaceLight: Long = 0xFFFFFFFF
    val surfaceDark: Long = 0xFF1E1E1E

    // Text
    val textPrimaryLight: Long = 0xFF000000
    val textPrimaryDark: Long = 0xFFFFFFFF
    val textSecondaryLight: Long = 0xFF8E8E93
    val textSecondaryDark: Long = 0xFFB0B0B0

    // Semantic statuses — exactly one green/red/amber per meaning, everywhere.
    val success: Long = 0xFF34C759
    val error: Long = 0xFFFF3B30
    val warning: Long = 0xFFFF9500

    // Neutrals
    val gray: Long = 0xFF8E8E93
    val darkGray: Long = 0xFF636366
    val lightGray: Long = 0xFFE5E5EA

    // Component accents
    val chipBackground: Long = 0xFF9C5C38 // Brown category chips ("Vodka")

    /** "#RRGGBB" form for callers that need CSS-style hex (theme previews). */
    fun hexString(argb: Long): String {
        val rgb = argb and 0xFFFFFF
        return "#" + rgb.toString(16).uppercase().padStart(6, '0')
    }
}

/**
 * Accent palette for the user-selectable accent color setting. "coral" is the
 * brand default; the others swap the primary color at the theme layer on both
 * platforms. Unknown names fall back to the brand coral.
 */
object AccentColorTokens {
    const val DEFAULT: String = "coral"

    val names: List<String> =
        listOf("coral", "blue", "green", "red", "orange", "purple", "pink")

    fun light(name: String): Long = when (name.lowercase()) {
        "blue" -> 0xFF007AFF
        "green" -> 0xFF34C759
        "red" -> 0xFFFF3B30
        "orange" -> 0xFFFF9500
        "purple" -> 0xFFAF52DE
        "pink" -> 0xFFFF2D55
        else -> ColorTokens.primaryLight
    }

    fun dark(name: String): Long = when (name.lowercase()) {
        "blue" -> 0xFF0A84FF
        "green" -> 0xFF30D158
        "red" -> 0xFFFF453A
        "orange" -> 0xFFFF9F0A
        "purple" -> 0xFFBF5AF2
        "pink" -> 0xFFFF375F
        else -> ColorTokens.primaryDark
    }
}

/** Spacing scale for paddings, gaps, and spacers. */
object SpacingTokens {
    val xs: Int = 4
    val sm: Int = 8
    val md: Int = 12
    val lg: Int = 16
    val xl: Int = 20
    val xxl: Int = 24
    val xxxl: Int = 32
}

/** Corner radius scale plus per-component radii. */
object RadiusTokens {
    val sm: Int = 8
    val md: Int = 12
    val lg: Int = 16
    val xl: Int = 20

    val card: Int = 12
    val button: Int = 8
    val chip: Int = 16
    val searchBar: Int = 10
}

/**
 * Type ramp sizes. Android builds its Material3 Typography from these; iOS
 * anchors the same roles to Dynamic Type text styles (AppTheme.Typography),
 * so these values double as documentation of the intended visual size there.
 */
object TypographyTokens {
    val display: Int = 28
    val title: Int = 24     // top bar titles
    val headline: Int = 20  // section headers
    val subtitle: Int = 18
    val body: Int = 16
    val bodySmall: Int = 14
    val caption: Int = 12
    val label: Int = 11
}
