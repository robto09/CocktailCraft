package com.cocktailcraft.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
            val themeState by themeViewModel.uiState.collectAsState()
            val isDarkMode = themeState.isDarkMode
            val isSystemTheme = themeState.isSystemTheme

            // Get the current system dark mode state
            val isSystemInDarkTheme = isSystemInDarkTheme()

            // Determine actual dark mode:
            // - If following system theme, use the actual system dark mode setting
            // - Otherwise, use the user's preference from the ViewModel
            val effectiveDarkMode = if (isSystemTheme) isSystemInDarkTheme else isDarkMode

            // Use the effective dark mode value for theming
            AnimatedCocktailBarTheme(darkTheme = effectiveDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

