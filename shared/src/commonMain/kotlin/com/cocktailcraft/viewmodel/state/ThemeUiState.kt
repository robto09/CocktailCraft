package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.designsystem.AccentColorTokens
import com.cocktailcraft.domain.model.ThemeMode
import com.cocktailcraft.domain.model.UserPreferences

/**
 * Consolidated UI state for the Theme screen.
 * Theme selection is a single [ThemeMode]; darkness/system-following are
 * derived, so the three values can never disagree.
 */
data class ThemeUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: String = AccentColorTokens.DEFAULT,
    val fontSize: String = "medium",
    val isHighContrast: Boolean = false,
    val isReducedMotion: Boolean = false,
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
) {
    val isDarkMode: Boolean get() = themeMode == ThemeMode.DARK
    val isSystemTheme: Boolean get() = themeMode == ThemeMode.SYSTEM
}
