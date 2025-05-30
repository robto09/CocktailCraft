package com.cocktailcraft.viewmodel

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
) : BaseViewModel() {
    
    // Use injected repository if not provided in constructor (for production)
    private val injectedAuthRepository: AuthRepository by inject()
    
    // Use the provided repository or the injected one
    private val repository: AuthRepository
        get() = authRepository ?: injectedAuthRepository
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()
    
    // Keep _errorString for backward compatibility
    private val _errorString = MutableStateFlow<String?>(null)
    val errorString: StateFlow<String?> = _errorString.asStateFlow()
    
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
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                repository.getCurrentUser().collect { user ->
                    _user.value = user
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to load profile: ${e.message}"
                handleException(e, "Failed to load profile")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                repository.signIn(email, password).collect { success ->
                    if (success) {
                        checkSignInStatus()
                        loadUserProfile()
                    } else {
                        _errorString.value = "Invalid email or password"
                        setError("Sign In Failed", "Invalid email or password")
                    }
                }
            } catch (e: Exception) {
                _errorString.value = "Sign in failed: ${e.message}"
                handleException(e, "Sign in failed")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                repository.signUp(email, password).collect { success ->
                    if (success) {
                        updateUserName(name)
                        checkSignInStatus()
                        loadUserProfile()
                    } else {
                        _errorString.value = "Sign up failed. Email may already be in use."
                        setError("Sign Up Failed", "Sign up failed. Email may already be in use.")
                    }
                }
            } catch (e: Exception) {
                _errorString.value = "Sign up failed: ${e.message}"
                handleException(e, "Sign up failed")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                repository.signOut().collect { success ->
                    if (success) {
                        _user.value = null
                        checkSignInStatus()
                    } else {
                        _errorString.value = "Sign out failed"
                        setError("Sign Out Failed", "Sign out failed")
                    }
                }
            } catch (e: Exception) {
                _errorString.value = "Sign out failed: ${e.message}"
                handleException(e, "Sign out failed")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun updateUserName(name: String) {
        viewModelScope.launch {
            setLoading(true)
            _errorString.value = null
            clearError()
            
            try {
                repository.updateUserName(name).collect { success ->
                    if (success) {
                        loadUserProfile()
                    } else {
                        _errorString.value = "Failed to update name"
                        setError("Update Failed", "Failed to update name")
                    }
                }
            } catch (e: Exception) {
                _errorString.value = "Failed to update name: ${e.message}"
                handleException(e, "Failed to update name")
            } finally {
                setLoading(false)
            }
        }
    }
    
    fun clearErrorString() {
        _errorString.value = null
    }
} 