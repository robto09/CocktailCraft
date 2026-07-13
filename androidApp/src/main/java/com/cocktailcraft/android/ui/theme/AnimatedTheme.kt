package com.cocktailcraft.android.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import android.os.Build
import androidx.core.view.WindowCompat

// Animation specifications
private val ColorAnimationSpec: AnimationSpec<Color> = tween(durationMillis = 600)

// Animated color scheme for light theme
private val AnimatedLightColorScheme = lightColorScheme(
    primary = AppColors.PrimaryLight,
    secondary = AppColors.SecondaryLight,
    tertiary = AppColors.ChipBackground,
    background = AppColors.BackgroundLight,
    surface = AppColors.SurfaceLight,
    surfaceVariant = AppColors.LightGray,
    error = AppColors.Error,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = AppColors.TextPrimaryLight,
    onSurface = AppColors.TextPrimaryLight,
    onSurfaceVariant = AppColors.TextSecondaryLight,
    outline = AppColors.Gray,
    onError = Color.White
)

// Animated color scheme for dark theme
private val AnimatedDarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryDark,
    secondary = AppColors.SecondaryDark,
    tertiary = AppColors.ChipBackground,
    background = AppColors.BackgroundDark,
    surface = AppColors.SurfaceDark,
    surfaceVariant = AppColors.DarkGray,
    error = AppColors.Error,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = AppColors.TextPrimaryDark,
    onSurface = AppColors.TextPrimaryDark,
    onSurfaceVariant = AppColors.TextSecondaryDark,
    outline = AppColors.Gray,
    onError = Color.White
)

/**
 * The app's single theme: smoothly transitions between light and dark palettes.
 * (The static CocktailBarTheme it once wrapped was dead code, deleted in AN-5.)
 *
 * @param dynamicColor Material You wallpaper color on API 31+ (coral/gold
 * scheme is the fallback). Defaults to false — deliberately, not the usual
 * template default — because ~300 call sites still read AppColors directly;
 * flipping this on before they migrate to MaterialTheme.colorScheme would
 * mix wallpaper-derived surfaces with hardcoded coral components. Flip the
 * default once that migration lands.
 */
@Composable
fun AnimatedCocktailBarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> AnimatedDarkColorScheme.copy(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface
        )

        else -> AnimatedLightColorScheme.copy(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface
        )
    }
    
    // Under edge-to-edge the status bar is transparent over the app's own
    // background; only the icon appearance needs managing (light icons on
    // dark theme, dark icons on light theme).
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
