package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.usecase.ThemeUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * ViewModel for managing theme preferences.
 * Uses use cases instead of directly accessing repositories.
 */
class ThemeViewModel : BaseViewModel() {

    // Use cases
    private val themeUseCase: ThemeUseCase by inject()

    // Theme state
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

    /**
     * Load the user's theme preferences.
     */
    private fun loadThemePreference() {
        executeWithErrorHandling(
            operation = {
                themeUseCase.getUserPreferences()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val preferences = result.data
                                _followSystemTheme.value = preferences.followSystemTheme

                                // If following system theme, use system setting, otherwise use saved preference
                                _isDarkMode.value = if (preferences.followSystemTheme) {
                                    _isSystemInDarkMode.value
                                } else {
                                    preferences.darkMode
                                }
                            }
                            is Result.Error -> {
                                // If there's an error, default to system setting
                                _followSystemTheme.value = true
                                _isDarkMode.value = _isSystemInDarkMode.value
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to load theme preferences. Using system defaults.",
            showAsEvent = false
        )
    }

    /**
     * Toggle dark mode on/off.
     */
    fun toggleDarkMode() {
        // Only toggle if not following system theme
        if (!_followSystemTheme.value) {
            val currentValue = _isDarkMode.value
            val newValue = !currentValue

            executeWithErrorHandling(
                operation = {
                    themeUseCase.setDarkMode(newValue, false)
                },
                onSuccess = { resultFlow ->
                    viewModelScope.launch {
                        resultFlow.collect { result ->
                            when (result) {
                                is Result.Success -> {
                                    if (result.data) {
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
                                }
                                is Result.Error -> {
                                    // If there's an error, revert UI state
                                    _isDarkMode.value = currentValue
                                    setError(
                                        title = "Theme Update Failed",
                                        message = result.message,
                                        category = ErrorUtils.ErrorCategory.DATA
                                    )
                                }
                                is Result.Loading -> {
                                    // Already handled by executeWithErrorHandling
                                }
                            }
                        }
                    }
                },
                defaultErrorMessage = "Failed to update theme. Please try again.",
                showAsEvent = true
            )
        }
    }

    /**
     * Set dark mode to a specific value.
     */
    fun setDarkMode(enabled: Boolean) {
        if (_isDarkMode.value != enabled) {
            executeWithErrorHandling(
                operation = {
                    themeUseCase.setDarkMode(enabled, false)
                },
                onSuccess = { resultFlow ->
                    viewModelScope.launch {
                        resultFlow.collect { result ->
                            when (result) {
                                is Result.Success -> {
                                    if (result.data) {
                                        _isDarkMode.value = enabled
                                        _followSystemTheme.value = false
                                    } else {
                                        setError(
                                            title = "Theme Update Failed",
                                            message = "Failed to update dark mode setting",
                                            category = ErrorUtils.ErrorCategory.DATA
                                        )
                                    }
                                }
                                is Result.Error -> {
                                    setError(
                                        title = "Theme Update Failed",
                                        message = result.message,
                                        category = ErrorUtils.ErrorCategory.DATA
                                    )
                                }
                                is Result.Loading -> {
                                    // Already handled by executeWithErrorHandling
                                }
                            }
                        }
                    }
                },
                defaultErrorMessage = "Failed to update theme. Please try again.",
                showAsEvent = true
            )
        }
    }

    /**
     * Toggle whether to follow the system theme.
     */
    fun toggleFollowSystemTheme() {
        val currentValue = _followSystemTheme.value
        val newValue = !currentValue

        executeWithErrorHandling(
            operation = {
                themeUseCase.setFollowSystemTheme(newValue, _isSystemInDarkMode.value)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data) {
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
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Theme Update Failed",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.DATA
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to update theme settings. Please try again.",
            showAsEvent = true
        )
    }
}
