package com.cocktailcraft.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.ThemeMode
import com.cocktailcraft.viewmodel.SharedThemeViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedThemeViewModel using SKIE.
 * This replaces the old ThemeViewModel with a thin wrapper around the shared implementation.
 *
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class ThemeViewModelSKIE : ViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedThemeViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val isDarkMode: StateFlow<Boolean> = sharedViewModel.isDarkMode
    val themeMode: StateFlow<ThemeMode> = sharedViewModel.themeMode
    val isSystemTheme: StateFlow<Boolean> = sharedViewModel.isSystemTheme
    val accentColor: StateFlow<String> = sharedViewModel.accentColor
    val fontSize: StateFlow<String> = sharedViewModel.fontSize
    val isHighContrast: StateFlow<Boolean> = sharedViewModel.isHighContrast
    val isReducedMotion: StateFlow<Boolean> = sharedViewModel.isReducedMotion
    
    // Expose loading and error from shared base class
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    
    // Computed properties
    val currentThemeName: String
        get() = sharedViewModel.currentThemeName
    
    val availableThemes: List<String>
        get() = sharedViewModel.availableThemes
    
    val availableAccentColors: List<String>
        get() = sharedViewModel.availableAccentColors
    
    val availableFontSizes: List<String>
        get() = sharedViewModel.availableFontSizes
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Set theme mode using SKIE async interop
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            sharedViewModel.setThemeMode(mode)
        }
    }
    
    /**
     * Value-based dark mode setter
     */
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            sharedViewModel.setDarkMode(enabled)
        }
    }
    
    /**
     * Set accent color using SKIE async interop
     */
    fun setAccentColor(color: String) {
        viewModelScope.launch {
            sharedViewModel.setAccentColor(color)
        }
    }
    
    /**
     * Set font size using SKIE async interop
     */
    fun setFontSize(size: String) {
        viewModelScope.launch {
            sharedViewModel.setFontSize(size)
        }
    }
    
    /**
     * Toggle high contrast mode using SKIE async interop
     */
    fun toggleHighContrast() {
        viewModelScope.launch {
            sharedViewModel.toggleHighContrast()
        }
    }
    
    /**
     * Toggle reduced motion using SKIE async interop
     */
    fun toggleReducedMotion() {
        viewModelScope.launch {
            sharedViewModel.toggleReducedMotion()
        }
    }
    
    /**
     * Reset theme to defaults using SKIE async interop
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            sharedViewModel.resetToDefaults()
        }
    }
    
    /**
     * Apply system theme using SKIE async interop
     */
    fun setFollowSystemTheme(enabled: Boolean) {
        viewModelScope.launch {
            sharedViewModel.setFollowSystemTheme(enabled)
        }
    }
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Check if theme mode is valid
     */
    fun isValidThemeMode(mode: String): Boolean {
        return sharedViewModel.isValidThemeMode(mode)
    }
    
    /**
     * Check if accent color is valid
     */
    fun isValidAccentColor(color: String): Boolean {
        return sharedViewModel.isValidAccentColor(color)
    }
    
    /**
     * Check if font size is valid
     */
    fun isValidFontSize(size: String): Boolean {
        return sharedViewModel.isValidFontSize(size)
    }
    
    /**
     * Get theme mode display name
     */
    fun getThemeModeDisplayName(mode: String): String {
        return sharedViewModel.getThemeModeDisplayName(mode)
    }
    
    /**
     * Get accent color display name
     */
    fun getAccentColorDisplayName(color: String): String {
        return sharedViewModel.getAccentColorDisplayName(color)
    }
    
    /**
     * Get font size display name
     */
    fun getFontSizeDisplayName(size: String): String {
        return sharedViewModel.getFontSizeDisplayName(size)
    }
    
    /**
     * Check if current theme is custom
     */
    fun isCustomTheme(): Boolean {
        return sharedViewModel.isCustomTheme()
    }
    
    /**
     * Get theme preview colors
     */
    fun getThemePreviewColors(mode: String): Map<String, String> {
        return sharedViewModel.getThemePreviewColors(mode)
    }
    
    /**
     * Check if system supports dark mode
     */
    fun systemSupportsDarkMode(): Boolean {
        return sharedViewModel.systemSupportsDarkMode()
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Refresh theme settings
     */
    fun refreshThemeSettings() {
        sharedViewModel.refreshThemeSettings()
    }
    
    /**
     * Export theme settings
     */
    fun exportThemeSettings(): Map<String, Any> {
        return sharedViewModel.exportThemeSettings()
    }
    
    /**
     * Import theme settings
     */
    fun importThemeSettings(settings: Map<String, Any>) {
        sharedViewModel.importThemeSettings(settings)
    }
    
    /**
     * Clean up when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        sharedViewModel.onCleared()
    }
}