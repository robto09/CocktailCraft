package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * ViewModel for managing theme preferences.
 * Supports both constructor injection (for testing) and Koin injection (for production).
 */
class ThemeViewModel(
    private val authRepository: AuthRepository? = null
) : BaseViewModel() {

    // Use injected repository if not provided in constructor (for production)
    private val injectedAuthRepository: AuthRepository by inject()

    // Use the provided repository or the injected one
    private val repository: AuthRepository
        get() = authRepository ?: injectedAuthRepository

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _followSystemTheme = MutableStateFlow(true)
    val followSystemTheme: StateFlow<Boolean> = _followSystemTheme.asStateFlow()

    // Current system dark mode state
    private val _isSystemInDarkMode = MutableStateFlow(false)

    init {
        loadThemePreference()
    }

    /**
     * Updates the system dark mode state
     */
    fun updateSystemDarkMode(isDark: Boolean) {
        _isSystemInDarkMode.value = isDark
        // If following system theme, update the dark mode state
        if (_followSystemTheme.value) {
            _isDarkMode.value = isDark
        }
    }

    private fun loadThemePreference() {
        viewModelScope.launch {
            setLoading(true)
            try {
                val preferences = repository.getUserPreferences().first()
                _followSystemTheme.value = preferences.followSystemTheme

                // If following system theme, use system setting, otherwise use saved preference
                _isDarkMode.value = if (preferences.followSystemTheme) {
                    _isSystemInDarkMode.value
                } else {
                    preferences.darkMode
                }
            } catch (e: Exception) {
                // If there's an error, default to system setting
                _followSystemTheme.value = true
                _isDarkMode.value = _isSystemInDarkMode.value
            } finally {
                setLoading(false)
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            // Only toggle if not following system theme
            if (!_followSystemTheme.value) {
                val currentValue = _isDarkMode.value
                val newValue = !currentValue
                _isDarkMode.value = newValue

                try {
                    // Get current preferences first
                    val currentPreferences = repository.getUserPreferences().first()

                    // Update with new dark mode value
                    val updatedPreferences = currentPreferences.copy(darkMode = newValue)
                    repository.updateUserPreferences(updatedPreferences)
                } catch (e: Exception) {
                    // If saving fails, revert the UI state
                    _isDarkMode.value = currentValue
                }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            if (_isDarkMode.value != enabled) {
                _isDarkMode.value = enabled

                try {
                    // Get current preferences first
                    val currentPreferences = repository.getUserPreferences().first()

                    // Update with new dark mode value and ensure follow system is disabled
                    val updatedPreferences = currentPreferences.copy(
                        darkMode = enabled,
                        followSystemTheme = false
                    )
                    repository.updateUserPreferences(updatedPreferences)
                    _followSystemTheme.value = false
                } catch (e: Exception) {
                    // If saving fails, revert the UI state
                    _isDarkMode.value = !enabled
                }
            }
        }
    }

    /**
     * Toggle whether to follow the system theme
     */
    fun toggleFollowSystemTheme() {
        viewModelScope.launch {
            val currentValue = _followSystemTheme.value
            val newValue = !currentValue
            _followSystemTheme.value = newValue

            try {
                // Get current preferences first
                val currentPreferences = repository.getUserPreferences().first()

                if (newValue) {
                    // If enabling follow system, update dark mode to match system
                    _isDarkMode.value = _isSystemInDarkMode.value

                    // Update preferences
                    val updatedPreferences = currentPreferences.copy(
                        followSystemTheme = newValue,
                        darkMode = _isSystemInDarkMode.value
                    )
                    repository.updateUserPreferences(updatedPreferences)
                } else {
                    // If disabling follow system, keep current dark mode
                    val updatedPreferences = currentPreferences.copy(
                        followSystemTheme = newValue
                    )
                    repository.updateUserPreferences(updatedPreferences)
                }
            } catch (e: Exception) {
                // If saving fails, revert the UI state
                _followSystemTheme.value = currentValue
            }
        }
    }
}
