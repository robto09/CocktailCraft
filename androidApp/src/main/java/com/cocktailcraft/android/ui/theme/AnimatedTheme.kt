package com.cocktailcraft.android.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
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

/**
 * The app's single theme: smoothly transitions between light and dark palettes.
 * (The static CocktailBarTheme it once wrapped was dead code, deleted in AN-5.)
 *
 * Colors reach UI code only through the theme (AN-5): MaterialTheme.colorScheme
 * (built by [cocktailColorScheme] from the shared tokens, honouring
 * [accentColor] and [highContrast]) plus CocktailCraftTheme.extendedColors for
 * brand/semantic colors Material has no role for.
 *
 * @param dynamicColor Material You wallpaper color on API 31+. Defaults to
 * false on purpose: the app has a brand palette and a user accent picker,
 * which a wallpaper-derived scheme would override. Every call site now reads
 * MaterialTheme.colorScheme, so turning it on is a one-argument change
 * (extended colors stay on-brand either way).
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
    // Reduce Motion swaps the 600ms theme cross-fade for an instant switch
    val colorSpec: AnimationSpec<Color> = if (reducedMotion) snap() else ColorAnimationSpec

    // Target brand scheme; the roles that differ between light and dark (or
    // follow the accent / high contrast) are animated below.
    val target = cocktailColorScheme(darkTheme, accentColor, highContrast)

    val primary by animateColorAsState(target.primary, colorSpec, label = "primary")
    val secondary by animateColorAsState(target.secondary, colorSpec, label = "secondary")
    val background by animateColorAsState(target.background, colorSpec, label = "background")
    val surface by animateColorAsState(target.surface, colorSpec, label = "surface")
    val onBackground by animateColorAsState(target.onBackground, colorSpec, label = "onBackground")
    val onSurface by animateColorAsState(target.onSurface, colorSpec, label = "onSurface")
    val onSurfaceVariant by animateColorAsState(
        target.onSurfaceVariant,
        colorSpec,
        label = "onSurfaceVariant"
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> target.copy(
            primary = primary,
            secondary = secondary,
            background = background,
            surface = surface,
            onBackground = onBackground,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant
        )
    }

    // Rating stars cross-fade with the brand gold (secondary) palette
    val extendedColors = cocktailExtendedColors(darkTheme).copy(rating = secondary)

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
            LocalExtendedColors provides extendedColors,
            LocalDarkTheme provides darkTheme,
            content = content
        )
    }
}
