package com.cocktailcraft.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import android.app.Activity
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

// Animation specifications
private val ColorAnimationSpec: AnimationSpec<Color> = tween(durationMillis = 600)

// Animated color scheme for light theme
private val AnimatedLightColorScheme = lightColorScheme(
    primary = AppColors.PrimaryLight,
    secondary = AppColors.SecondaryLight,
    background = AppColors.BackgroundLight,
    surface = AppColors.SurfaceLight,
    error = AppColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = AppColors.TextPrimaryLight,
    onSurface = AppColors.TextPrimaryLight,
    onError = Color.White
)

// Animated color scheme for dark theme
private val AnimatedDarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryDark,
    secondary = AppColors.SecondaryDark,
    background = AppColors.BackgroundDark,
    surface = AppColors.SurfaceDark,
    error = AppColors.Error,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = AppColors.TextPrimaryDark,
    onSurface = AppColors.TextPrimaryDark,
    onError = Color.White
)

/**
 * Animated version of CocktailBarTheme that smoothly transitions between light and dark themes
 */
@Composable
fun AnimatedCocktailBarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Update the AppColors isDarkTheme value to match current theme
    AppColors.isDarkTheme = darkTheme
    
    // Animate color transitions
    val primary by animateColorAsState(
        targetValue = if (darkTheme) AppColors.PrimaryDark else AppColors.PrimaryLight,
        animationSpec = ColorAnimationSpec,
        label = "primary"
    )
    
    val secondary by animateColorAsState(
        targetValue = if (darkTheme) AppColors.SecondaryDark else AppColors.SecondaryLight,
        animationSpec = ColorAnimationSpec,
        label = "secondary"
    )
    
    val background by animateColorAsState(
        targetValue = if (darkTheme) AppColors.BackgroundDark else AppColors.BackgroundLight,
        animationSpec = ColorAnimationSpec,
        label = "background"
    )
    
    val surface by animateColorAsState(
        targetValue = if (darkTheme) AppColors.SurfaceDark else AppColors.SurfaceLight,
        animationSpec = ColorAnimationSpec,
        label = "surface"
    )
    
    val onBackground by animateColorAsState(
        targetValue = if (darkTheme) AppColors.TextPrimaryDark else AppColors.TextPrimaryLight,
        animationSpec = ColorAnimationSpec,
        label = "onBackground"
    )
    
    val onSurface by animateColorAsState(
        targetValue = if (darkTheme) AppColors.TextPrimaryDark else AppColors.TextPrimaryLight,
        animationSpec = ColorAnimationSpec,
        label = "onSurface"
    )
    
    // Create a custom color scheme with animated colors
    val colorScheme = if (darkTheme) {
        AnimatedDarkColorScheme.copy(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface
        )
    } else {
        AnimatedLightColorScheme.copy(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface
        )
    }
    
    // Handle system UI colors with animation
    val systemUiController = rememberSystemUiController()
    val statusBarColor by animateColorAsState(
        targetValue = if (darkTheme) AppColors.BackgroundDark else AppColors.PrimaryLight,
        animationSpec = ColorAnimationSpec,
        label = "statusBarColor"
    )
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Set status bar color with animation
            window.statusBarColor = statusBarColor.toArgb()
            
            // Set status bar icons to be light or dark based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
