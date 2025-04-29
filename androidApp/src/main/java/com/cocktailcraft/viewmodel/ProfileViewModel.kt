package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.usecase.AuthUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * ViewModel for the profile screen.
 * Uses use cases instead of directly accessing repositories.
 *
 * Following MVVM + Clean Architecture principles, this ViewModel:
 * - Depends on use cases, not repositories
 * - Manages UI state for user profile and authentication
 * - Handles user interactions like signing in/out and updating profile
 * - Provides a clean API for the UI layer
 * - Implements the IProfileViewModel interface for cross-platform compatibility
 */
class ProfileViewModel(
    private val authUseCase: AuthUseCase
) : BaseViewModel(), IProfileViewModel {

    // User state
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    init {
        checkSignInStatus()
        loadUserProfile()
    }

    /**
     * Check if the user is currently signed in.
     */
    private fun checkSignInStatus() {
        executeWithErrorHandling(
            operation = {
                authUseCase.isSignedIn()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _isSignedIn.value = result.data
                            }
                            is Result.Error -> {
                                // Don't show error for sign-in status check
                                _isSignedIn.value = false
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to check sign-in status.",
            showLoading = false,
            showAsEvent = false
        )
    }

    /**
     * Load the current user's profile.
     */
    private fun loadUserProfile() {
        executeWithErrorHandling(
            operation = {
                authUseCase.getCurrentUser()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                _user.value = result.data
                            }
                            is Result.Error -> {
                                // Only show error if signed in but can't load profile
                                if (_isSignedIn.value) {
                                    setError(
                                        title = "Failed to Load Profile",
                                        message = result.message,
                                        category = ErrorUtils.ErrorCategory.DATA,
                                        recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadUserProfile() }
                                    )
                                }
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to load user profile. Please try again.",
            recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadUserProfile() }
        )
    }

    /**
     * Sign in with email and password.
     */
    fun signIn(email: String, password: String) {
        executeWithErrorHandling(
            operation = {
                authUseCase.signIn(email, password)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data) {
                                    checkSignInStatus()
                                    loadUserProfile()
                                } else {
                                    setError(
                                        title = "Sign In Failed",
                                        message = "Invalid email or password",
                                        category = ErrorUtils.ErrorCategory.AUTHENTICATION
                                    )
                                }
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Sign In Failed",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.AUTHENTICATION
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to sign in. Please try again."
        )
    }

    /**
     * Sign up with name, email, and password.
     */
    fun signUp(name: String, email: String, password: String) {
        executeWithErrorHandling(
            operation = {
                authUseCase.signUp(email, password)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data) {
                                    updateUserName(name)
                                    checkSignInStatus()
                                    loadUserProfile()
                                } else {
                                    setError(
                                        title = "Sign Up Failed",
                                        message = "Email may already be in use",
                                        category = ErrorUtils.ErrorCategory.AUTHENTICATION
                                    )
                                }
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Sign Up Failed",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.AUTHENTICATION
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to sign up. Please try again."
        )
    }

    /**
     * Sign out the current user.
     */
    fun signOut() {
        executeWithErrorHandling(
            operation = {
                authUseCase.signOut()
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data) {
                                    _user.value = null
                                    checkSignInStatus()
                                } else {
                                    setError(
                                        title = "Sign Out Failed",
                                        message = "Failed to sign out",
                                        category = ErrorUtils.ErrorCategory.AUTHENTICATION
                                    )
                                }
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Sign Out Failed",
                                    message = result.message,
                                    category = ErrorUtils.ErrorCategory.AUTHENTICATION
                                )
                            }
                            is Result.Loading -> {
                                // Already handled by executeWithErrorHandling
                            }
                        }
                    }
                }
            },
            defaultErrorMessage = "Failed to sign out. Please try again."
        )
    }

    /**
     * Update the user's name.
     */
    fun updateUserName(name: String) {
        executeWithErrorHandling(
            operation = {
                authUseCase.updateUserName(name)
            },
            onSuccess = { resultFlow ->
                viewModelScope.launch {
                    resultFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                if (result.data) {
                                    loadUserProfile()
                                } else {
                                    setError(
                                        title = "Update Failed",
                                        message = "Failed to update name",
                                        category = ErrorUtils.ErrorCategory.DATA
                                    )
                                }
                            }
                            is Result.Error -> {
                                setError(
                                    title = "Update Failed",
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
            defaultErrorMessage = "Failed to update name. Please try again."
        )
    }
}