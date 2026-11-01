package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.designsystem.AccentColorTokens
import com.cocktailcraft.domain.model.ThemeMode
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
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
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingNonCancellation
import com.cocktailcraft.viewmodel.state.ThemeUiState
import dev.icerock.moko.resources.StringResource
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
    // Direct repository dependency: preferences are read and saved
    // as they are, with nothing to wrap
    private val authRepository: AuthRepository
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

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
     * Like every setter here it applies at once and saves on viewModelScope;
     * a failed save is reported on [error].
     */
    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        persist(MR.strings.error_update_theme_mode_failed)
    }

    /**
     * Value-based dark mode setter: enabling/disabling always leaves
     * system-following off, because the user expressed an explicit choice.
     */
    fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    /**
     * Value-based system-following setter. Turning it off falls back to the
     * last persisted explicit choice.
     */
    fun setFollowSystemTheme(enabled: Boolean) {
        setThemeMode(
            when {
                enabled -> ThemeMode.SYSTEM
                _uiState.value.userPreferences.darkMode -> ThemeMode.DARK
                else -> ThemeMode.LIGHT
            }
        )
    }

    fun setAccentColor(color: String) {
        _uiState.update { it.copy(accentColor = color) }
        persist(MR.strings.error_update_accent_color_failed)
    }

    fun setFontSize(size: String) {
        _uiState.update { it.copy(fontSize = size) }
        persist(MR.strings.error_update_font_size_failed)
    }

    fun setHighContrast(enabled: Boolean) {
        _uiState.update { it.copy(isHighContrast = enabled) }
        persist(MR.strings.error_update_high_contrast_failed)
    }

    fun setReducedMotion(enabled: Boolean) {
        _uiState.update { it.copy(isReducedMotion = enabled) }
        persist(MR.strings.error_update_reduced_motion_failed)
    }

    fun resetToDefaults() {
        _uiState.update { ThemeUiState() }
        persist(MR.strings.error_reset_theme_failed)
    }

    // MARK: - Synchronous Helper Methods

    /**
     * Get accent color display name.
     */
    fun getAccentColorDisplayName(color: String): StringDesc = when (color.lowercase()) {
        "coral" -> MR.strings.accent_color_coral.desc()
        "blue" -> MR.strings.accent_color_blue.desc()
        "green" -> MR.strings.accent_color_green.desc()
        "red" -> MR.strings.accent_color_red.desc()
        "orange" -> MR.strings.accent_color_orange.desc()
        "purple" -> MR.strings.accent_color_purple.desc()
        "pink" -> MR.strings.accent_color_pink.desc()
        else -> StringDesc.Raw(color.replaceFirstChar { it.uppercase() })
    }

    /**
     * Get font size display name.
     */
    fun getFontSizeDisplayName(size: String): StringDesc = when (size.lowercase()) {
        "small" -> MR.strings.font_size_small
        "medium" -> MR.strings.font_size_medium
        "large" -> MR.strings.font_size_large
        "xlarge" -> MR.strings.font_size_xlarge
        else -> MR.strings.common_unknown
    }.desc()

    // MARK: - Private Helper Methods

    private suspend fun loadUserPreferences() {
        // Silent fail for preferences loading
        runCatchingNonCancellation(onFailure = {}) {
            val preferences = authRepository.getUserPreferences().getOrDefault(UserPreferences())
            _uiState.update {
                it.copy(
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
                )
            }
        }
    }

    /**
     * Save the current state as the user's preferences, on viewModelScope
     * so leaving the screen can't drop the write. [failureMessage] names
     * the setting for an unexpected failure.
     */
    private fun persist(failureMessage: StringResource) {
        viewModelScope.launch { savePreferences(failureMessage) }
    }

    private suspend fun savePreferences(failureMessage: StringResource) {
        runCatchingNonCancellation(onFailure = { handleException(it, failureMessage) }) {
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

            val success = authRepository.updateUserPreferences(updatedPreferences).getOrThrow()
            if (success) {
                _uiState.update { it.copy(userPreferences = updatedPreferences) }
            } else {
                setError(
                    MR.strings.error_save_failed_title,
                    MR.strings.error_save_theme_failed,
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
        }
    }
}
