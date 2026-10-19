package com.cocktailcraft.android.ui.theme

import android.app.Activity
import android.os.Build
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.cocktailcraft.designsystem.AccentColorTokens
import com.revenuecat.placeholder.LocalPlaceholderTheme
import com.revenuecat.placeholder.PlaceholderDefaults
import com.revenuecat.placeholder.PlaceholderTheme
import com.revenuecat.placeholder.Shimmer

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
 * (The static CocktailBarTheme it once wrapped was dead code, since deleted.)
 *
 * Colors reach UI code only through the theme: MaterialTheme.colorScheme
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

    // Theme-dependent extended colors cross-fade with the scheme; rating
    // stars follow the brand gold (secondary) palette
    val targetExtended = cocktailExtendedColors(darkTheme, accentColor)
    val neutralSubtle by animateColorAsState(targetExtended.neutralSubtle, colorSpec, label = "neutralSubtle")
    val surfaceRaised by animateColorAsState(targetExtended.surfaceRaised, colorSpec, label = "surfaceRaised")
    val topBar by animateColorAsState(targetExtended.topBar, colorSpec, label = "topBar")
    val extendedColors = targetExtended.copy(
        rating = secondary,
        neutralSubtle = neutralSubtle,
        surfaceRaised = surfaceRaised,
        topBar = topBar
    )

    // Under edge-to-edge the status bar is transparent over the app's own
    // background. Every screen paints the top bar behind it (AppTopBar):
    // brand coral in light theme, dark surface in dark theme. Both need white
    // status icons, matching the iOS branded navigation bar.
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
            LocalPlaceholderTheme provides cocktailPlaceholderTheme(
                fill = neutralSubtle,
                highlight = colorScheme.surface,
                reducedMotion = reducedMotion
            ),
            content = content
        )
    }
}

/**
 * Defaults for every Modifier.placeholder in the app: skeleton blocks use the
 * image-placeholder fill (neutralSubtle) with a surface-colored shimmer.
 * Reduce Motion drops the shimmer and the fade, leaving static blocks.
 */
@Composable
private fun cocktailPlaceholderTheme(
    fill: Color,
    highlight: Color,
    reducedMotion: Boolean
): PlaceholderTheme = remember(fill, highlight, reducedMotion) {
    if (reducedMotion) {
        PlaceholderTheme(
            color = fill,
            highlight = null,
            placeholderFadeTransitionSpec = { snap() },
            contentFadeTransitionSpec = { snap() }
        )
    } else {
        PlaceholderTheme(
            color = fill,
            highlight = Shimmer(
                highlightColor = highlight.copy(alpha = 0.6f),
                animationSpec = PlaceholderDefaults.shimmer.animationSpec
            )
        )
    }
}
