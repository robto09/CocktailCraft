package com.cocktailcraft.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.cocktailcraft.android.ui.main.MainScreen
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // targetSdk 35+ enforces edge-to-edge; opt in explicitly so older
        // API levels get the same window behavior.
        enableEdgeToEdge()
        setContent {
            // Observe the shared theme state (single source of truth)
            val themeViewModel: SharedThemeViewModel = koinInject()
            val themeState by themeViewModel.uiState.collectAsStateWithLifecycle()
            val isDarkMode = themeState.isDarkMode
            val isSystemTheme = themeState.isSystemTheme

            // Get the current system dark mode state
            val isSystemInDarkTheme = isSystemInDarkTheme()

            // Determine actual dark mode:
            // - If following system theme, use the actual system dark mode setting
            // - Otherwise, use the user's preference from the ViewModel
            val effectiveDarkMode = if (isSystemTheme) isSystemInDarkTheme else isDarkMode

            // In-app Font Size setting multiplies the system font scale
            val fontScale = when (themeState.fontSize.lowercase()) {
                "small" -> 0.85f
                "large" -> 1.15f
                "xlarge" -> 1.3f
                else -> 1f
            }

            AnimatedCocktailBarTheme(
                darkTheme = effectiveDarkMode,
                accentColor = themeState.accentColor,
                highContrast = themeState.isHighContrast,
                fontScale = fontScale,
                reducedMotion = themeState.isReducedMotion
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        // Expose Compose testTags as resource-ids so UiAutomator
                        // (baseline profile generator, macrobenchmarks) can match
                        // nodes with By.res instead of locale-dependent text.
                        .semantics { testTagsAsResourceId = true },
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

