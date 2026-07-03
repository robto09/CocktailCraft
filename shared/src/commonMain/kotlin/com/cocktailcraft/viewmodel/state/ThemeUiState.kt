package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.UserPreferences

/**
 * Consolidated UI state for the Theme screen.
 */
data class ThemeUiState(
    val isDarkMode: Boolean = false,
    val themeMode: String = "system",
    val isSystemTheme: Boolean = true,
    val accentColor: String = "blue",
    val fontSize: String = "medium",
    val isHighContrast: Boolean = false,
    val isReducedMotion: Boolean = false,
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
)

