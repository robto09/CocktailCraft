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
    override val user: StateFlow<User?> = _user.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    override val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    init {
        checkSignInStatus()
        loadUserProfile()
    }

    /**
     * Check if the user is currently signed in.
     */
    private fun checkSignInStatus() {
        viewModelScope.launch {
            handleResultFlow(
                flow = authUseCase.isSignedIn(),
                onSuccess = { isSignedIn ->
                    _isSignedIn.value = isSignedIn
                },
                onError = { _ ->
                    // Don't show error for sign-in status check
                    _isSignedIn.value = false
                },
                defaultErrorMessage = "Failed to check sign-in status.",
                showLoading = false,
                showAsEvent = false
            )
        }
    }

    /**
     * Load the current user's profile.
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            handleResultFlow(
                flow = authUseCase.getCurrentUser(),
                onSuccess = { user ->
                    _user.value = user
                },
                onError = { _ ->
                    // Only show error if signed in but can't load profile
                    if (!_isSignedIn.value) {
                        // Suppress error if not signed in
                        return@handleResultFlow
                    }
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to load user profile. Please try again.",
                recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadUserProfile() }
            )
        }
    }

    /**
     * Sign in with email and password.
     */
    override fun signIn(email: String, password: String) {
        viewModelScope.launch {
            handleResultFlow(
                flow = authUseCase.signIn(email, password),
                onSuccess = { success ->
                    if (success) {
                        checkSignInStatus()
                        loadUserProfile()
                    } else {
                        setError(
                            title = "Sign In Failed",
                            message = "Invalid email or password",
                            category = ErrorUtils.ErrorCategory.AUTHENTICATION
                        )
                    }
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to sign in. Please try again."
            )
        }
    }

    /**
     * Sign up with name, email, and password.
     */
    override fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            handleResultFlow(
                flow = authUseCase.signUp(email, password),
                onSuccess = { success ->
                    if (success) {
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
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to sign up. Please try again."
            )
        }
    }

    /**
     * Sign out the current user.
     */
    override fun signOut() {
        viewModelScope.launch {
            handleResultFlow(
                flow = authUseCase.signOut(),
                onSuccess = { success ->
                    if (success) {
                        _user.value = null
                        checkSignInStatus()
                    } else {
                        setError(
                            title = "Sign Out Failed",
                            message = "Failed to sign out",
                            category = ErrorUtils.ErrorCategory.AUTHENTICATION
                        )
                    }
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to sign out. Please try again."
            )
        }
    }

    /**
     * Update the user's name.
     */
    override fun updateUserName(name: String) {
        viewModelScope.launch {
            handleResultFlow(
                flow = authUseCase.updateUserName(name),
                onSuccess = { success ->
                    if (success) {
                        loadUserProfile()
                    } else {
                        setError(
                            title = "Update Failed",
                            message = "Failed to update name",
                            category = ErrorUtils.ErrorCategory.DATA
                        )
                    }
                },
                onError = { _ ->
                    // Error handling is done by handleResultFlow
                },
                defaultErrorMessage = "Failed to update name. Please try again."
            )
        }
    }
}