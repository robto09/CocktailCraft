package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ThemeViewModel(
    private val authRepository: AuthRepository? = null
) : ViewModel(), KoinComponent {
    
    // Use injected repository if not provided in constructor (for production)
    private val injectedAuthRepository: AuthRepository by inject()
    
    // Use the provided repository or the injected one
    private val repository: AuthRepository
        get() = authRepository ?: injectedAuthRepository
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadThemePreference()
    }
    
    private fun loadThemePreference() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val preferences = repository.getUserPreferences().first()
                _isDarkMode.value = preferences.darkMode
            } catch (e: Exception) {
                // If there's an error, default to system setting
                _isDarkMode.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
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
    
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            if (_isDarkMode.value != enabled) {
                _isDarkMode.value = enabled
                
                try {
                    // Get current preferences first
                    val currentPreferences = repository.getUserPreferences().first()
                    
                    // Update with new dark mode value
                    val updatedPreferences = currentPreferences.copy(darkMode = enabled)
                    repository.updateUserPreferences(updatedPreferences)
                } catch (e: Exception) {
                    // If saving fails, revert the UI state
                    _isDarkMode.value = !enabled
                }
            }
        }
    }
}
