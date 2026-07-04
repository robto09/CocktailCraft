package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.ThemeMode
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.ThemeUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Theme functionality.
 * Uses consolidated [ThemeUiState] for atomic state updates.
 *
 * All theme mutations are value-based setters (never flip-style toggles),
 * so a re-fired UI callback can only re-assert the same state.
 */
class SharedThemeViewModel : SharedViewModel() {

    private val manageProfileUseCase: ManageProfileUseCase by inject()

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    val currentThemeName: String
        get() = getThemeModeDisplayName(themeModeToString(_uiState.value.themeMode))
    val availableThemes: List<String>
        get() = listOf("system", "light", "dark")
    val availableAccentColors: List<String>
        get() = listOf("blue", "green", "red", "orange", "purple", "pink")
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
            handleException(e, "Failed to update theme mode")
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
            handleException(e, "Failed to update accent color")
        }
    }

    suspend fun setFontSize(size: String) {
        try {
            _uiState.update { it.copy(fontSize = size) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to update font size")
        }
    }

    suspend fun toggleHighContrast() {
        try {
            _uiState.update { it.copy(isHighContrast = !it.isHighContrast) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to toggle high contrast")
        }
    }

    suspend fun toggleReducedMotion() {
        try {
            _uiState.update { it.copy(isReducedMotion = !it.isReducedMotion) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to toggle reduced motion")
        }
    }

    suspend fun resetToDefaults() {
        try {
            _uiState.update { ThemeUiState() }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to reset theme settings")
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
    fun getThemeModeDisplayName(mode: String): String {
        return when (mode.lowercase()) {
            "light" -> "Light"
            "dark" -> "Dark"
            "system" -> "System"
            else -> "Unknown"
        }
    }

    /**
     * Get accent color display name.
     */
    fun getAccentColorDisplayName(color: String): String {
        return color.replaceFirstChar { it.uppercase() }
    }

    /**
     * Get font size display name.
     */
    fun getFontSizeDisplayName(size: String): String {
        return when (size.lowercase()) {
            "small" -> "Small"
            "medium" -> "Medium"
            "large" -> "Large"
            "xlarge" -> "Extra Large"
            else -> "Unknown"
        }
    }

    /**
     * Check if current theme is custom.
     */
    fun isCustomTheme(): Boolean {
        return !_uiState.value.isSystemTheme
    }

    /**
     * Get theme preview colors.
     */
    fun getThemePreviewColors(mode: String): Map<String, String> {
        return when (mode.lowercase()) {
            "light" -> mapOf(
                "background" to "#FFFFFF",
                "surface" to "#F5F5F5",
                "primary" to "#007AFF",
                "text" to "#000000"
            )
            "dark" -> mapOf(
                "background" to "#000000",
                "surface" to "#1C1C1E",
                "primary" to "#0A84FF",
                "text" to "#FFFFFF"
            )
            else -> mapOf(
                "background" to "#FFFFFF",
                "surface" to "#F5F5F5",
                "primary" to "#007AFF",
                "text" to "#000000"
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

    /**
     * Export theme settings.
     */
    fun exportThemeSettings(): Map<String, Any> {
        val s = _uiState.value
        return mapOf(
            "isDarkMode" to s.isDarkMode,
            "themeMode" to themeModeToString(s.themeMode),
            "isSystemTheme" to s.isSystemTheme,
            "accentColor" to s.accentColor,
            "fontSize" to s.fontSize,
            "isHighContrast" to s.isHighContrast,
            "isReducedMotion" to s.isReducedMotion
        )
    }

    fun importThemeSettings(settings: Map<String, Any>) {
        viewModelScope.launch {
            try {
                _uiState.update { state ->
                    state.copy(
                        themeMode = (settings["themeMode"] as? String)
                            ?.let { ThemeMode.fromString(it) } ?: state.themeMode,
                        accentColor = (settings["accentColor"] as? String) ?: state.accentColor,
                        fontSize = (settings["fontSize"] as? String) ?: state.fontSize,
                        isHighContrast = (settings["isHighContrast"] as? Boolean) ?: state.isHighContrast,
                        isReducedMotion = (settings["isReducedMotion"] as? Boolean) ?: state.isReducedMotion
                    )
                }
                savePreferences()
            } catch (e: Exception) {
                handleException(e, "Failed to import theme settings")
            }
        }
    }

    // MARK: - Private Helper Methods

    private fun themeModeToString(mode: ThemeMode): String = when (mode) {
        ThemeMode.LIGHT -> "light"
        ThemeMode.DARK -> "dark"
        ThemeMode.SYSTEM -> "system"
    }

    private suspend fun loadUserPreferences() {
        try {
            val preferences = manageProfileUseCase.getUserPreferences().getOrDefault(UserPreferences())
            _uiState.update { it.copy(
                userPreferences = preferences,
                themeMode = when {
                    preferences.followSystemTheme -> ThemeMode.SYSTEM
                    preferences.darkMode -> ThemeMode.DARK
                    else -> ThemeMode.LIGHT
                }
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
                followSystemTheme = state.isSystemTheme
            )

            val success = manageProfileUseCase.updateUserPreferences(updatedPreferences).getOrDefault(false)
            if (success) {
                _uiState.update { it.copy(userPreferences = updatedPreferences) }
            } else {
                setError(
                    "Save Failed",
                    "Failed to save theme preferences",
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
        } catch (e: Exception) {
            handleException(e, "Failed to save theme preferences")
        }
    }
}
