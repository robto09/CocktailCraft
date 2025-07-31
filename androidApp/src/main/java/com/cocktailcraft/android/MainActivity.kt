package com.cocktailcraft.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cocktailcraft.android.ui.main.MainScreen
import com.cocktailcraft.android.ui.theme.AnimatedCocktailBarTheme
import com.cocktailcraft.android.viewmodel.ThemeViewModelSKIE

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Get the ThemeViewModel to observe dark mode preference
            val themeViewModel: ThemeViewModelSKIE = viewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            // Get the current system dark mode state
            val isSystemInDarkTheme = isSystemInDarkTheme()

            // Update the ThemeViewModel with the current system dark mode state
            DisposableEffect(isSystemInDarkTheme) {
                if (isSystemInDarkTheme) {
                    themeViewModel.applySystemTheme()
                }
                onDispose { }
            }

            // Use the dark mode value from the ThemeViewModel
            AnimatedCocktailBarTheme(darkTheme = isDarkMode) {
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

