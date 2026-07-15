package com.cocktailcraft.android.ui.theme

import androidx.compose.ui.unit.dp
import com.cocktailcraft.designsystem.SpacingTokens

/**
 * Spacing scale for paddings, gaps, and spacers, mapped from the
 * cross-platform SpacingTokens (shared/designsystem/DesignTokens.kt) —
 * identical to iOS AppTheme.Spacing. Use these instead of raw dp literals so
 * layout rhythm stays consistent and adjustable in one place. Component sizes
 * (icons, images, touch targets) are not spacing — keep those as explicit dp.
 */
object Spacing {
    val xs = SpacingTokens.xs.dp
    val sm = SpacingTokens.sm.dp
    val md = SpacingTokens.md.dp
    val lg = SpacingTokens.lg.dp
    val xl = SpacingTokens.xl.dp
    val xxl = SpacingTokens.xxl.dp
    val xxxl = SpacingTokens.xxxl.dp
}
