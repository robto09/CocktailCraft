package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.usecase.ThemeUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing theme preferences.
 * Uses use cases instead of directly accessing repositories.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state for theme preferences (dark mode, system theme)
 * - Handles user interactions like toggling theme modes
 * - Provides a clean API for the UI layer
 * - Implements the IThemeViewModel interface for cross-platform compatibility
 */
class ThemeViewModel(
    private val themeUseCase: ThemeUseCase
) : BaseViewModel(), IThemeViewModel {

    // Theme state
    private val _isDarkMode = MutableStateFlow(false)
    override val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _followSystemTheme = MutableStateFlow(true)
    override val followSystemTheme: StateFlow<Boolean> = _followSystemTheme.asStateFlow()

    // Current system dark mode state
    private val _isSystemInDarkMode = MutableStateFlow(false)

    init {
        loadThemePreference()
    }

    /**
     * Updates the system dark mode state
     */
    override fun updateSystemDarkMode(isDark: Boolean) {
        _isSystemInDarkMode.value = isDark
        // If following system theme, update the dark mode state
        if (_followSystemTheme.value) {
            _isDarkMode.value = isDark
        }
    }

    /**
     * Load the user's theme preferences.
     */
    private fun loadThemePreference() {
        viewModelScope.launch {
            handleResultFlow(
                flow = themeUseCase.getUserPreferences(),
                onSuccess = { preferences ->
                    _followSystemTheme.value = preferences.followSystemTheme

                    // If following system theme, use system setting, otherwise use saved preference
                    _isDarkMode.value = if (preferences.followSystemTheme) {
                        _isSystemInDarkMode.value
                    } else {
                        preferences.darkMode
                    }
                },
                onError = { _ ->
                    // If there's an error, default to system setting
                    _followSystemTheme.value = true
                    _isDarkMode.value = _isSystemInDarkMode.value
                },
                defaultErrorMessage = "Failed to load theme preferences. Using system defaults.",
                showAsEvent = false
            )
        }
    }

    /**
     * Toggle dark mode on/off.
     */
    override fun toggleDarkMode() {
        // Only toggle if not following system theme
        if (!_followSystemTheme.value) {
            val currentValue = _isDarkMode.value
            val newValue = !currentValue

            viewModelScope.launch {
                handleResultFlow(
                    flow = themeUseCase.setDarkMode(newValue, false),
                    onSuccess = { success ->
                        if (success) {
                            _isDarkMode.value = newValue
                        } else {
                            // If operation failed, revert UI state
                            _isDarkMode.value = currentValue
                            setError(
                                title = "Theme Update Failed",
                                message = "Failed to update dark mode setting",
                                category = ErrorUtils.ErrorCategory.DATA
                            )
                        }
                    },
                    onError = { _ ->
                        // If there's an error, revert UI state
                        _isDarkMode.value = currentValue
                    },
                    defaultErrorMessage = "Failed to update theme. Please try again.",
                    showAsEvent = true
                )
            }
        }
    }

    /**
     * Set dark mode to a specific value.
     */
    override fun setDarkMode(enabled: Boolean) {
        if (_isDarkMode.value != enabled) {
            viewModelScope.launch {
                handleResultFlow(
                    flow = themeUseCase.setDarkMode(enabled, false),
                    onSuccess = { success ->
                        if (success) {
                            _isDarkMode.value = enabled
                            _followSystemTheme.value = false
                        } else {
                            setError(
                                title = "Theme Update Failed",
                                message = "Failed to update dark mode setting",
                                category = ErrorUtils.ErrorCategory.DATA
                            )
                        }
                    },
                    onError = { _ ->
                        // Error handling is done by handleResultFlow
                    },
                    defaultErrorMessage = "Failed to update theme. Please try again.",
                    showAsEvent = true
                )
            }
        }
    }

    /**
     * Toggle whether to follow the system theme.
     */
    override fun toggleFollowSystemTheme() {
        val currentValue = _followSystemTheme.value
        val newValue = !currentValue

        viewModelScope.launch {
            handleResultFlow(
                flow = themeUseCase.setFollowSystemTheme(newValue, _isSystemInDarkMode.value),
                onSuccess = { success ->
                    if (success) {
                        _followSystemTheme.value = newValue

                        // If enabling follow system, update dark mode to match system
                        if (newValue) {
                            _isDarkMode.value = _isSystemInDarkMode.value
                        }
                    } else {
                        setError(
                            title = "Theme Update Failed",
                            message = "Failed to update system theme setting",
                            category = ErrorUtils.ErrorCategory.DATA
                        )
                    }
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to update theme settings. Please try again.",
                showAsEvent = true
            )
        }
    }
}
