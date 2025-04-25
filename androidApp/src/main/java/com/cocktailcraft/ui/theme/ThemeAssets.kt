package com.cocktailcraft.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.cocktailcraft.R

/**
 * Provides access to theme-specific assets throughout the app.
 * This allows different images, icons, and other resources to be used
 * based on whether the app is in light or dark mode.
 */
object ThemeAssets {
    
    /**
     * Returns a theme-specific logo based on the current theme
     */
    @Composable
    @ReadOnlyComposable
    fun logo(): Painter {
        return if (AppColors.isDarkTheme) {
            painterResource(id = R.drawable.logo_dark)
        } else {
            painterResource(id = R.drawable.logo_light)
        }
    }
    
    /**
     * Returns a theme-specific background image based on the current theme
     */
    @Composable
    @ReadOnlyComposable
    fun backgroundPattern(): Painter {
        return if (AppColors.isDarkTheme) {
            painterResource(id = R.drawable.bg_pattern_dark)
        } else {
            painterResource(id = R.drawable.bg_pattern_light)
        }
    }
    
    /**
     * Returns a theme-specific placeholder image for cocktails
     */
    @Composable
    @ReadOnlyComposable
    fun cocktailPlaceholder(): Painter {
        return if (AppColors.isDarkTheme) {
            painterResource(id = R.drawable.cocktail_placeholder_dark)
        } else {
            painterResource(id = R.drawable.cocktail_placeholder_light)
        }
    }
    
    /**
     * Returns a theme-specific empty state illustration
     */
    @Composable
    @ReadOnlyComposable
    fun emptyStateIllustration(): Painter {
        return if (AppColors.isDarkTheme) {
            painterResource(id = R.drawable.empty_state_dark)
        } else {
            painterResource(id = R.drawable.empty_state_light)
        }
    }
}
