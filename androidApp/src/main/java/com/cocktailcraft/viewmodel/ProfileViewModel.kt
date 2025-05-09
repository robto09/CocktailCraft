package com.cocktailcraft.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProfileViewModel(
    private val authRepository: AuthRepository? = null
) : ViewModel(), KoinComponent {
    
    // Use injected repository if not provided in constructor (for production)
    private val injectedAuthRepository: AuthRepository by inject()
    
    // Use the provided repository or the injected one
    private val repository: AuthRepository
        get() = authRepository ?: injectedAuthRepository
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        checkSignInStatus()
        loadUserProfile()
    }
    
    private fun checkSignInStatus() {
        viewModelScope.launch {
            repository.isUserSignedIn().collect { isSignedIn ->
                _isSignedIn.value = isSignedIn
            }
        }
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                repository.getCurrentUser().collect { user ->
                    _user.value = user
                }
            } catch (e: Exception) {
                _error.value = "Failed to load profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                repository.signIn(email, password).collect { success ->
                    if (success) {
                        checkSignInStatus()
                        loadUserProfile()
                    } else {
                        _error.value = "Invalid email or password"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Sign in failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                repository.signUp(email, password).collect { success ->
                    if (success) {
                        updateUserName(name)
                        checkSignInStatus()
                        loadUserProfile()
                    } else {
                        _error.value = "Sign up failed. Email may already be in use."
                    }
                }
            } catch (e: Exception) {
                _error.value = "Sign up failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                repository.signOut().collect { success ->
                    if (success) {
                        _user.value = null
                        checkSignInStatus()
                    } else {
                        _error.value = "Sign out failed"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Sign out failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateUserName(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                repository.updateUserName(name).collect { success ->
                    if (success) {
                        loadUserProfile()
                    } else {
                        _error.value = "Failed to update name"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update name: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 