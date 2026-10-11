package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.designsystem.AccentColorTokens
import com.cocktailcraft.designsystem.ColorTokens
import com.cocktailcraft.domain.model.ThemeMode
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.accent_color_blue
import com.cocktailcraft.resources.accent_color_coral
import com.cocktailcraft.resources.accent_color_green
import com.cocktailcraft.resources.accent_color_orange
import com.cocktailcraft.resources.accent_color_pink
import com.cocktailcraft.resources.accent_color_purple
import com.cocktailcraft.resources.accent_color_red
import com.cocktailcraft.resources.common_unknown
import com.cocktailcraft.resources.error_reset_theme_failed
import com.cocktailcraft.resources.error_save_failed_title
import com.cocktailcraft.resources.error_save_theme_failed
import com.cocktailcraft.resources.error_update_accent_color_failed
import com.cocktailcraft.resources.error_update_font_size_failed
import com.cocktailcraft.resources.error_update_high_contrast_failed
import com.cocktailcraft.resources.error_update_reduced_motion_failed
import com.cocktailcraft.resources.error_update_theme_mode_failed
import com.cocktailcraft.resources.font_size_large
import com.cocktailcraft.resources.font_size_medium
import com.cocktailcraft.resources.font_size_small
import com.cocktailcraft.resources.font_size_xlarge
import com.cocktailcraft.resources.theme_mode_dark
import com.cocktailcraft.resources.theme_mode_light
import com.cocktailcraft.resources.theme_mode_system
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.ThemeUiState
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Theme functionality.
 * Uses consolidated [ThemeUiState] for atomic state updates; [uiState] is the
 * only public state surface (the option lists below are constants, and the
 * remaining synchronous helpers are pure formatters/validators).
 *
 * All theme mutations are value-based setters (never flip-style toggles),
 * so a re-fired UI callback can only re-assert the same state.
 */
