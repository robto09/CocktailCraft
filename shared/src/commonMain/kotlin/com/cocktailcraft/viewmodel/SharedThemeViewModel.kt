package com.cocktailcraft.viewmodel

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
 */
class SharedThemeViewModel : SharedViewModel() {

    private val manageProfileUseCase: ManageProfileUseCase by inject()

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    // Derived StateFlows for backward compatibility
    val isDarkMode: StateFlow<Boolean> = _uiState
        .map { it.isDarkMode }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val themeMode: StateFlow<String> = _uiState
        .map { it.themeMode }.stateIn(viewModelScope, SharingStarted.Eagerly, "system")
    val isSystemTheme: StateFlow<Boolean> = _uiState
        .map { it.isSystemTheme }.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val accentColor: StateFlow<String> = _uiState
        .map { it.accentColor }.stateIn(viewModelScope, SharingStarted.Eagerly, "blue")
    val fontSize: StateFlow<String> = _uiState
        .map { it.fontSize }.stateIn(viewModelScope, SharingStarted.Eagerly, "medium")
    val isHighContrast: StateFlow<Boolean> = _uiState
        .map { it.isHighContrast }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isReducedMotion: StateFlow<Boolean> = _uiState
        .map { it.isReducedMotion }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val userPreferences: StateFlow<UserPreferences> = _uiState
        .map { it.userPreferences }.stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val currentThemeName: String
        get() = getThemeModeDisplayName(_uiState.value.themeMode)
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
     * Set theme mode.
     * SKIE will convert this to Swift async function.
     */
    suspend fun setThemeMode(mode: String) {
        try {
            _uiState.update { state ->
                when (mode.lowercase()) {
                    "light" -> state.copy(themeMode = mode, isDarkMode = false, isSystemTheme = false)
                    "dark" -> state.copy(themeMode = mode, isDarkMode = true, isSystemTheme = false)
                    "system" -> state.copy(themeMode = mode, isSystemTheme = true)
                    else -> state.copy(themeMode = mode)
                }
            }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to update theme mode")
        }
    }

    suspend fun toggleDarkMode() {
        try {
            _uiState.update { it.copy(
                isDarkMode = !it.isDarkMode,
                isSystemTheme = false,
                themeMode = if (!it.isDarkMode) "dark" else "light"
            ) }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to toggle dark mode")
        }
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

    suspend fun applySystemTheme() {
        try {
            _uiState.update { state ->
                val newSystem = !state.isSystemTheme
                state.copy(
                    isSystemTheme = newSystem,
                    themeMode = if (newSystem) "system" else state.themeMode
                )
            }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to apply system theme")
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
            "themeMode" to s.themeMode,
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
                        isDarkMode = (settings["isDarkMode"] as? Boolean) ?: state.isDarkMode,
                        themeMode = (settings["themeMode"] as? String) ?: state.themeMode,
                        isSystemTheme = (settings["isSystemTheme"] as? Boolean) ?: state.isSystemTheme,
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

    private suspend fun loadUserPreferences() {
        try {
            val preferences = manageProfileUseCase.getUserPreferences().getOrDefault(UserPreferences())
            _uiState.update { it.copy(
                userPreferences = preferences,
                isDarkMode = preferences.darkMode,
                isSystemTheme = preferences.followSystemTheme,
                themeMode = when {
                    preferences.followSystemTheme -> "system"
                    preferences.darkMode -> "dark"
                    else -> "light"
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
                darkMode = state.isDarkMode,
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