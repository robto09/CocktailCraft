package com.cocktailcraft.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.cocktailcraft.designsystem.RadiusTokens

/**
 * Shape scale built from the cross-platform RadiusTokens
 * (shared/designsystem/DesignTokens.kt), matching iOS AppTheme.CornerRadius.
 */
val CocktailShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(RadiusTokens.sm.dp),
    medium = RoundedCornerShape(RadiusTokens.md.dp),
    large = RoundedCornerShape(RadiusTokens.lg.dp),
    extraLarge = RoundedCornerShape(RadiusTokens.xl.dp)
)
