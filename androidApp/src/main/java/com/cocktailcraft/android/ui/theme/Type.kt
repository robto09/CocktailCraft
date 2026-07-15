package com.cocktailcraft.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cocktailcraft.designsystem.TypographyTokens

/**
 * Material3 type ramp built from the cross-platform TypographyTokens
 * (shared/designsystem/DesignTokens.kt). iOS anchors the same roles to
 * Dynamic Type text styles, so the two platforms share one visual scale.
 *
 * Role mapping used across the app:
 * - headlineMedium: top bar titles (24sp bold)
 * - titleLarge: section headers (20sp semibold)
 * - titleMedium: card/list item titles (18sp semibold)
 * - bodyLarge/bodyMedium/bodySmall: body copy ramp
 * - labelLarge/labelMedium: buttons, chips, nav labels
 */
val CocktailTypography = Typography(
    displaySmall = TextStyle(
        fontSize = TypographyTokens.display.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = TypographyTokens.title.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = TypographyTokens.headline.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = TypographyTokens.subtitle.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = TypographyTokens.body.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = TypographyTokens.bodySmall.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = TypographyTokens.caption.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = TypographyTokens.bodySmall.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontSize = TypographyTokens.caption.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = TypographyTokens.label.sp,
        fontWeight = FontWeight.Medium
    )
)
