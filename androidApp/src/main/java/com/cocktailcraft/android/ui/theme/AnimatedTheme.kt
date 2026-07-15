package com.cocktailcraft.android.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import android.app.Activity
import android.os.Build
import androidx.core.view.WindowCompat
import com.cocktailcraft.designsystem.AccentColorTokens

// Animation specifications
private val ColorAnimationSpec: AnimationSpec<Color> = tween(durationMillis = 600)

/**
 * True when the user enabled the Reduce Motion accessibility setting.
 * Animation call sites (shimmer, list entrances, decorative transitions)
 * read this to swap animated behavior for a static equivalent.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

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
    accentColor: String = AccentColorTokens.DEFAULT,
    highContrast: Boolean = false,
    fontScale: Float = 1f,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    // Publish the shared theme state into AppColors so its ~300 direct
    // readers pick up dark mode, the selected accent, and high contrast.
    AppColors.isDarkTheme = darkTheme
    AppColors.accentName = accentColor
    AppColors.isHighContrast = highContrast

    // Reduce Motion swaps the 600ms theme cross-fade for an instant switch
    val colorSpec: AnimationSpec<Color> = if (reducedMotion) snap() else ColorAnimationSpec

    // Animate color transitions (primary follows the selected accent)
    val primary by animateColorAsState(
        targetValue = Color(
            if (darkTheme) AccentColorTokens.dark(accentColor)
            else AccentColorTokens.light(accentColor)
        ),
        animationSpec = colorSpec,
        label = "primary"
    )

    val secondary by animateColorAsState(
        targetValue = if (darkTheme) AppColors.SecondaryDark else AppColors.SecondaryLight,
        animationSpec = colorSpec,
        label = "secondary"
    )

    val background by animateColorAsState(
        targetValue = if (darkTheme) AppColors.BackgroundDark else AppColors.BackgroundLight,
        animationSpec = colorSpec,
        label = "background"
    )

    val surface by animateColorAsState(
        targetValue = if (darkTheme) AppColors.SurfaceDark else AppColors.SurfaceLight,
        animationSpec = colorSpec,
        label = "surface"
    )

    val onBackground by animateColorAsState(
        targetValue = if (darkTheme) AppColors.TextPrimaryDark else AppColors.TextPrimaryLight,
        animationSpec = colorSpec,
        label = "onBackground"
    )

    val onSurface by animateColorAsState(
        targetValue = if (darkTheme) AppColors.TextPrimaryDark else AppColors.TextPrimaryLight,
        animationSpec = colorSpec,
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
    // background. Every screen paints the branded coral top bar behind it
    // (AppTopBar), so status icons are always white for contrast — in light
    // AND dark theme, matching the iOS branded navigation bar.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    
    // App-level font scaling (user's in-app Font Size setting) multiplies the
    // system font scale, so every sp-based text size follows it.
    val density = LocalDensity.current
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CocktailTypography,
        shapes = CocktailShapes
    ) {
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, density.fontScale * fontScale),
            LocalReducedMotion provides reducedMotion,
            content = content
        )
    }
}