class SharedThemeViewModel internal constructor(
    private val manageProfileUseCase: ManageProfileUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    val availableThemes: List<String>
        get() = listOf("system", "light", "dark")
    val availableAccentColors: List<String>
        get() = AccentColorTokens.names
    val availableFontSizes: List<String>
        get() = listOf("small", "medium", "large", "xlarge")

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            loadUserPreferences()
        }
    }

    /**
     * Set the theme mode. The single mutation point for theme selection.
     * SKIE will convert this to Swift async function.
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        try {
            _uiState.update { it.copy(themeMode = mode) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_theme_mode_failed)
        }
    }

    /**
     * String convenience for callers driven by the availableThemes list.
     */
    suspend fun setThemeMode(mode: String) {
        setThemeMode(ThemeMode.fromString(mode))
    }

    /**
     * Value-based dark mode setter: enabling/disabling always leaves
     * system-following off, because the user expressed an explicit choice.
     */
    suspend fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    /**
     * Value-based system-following setter. Turning it off falls back to the
     * last persisted explicit choice.
     */
    suspend fun setFollowSystemTheme(enabled: Boolean) {
        setThemeMode(
            when {
                enabled -> ThemeMode.SYSTEM
                _uiState.value.userPreferences.darkMode -> ThemeMode.DARK
                else -> ThemeMode.LIGHT
            }
        )
    }

    suspend fun setAccentColor(color: String) {
        try {
            _uiState.update { it.copy(accentColor = color) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_accent_color_failed)
        }
    }

    suspend fun setFontSize(size: String) {
        try {
            _uiState.update { it.copy(fontSize = size) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_font_size_failed)
        }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        try {
            _uiState.update { it.copy(isHighContrast = enabled) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_high_contrast_failed)
        }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        try {
            _uiState.update { it.copy(isReducedMotion = enabled) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_reduced_motion_failed)
        }
    }

    suspend fun resetToDefaults() {
        try {
            _uiState.update { ThemeUiState() }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, MR.strings.error_reset_theme_failed)
        }
    }

    // MARK: - Synchronous Helper Methods

    /**
     * Validate theme mode.
     */
    fun isValidThemeMode(mode: String): Boolean {
        return availableThemes.contains(mode.lowercase())
    }

    /**
     * Validate accent color.
     */
    fun isValidAccentColor(color: String): Boolean {
        return availableAccentColors.contains(color.lowercase())
    }

    /**
     * Validate font size.
     */
    fun isValidFontSize(size: String): Boolean {
        return availableFontSizes.contains(size.lowercase())
    }

    /**
     * Get theme mode display name.
     */
    fun getThemeModeDisplayName(mode: String): StringDesc {
        return when (mode.lowercase()) {
            "light" -> MR.strings.theme_mode_light
            "dark" -> MR.strings.theme_mode_dark
            "system" -> MR.strings.theme_mode_system
            else -> MR.strings.common_unknown
        }.desc()
    }

    /**
     * Get accent color display name.
     */
    fun getAccentColorDisplayName(color: String): StringDesc {
        return when (color.lowercase()) {
            "coral" -> MR.strings.accent_color_coral.desc()
            "blue" -> MR.strings.accent_color_blue.desc()
            "green" -> MR.strings.accent_color_green.desc()
            "red" -> MR.strings.accent_color_red.desc()
            "orange" -> MR.strings.accent_color_orange.desc()
            "purple" -> MR.strings.accent_color_purple.desc()
            "pink" -> MR.strings.accent_color_pink.desc()
            else -> StringDesc.Raw(color.replaceFirstChar { it.uppercase() })
        }
    }

    /**
     * Get font size display name.
     */
    fun getFontSizeDisplayName(size: String): StringDesc {
        return when (size.lowercase()) {
            "small" -> MR.strings.font_size_small
            "medium" -> MR.strings.font_size_medium
            "large" -> MR.strings.font_size_large
            "xlarge" -> MR.strings.font_size_xlarge
            else -> MR.strings.common_unknown
        }.desc()
    }

    /**
     * Get theme preview colors.
     */
    fun getThemePreviewColors(mode: String): Map<String, String> {
        // Real brand values from the shared design tokens (the previous
        // implementation returned iOS-system-blue placeholders that matched
        // neither platform's actual palette).
        return when (mode.lowercase()) {
            "dark" -> mapOf(
                "background" to ColorTokens.hexString(ColorTokens.backgroundDark),
                "surface" to ColorTokens.hexString(ColorTokens.surfaceDark),
                "primary" to ColorTokens.hexString(ColorTokens.primaryDark),
                "text" to ColorTokens.hexString(ColorTokens.textPrimaryDark)
            )
            else -> mapOf(
                "background" to ColorTokens.hexString(ColorTokens.backgroundLight),
                "surface" to ColorTokens.hexString(ColorTokens.surfaceLight),
                "primary" to ColorTokens.hexString(ColorTokens.primaryLight),
                "text" to ColorTokens.hexString(ColorTokens.textPrimaryLight)
            )
        }
    }

    /**
     * Check if system supports dark mode.
     */
    fun systemSupportsDarkMode(): Boolean {
        return true // Most modern systems support dark mode
    }

    /**
     * Refresh theme settings.
     */
    fun refreshThemeSettings() {
        viewModelScope.launch {
            loadUserPreferences()
        }
    }

    // MARK: - Private Helper Methods

    private suspend fun loadUserPreferences() {
        try {
            val preferences = manageProfileUseCase.getUserPreferences().getOrDefault(UserPreferences())
            _uiState.update { it.copy(
                userPreferences = preferences,
                themeMode = when {
                    preferences.followSystemTheme -> ThemeMode.SYSTEM
                    preferences.darkMode -> ThemeMode.DARK
                    else -> ThemeMode.LIGHT
                },
                accentColor = preferences.accentColor,
                fontSize = preferences.fontSize,
                isHighContrast = preferences.isHighContrast,
                isReducedMotion = preferences.isReducedMotion
            ) }
        } catch (e: Exception) {
            // Silent fail for preferences loading
        }
    }

    private suspend fun savePreferences() {
        try {
            val state = _uiState.value
            val updatedPreferences = state.userPreferences.copy(
                // Under SYSTEM, keep the last explicit choice so turning
                // system-following off restores it.
                darkMode = when (state.themeMode) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> state.userPreferences.darkMode
                },
                followSystemTheme = state.isSystemTheme,
                accentColor = state.accentColor,
                fontSize = state.fontSize,
                isHighContrast = state.isHighContrast,
                isReducedMotion = state.isReducedMotion
            )

            val success = manageProfileUseCase.updateUserPreferences(updatedPreferences).getOrThrow()
            if (success) {
                _uiState.update { it.copy(userPreferences = updatedPreferences) }
            } else {
                setError(
                    MR.strings.error_save_failed_title,
                    MR.strings.error_save_theme_failed,
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
        } catch (e: Exception) {
            handleException(e, MR.strings.error_save_theme_failed)
        }
    }
}
