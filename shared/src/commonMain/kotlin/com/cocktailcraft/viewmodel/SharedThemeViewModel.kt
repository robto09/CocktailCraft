package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Theme functionality.
 * Designed for full SKIE interoperability with iOS.
 * 
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedThemeViewModel : SharedViewModel() {
    
    private val repository: AuthRepository by inject()
    
    // Theme state - SKIE will convert these to Swift AsyncSequence
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
    
    private val _isSystemTheme = MutableStateFlow(true)
    val isSystemTheme: StateFlow<Boolean> = _isSystemTheme.asStateFlow()
    
    private val _accentColor = MutableStateFlow("blue")
    val accentColor: StateFlow<String> = _accentColor.asStateFlow()
    
    private val _fontSize = MutableStateFlow("medium")
    val fontSize: StateFlow<String> = _fontSize.asStateFlow()
    
    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()
    
    private val _isReducedMotion = MutableStateFlow(false)
    val isReducedMotion: StateFlow<Boolean> = _isReducedMotion.asStateFlow()
    
    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()
    
    // Computed properties
    val currentThemeName: String
        get() = getThemeModeDisplayName(_themeMode.value)
    
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
            _themeMode.value = mode
            when (mode.lowercase()) {
                "light" -> {
                    _isDarkMode.value = false
                    _isSystemTheme.value = false
                }
                "dark" -> {
                    _isDarkMode.value = true
                    _isSystemTheme.value = false
                }
                "system" -> {
                    _isSystemTheme.value = true
                }
            }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to update theme mode", showAsEvent = true)
        }
    }
    
    /**
     * Toggle dark mode.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleDarkMode() {
        try {
            _isDarkMode.value = !_isDarkMode.value
            _isSystemTheme.value = false
            _themeMode.value = if (_isDarkMode.value) "dark" else "light"
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to toggle dark mode", showAsEvent = true)
        }
    }
    
    /**
     * Set accent color.
     * SKIE will convert this to Swift async function.
     */
    suspend fun setAccentColor(color: String) {
        try {
            _accentColor.value = color
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to update accent color", showAsEvent = true)
        }
    }
    
    /**
     * Set font size.
     * SKIE will convert this to Swift async function.
     */
    suspend fun setFontSize(size: String) {
        try {
            _fontSize.value = size
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to update font size", showAsEvent = true)
        }
    }
    
    /**
     * Toggle high contrast mode.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleHighContrast() {
        try {
            _isHighContrast.value = !_isHighContrast.value
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to toggle high contrast", showAsEvent = true)
        }
    }
    
    /**
     * Toggle reduced motion.
     * SKIE will convert this to Swift async function.
     */
    suspend fun toggleReducedMotion() {
        try {
            _isReducedMotion.value = !_isReducedMotion.value
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to toggle reduced motion", showAsEvent = true)
        }
    }
    
    /**
     * Reset theme to defaults.
     * SKIE will convert this to Swift async function.
     */
    suspend fun resetToDefaults() {
        try {
            _isDarkMode.value = false
            _themeMode.value = "system"
            _isSystemTheme.value = true
            _accentColor.value = "blue"
            _fontSize.value = "medium"
            _isHighContrast.value = false
            _isReducedMotion.value = false
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to reset theme settings", showAsEvent = true)
        }
    }
    
    /**
     * Apply system theme.
     * SKIE will convert this to Swift async function.
     */
    suspend fun applySystemTheme() {
        try {
            _isSystemTheme.value = !_isSystemTheme.value
            if (_isSystemTheme.value) {
                _themeMode.value = "system"
            }
            savePreferences()
        } catch (e: Exception) {
            handleException(e, "Failed to apply system theme", showAsEvent = true)
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
        return !_isSystemTheme.value
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
        return mapOf(
            "isDarkMode" to _isDarkMode.value,
            "themeMode" to _themeMode.value,
            "isSystemTheme" to _isSystemTheme.value,
            "accentColor" to _accentColor.value,
            "fontSize" to _fontSize.value,
            "isHighContrast" to _isHighContrast.value,
            "isReducedMotion" to _isReducedMotion.value
        )
    }
    
    /**
     * Import theme settings.
     */
    fun importThemeSettings(settings: Map<String, Any>) {
        viewModelScope.launch {
            try {
                settings["isDarkMode"]?.let { _isDarkMode.value = it as Boolean }
                settings["themeMode"]?.let { _themeMode.value = it as String }
                settings["isSystemTheme"]?.let { _isSystemTheme.value = it as Boolean }
                settings["accentColor"]?.let { _accentColor.value = it as String }
                settings["fontSize"]?.let { _fontSize.value = it as String }
                settings["isHighContrast"]?.let { _isHighContrast.value = it as Boolean }
                settings["isReducedMotion"]?.let { _isReducedMotion.value = it as Boolean }
                savePreferences()
            } catch (e: Exception) {
                handleException(e, "Failed to import theme settings", showAsEvent = true)
            }
        }
    }
    
    // MARK: - Private Helper Methods
    
    private suspend fun loadUserPreferences() {
        try {
            repository.getUserPreferences()
                .catch { /* Silent fail */ }
                .collect { preferences ->
                    _userPreferences.value = preferences
                    
                    // Apply loaded preferences
                    _isDarkMode.value = preferences.darkMode
                    _isSystemTheme.value = preferences.followSystemTheme
                    
                    // Determine current theme mode
                    _themeMode.value = when {
                        preferences.followSystemTheme -> "system"
                        preferences.darkMode -> "dark"
                        else -> "light"
                    }
                }
        } catch (e: Exception) {
            // Silent fail for preferences loading
        }
    }
    
    private suspend fun savePreferences() {
        try {
            val updatedPreferences = _userPreferences.value.copy(
                darkMode = _isDarkMode.value,
                followSystemTheme = _isSystemTheme.value
            )
            
            repository.updateUserPreferences(updatedPreferences)
                .catch { e ->
                    handleException(e, "Failed to save theme preferences")
                }
                .collect { success ->
                    if (success) {
                        _userPreferences.value = updatedPreferences
                    } else {
                        setError(
                            "Save Failed",
                            "Failed to save theme preferences",
                            ErrorHandler.ErrorCategory.SERVER,
                            showAsEvent = true
                        )
                    }
                }
        } catch (e: Exception) {
            handleException(e, "Failed to save theme preferences", showAsEvent = true)
        }
    }
}