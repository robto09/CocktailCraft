package com.cocktailcraft.android.screenshot

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.ui.theme.AppColors
import com.cocktailcraft.designsystem.AccentColorTokens
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.roborazziSystemPropertyOutputDirectory
import org.junit.Rule
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Theme configuration a screenshot is rendered in. [name] becomes the
 * filename suffix, so renaming a variant orphans its baselines.
 */
data class ThemeVariant(
    val name: String,
    val darkTheme: Boolean,
    val accentColor: String = AccentColorTokens.DEFAULT,
    val highContrast: Boolean = false,
    val fontScale: Float = 1f
) {
    override fun toString() = name

    companion object {
        val Light = ThemeVariant("light", darkTheme = false)
        val Dark = ThemeVariant("dark", darkTheme = true)

        // Accessibility variants for a handful of key components
        val HighContrastDark = ThemeVariant("dark_highContrast", darkTheme = true, highContrast = true)
        val LargeFont = ThemeVariant("light_largeFont", darkTheme = false, fontScale = 1.5f)
        val AccentBlue = ThemeVariant("light_accentBlue", darkTheme = false, accentColor = "blue")

        fun lightAndDark(): List<Array<Any>> = listOf(arrayOf<Any>(Light), arrayOf<Any>(Dark))
    }
}

/**
 * Base for Roborazzi screenshot tests: renders one composable per test inside
 * the app theme on Robolectric (native graphics, no emulator) and captures it.
 *
 * Subclasses run under [ParameterizedRobolectricTestRunner] with
 * [ThemeVariant.lightAndDark] so every test yields a light and a dark image.
 * Baselines live in androidApp/src/test/screenshots — record with
 * `./gradlew recordRoborazziDebug`, verify with `./gradlew verifyRoborazziDebug`
 * (also what a plain `testDebugUnitTest` run does).
 */
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// Plain Application: the real one starts Koin, network monitoring and WorkManager
@Config(application = Application::class)
abstract class ScreenshotTest(private val variant: ThemeVariant) {

    @get:Rule
    val composeRule = createComposeRule()

    /**
     * Captures [content] as `<ClassName>/<name>_<variant>.png`. The capture is
     * cropped to the content (plus a small themed margin so card shadows show).
     */
    @OptIn(ExperimentalRoborazziApi::class)
    protected fun snapshot(name: String, content: @Composable () -> Unit) {
        // Keep resource qualifiers (values-night) consistent with the theme
        RuntimeEnvironment.setQualifiers(if (variant.darkTheme) "+night" else "+notnight")
        composeRule.setContent {
            AnimatedCocktailBarTheme(
                darkTheme = variant.darkTheme,
                accentColor = variant.accentColor,
                highContrast = variant.highContrast,
                fontScale = variant.fontScale
            ) {
                Box(
                    modifier = Modifier
                        .testTag(CAPTURE_TAG)
                        .background(AppColors.Background)
                        .padding(8.dp)
                ) {
                    content()
                }
            }
        }
        composeRule.onNodeWithTag(CAPTURE_TAG).captureRoboImage(
            filePath = "${roborazziSystemPropertyOutputDirectory()}/" +
                "${javaClass.simpleName}/${name}_${variant.name}.png"
        )
    }

    companion object {
        private const val CAPTURE_TAG = "screenshot-root"
    }
}
