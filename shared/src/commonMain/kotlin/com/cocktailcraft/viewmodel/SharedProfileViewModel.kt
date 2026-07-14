package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.util.AuthInputValidator
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.ProfileUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Profile functionality.
 * Uses consolidated [ProfileUiState] for atomic state updates.
 */
class SharedProfileViewModel internal constructor(
    private val manageProfileUseCase: ManageProfileUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val hasUser: Boolean
        get() = _uiState.value.user != null
    val isGuest: Boolean
        get() = !_uiState.value.isLoggedIn
    val userName: String
        get() = _uiState.value.user?.name ?: "Guest"

    init {
        initialize()
    }
    
    private fun initialize() {
        viewModelScope.launch {
            // Check if user is already signed in
            checkAuthStatus()
            
            // Load user preferences
            loadUserPreferences()
        }
    }
    
    /**
     * Check current authentication status.
     * SKIE will convert this to Swift async function.
     */
    suspend fun checkAuthStatus() {
        try {
            val signedIn = manageProfileUseCase.isUserSignedIn().getOrThrow()
            _uiState.update { it.copy(
                isLoggedIn = signedIn,
                authStatus = if (signedIn) "Authenticated" else "Not Authenticated"
            ) }
            if (signedIn) {
                loadCurrentUser()
            } else {
                _uiState.update { it.copy(user = null) }
            }
        } catch (e: Exception) {
            handleException(e, "Failed to check authentication status")
        }
    }
    
    /**
     * Sign in with email and password.
     * SKIE will convert this to Swift async function.
     */
    suspend fun signIn(email: String, password: String): Boolean {
        // Sign-in stays lenient on purpose: accounts created under older,
        // looser rules must still be able to authenticate. Strict format
        // rules apply where the email is first stored (signUp/updateProfile).
        if (email.isBlank() || !email.contains('@')) {
            setError(
                "Invalid Email",
                "Please enter a valid email address",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        // Like the email gate above: existing accounts may hold passwords set
        // under an older policy, so sign-in only rejects empty input.
        if (password.isBlank()) {
            setError(
                "Invalid Password",
                "Please enter your password",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        _uiState.update { it.copy(isAuthenticating = true, isLoading = true) }

        return try {
            val success = manageProfileUseCase.signIn(email, password).getOrThrow()
            if (success) {
                _uiState.update { it.copy(authError = null, authStatus = "Sign In Successful") }
                checkAuthStatus()
            } else {
                _uiState.update { it.copy(authStatus = "Sign In Failed") }
                setError(
                    "Sign In Failed",
                    "Invalid email or password",
                    ErrorHandler.ErrorCategory.AUTHENTICATION
                )
            }
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            success
        } catch (e: Exception) {
            handleException(e, "Failed to sign in")
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            false
        }
    }
    
    /**
     * Sign up with name, email and password.
     * SKIE will convert this to Swift async function.
     */
    suspend fun signUp(name: String, email: String, password: String): Boolean {
        if (name.isBlank()) {
            setError(
                "Invalid Name",
                "Please enter your name",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        if (!isValidEmail(email)) {
            setError(
                "Invalid Email",
                "Please enter a valid email address",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        if (!meetsPasswordPolicy(password)) {
            setError(
                "Invalid Password",
                PASSWORD_POLICY_MESSAGE,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        _uiState.update { it.copy(isAuthenticating = true, isLoading = true) }

        return try {
            val success = manageProfileUseCase.signUp(email, password).getOrThrow()
            if (success) {
                _uiState.update { it.copy(authError = null, authStatus = "Sign Up Successful") }
                updateProfile(name, email)
                checkAuthStatus()
            } else {
                _uiState.update { it.copy(authStatus = "Sign Up Failed") }
                setError(
                    "Sign Up Failed",
                    "Failed to create account. Email may already be in use.",
                    ErrorHandler.ErrorCategory.AUTHENTICATION
                )
            }
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            success
        } catch (e: Exception) {
            handleException(e, "Failed to create account")
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            false
        }
    }
    
    /**
     * Sign out current user.
     * SKIE will convert this to Swift async function.
     */
    suspend fun signOut(): Boolean {
        
        return try {
            val success = manageProfileUseCase.signOut().getOrThrow()
            if (success) {
                _uiState.update { it.copy(user = null, isLoggedIn = false, authError = null, authStatus = "Signed Out") }
            } else {
                _uiState.update { it.copy(authStatus = "Sign Out Failed") }
                setError(
                    "Sign Out Failed",
                    "Failed to sign out. Please try again.",
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
            success
        } catch (e: Exception) {
            handleException(e, "Failed to sign out")
            false
        }
    }
    
    /**
     * Update user profile information.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateProfile(name: String, email: String): Boolean {
        if (name.isBlank()) {
            setError(
                "Invalid Name",
                "Please enter your name",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        if (!isValidEmail(email)) {
            setError(
                "Invalid Email",
                "Please enter a valid email address",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        
        return try {
            val nameSuccess = manageProfileUseCase.updateUserName(name).getOrThrow()
            if (nameSuccess) {
                loadCurrentUser()
            } else {
                setError(
                    "Update Failed",
                    "Failed to update profile information",
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
            nameSuccess
        } catch (e: Exception) {
            handleException(e, "Failed to update profile")
            false
        }
    }
    
    /**
     * Update user address.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateAddress(address: Address): Boolean {
        
        return try {
            val success = manageProfileUseCase.updateUserAddress(address).getOrThrow()
            if (success) {
                loadCurrentUser()
            } else {
                setError(
                    "Update Failed",
                    "Failed to update address",
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
            success
        } catch (e: Exception) {
            handleException(e, "Failed to update address")
            false
        }
    }
    
    /**
     * Change user password.
     * SKIE will convert this to Swift async function.
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        // The old password may predate the current policy — only the new one
        // has to meet it.
        if (oldPassword.isBlank()) {
            setError(
                "Invalid Password",
                "Please enter your current password",
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        if (!meetsPasswordPolicy(newPassword)) {
            setError(
                "Invalid Password",
                PASSWORD_POLICY_MESSAGE,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }


        return try {
            val success = manageProfileUseCase.changePassword(oldPassword, newPassword).getOrThrow()
            if (success) {
                setError(
                    "Password Changed",
                    "Your password has been updated successfully",
                    ErrorHandler.ErrorCategory.UNKNOWN
                )
            } else {
                setError(
                    "Change Failed",
                    "Failed to change password. Check your current password.",
                    ErrorHandler.ErrorCategory.AUTHENTICATION
                )
            }
            success
        } catch (e: Exception) {
            handleException(e, "Failed to change password")
            false
        }
    }
    
    // MARK: - Synchronous Helper Methods

    /**
     * Validate email format.
     */
    fun isValidEmail(email: String): Boolean = AuthInputValidator.isValidEmail(email)

    /**
     * Validate password strength.
     */
    fun isValidPassword(password: String): Boolean = AuthInputValidator.isValidPassword(password)

    /**
     * Full policy check for a newly set password (length + strength).
     */
    fun meetsPasswordPolicy(password: String): Boolean = AuthInputValidator.meetsPasswordPolicy(password)

    /**
     * Get password strength score.
     */
    fun getPasswordStrength(password: String): Int = AuthInputValidator.passwordStrength(password)

    /**
     * Get display name for UI.
     */
    fun getDisplayName(): String {
        return _uiState.value.user?.name?.takeIf { it.isNotBlank() } ?: "User"
    }

    /**
     * Get user initials for avatar.
     */
    fun getUserInitials(): String {
        val name = _uiState.value.user?.name ?: return "U"
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercase()}${parts[1].firstOrNull()?.uppercase()}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "U"
        }
    }
    
    /**
     * Check if user has complete profile.
     */
    fun hasCompleteProfile(): Boolean {
        val user = _uiState.value.user ?: return false
        return user.name.isNotBlank() && user.email.isNotBlank()
    }
    
    /**
     * Get user email.
     */
    fun getUserEmail(): String {
        return _uiState.value.user?.email ?: ""
    }

    fun isEmailVerified(): Boolean {
        return _uiState.value.user?.isEmailVerified ?: false
    }
    
    /**
     * Refresh profile data.
     */
    fun refresh() {
        viewModelScope.launch {
            checkAuthStatus()
            loadUserPreferences()
        }
    }
    
    // MARK: - Private Helper Methods
    
    private suspend fun loadCurrentUser() {
        try {
            val loadedUser = manageProfileUseCase.getCurrentUser().getOrThrow()
            _uiState.update { it.copy(user = loadedUser) }
        } catch (e: Exception) {
            // Silent fail for user loading
        }
    }

    private suspend fun loadUserPreferences() {
        try {
            val prefs = manageProfileUseCase.getUserPreferences().getOrDefault(UserPreferences())
            _uiState.update { it.copy(preferences = prefs) }
        } catch (e: Exception) {
            // Silent fail for preferences loading
        }
    }

    private companion object {
        val PASSWORD_POLICY_MESSAGE =
            "Password must be at least ${AuthInputValidator.MIN_PASSWORD_LENGTH} characters " +
                "and mix at least two of: uppercase letters, lowercase letters, numbers, symbols"
    }
}