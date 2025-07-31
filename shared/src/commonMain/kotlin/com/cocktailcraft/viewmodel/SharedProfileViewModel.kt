package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.util.ErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Shared ViewModel for Profile functionality.
 * Designed for full SKIE interoperability with iOS.
 * 
 * Key SKIE features:
 * - StateFlows are automatically converted to Swift AsyncSequence
 * - Suspend functions become Swift async functions
 * - No FlowCollector bridge needed
 */
class SharedProfileViewModel : SharedViewModel() {
    
    private val repository: AuthRepository by inject()
    
    // User state - SKIE will convert these to Swift AsyncSequence
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _authStatus = MutableStateFlow("Unknown")
    val authStatus: StateFlow<String> = _authStatus.asStateFlow()
    
    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()
    
    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()
    
    // Computed properties
    val hasUser: Boolean
        get() = _user.value != null
    
    val isGuest: Boolean
        get() = !_isLoggedIn.value
    
    val userName: String
        get() = _user.value?.name ?: "Guest"
    
    
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
            repository.isUserSignedIn()
                .catch { e ->
                    handleException(e, "Failed to check authentication status")
                }
                .collect { signedIn ->
                    _isLoggedIn.value = signedIn
                    _authStatus.value = if (signedIn) "Authenticated" else "Not Authenticated"
                    if (signedIn) {
                        loadCurrentUser()
                    } else {
                        _user.value = null
                    }
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
        if (!isEmailValid(email)) {
            setError(
                "Invalid Email",
                "Please enter a valid email address",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        if (!isPasswordValid(password)) {
            setError(
                "Invalid Password",
                "Password must be at least 6 characters long",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        _isAuthenticating.value = true
        setLoading(true)
        
        return try {
            repository.signIn(email, password)
                .catch { e ->
                    handleException(e, "Failed to sign in")
                    _isAuthenticating.value = false
                    setLoading(false)
                }
                .collect { success ->
                    if (success) {
                        _authError.value = null
                        _authStatus.value = "Sign In Successful"
                        checkAuthStatus()
                    } else {
                        _authStatus.value = "Sign In Failed"
                        setError(
                            "Sign In Failed",
                            "Invalid email or password",
                            ErrorHandler.ErrorCategory.AUTHENTICATION,
                            showAsEvent = true
                        )
                    }
                    _isAuthenticating.value = false
                    setLoading(false)
                }
            true
        } catch (e: Exception) {
            handleException(e, "Failed to sign in", showAsEvent = true)
            _isAuthenticating.value = false
            setLoading(false)
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
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        if (!isEmailValid(email)) {
            setError(
                "Invalid Email",
                "Please enter a valid email address",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        if (!isPasswordValid(password)) {
            setError(
                "Invalid Password",
                "Password must be at least 6 characters long",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        _isAuthenticating.value = true
        setLoading(true)
        
        return try {
            repository.signUp(email, password)
                .catch { e ->
                    handleException(e, "Failed to create account")
                    _isAuthenticating.value = false
                    setLoading(false)
                }
                .collect { success ->
                    if (success) {
                        _authError.value = null
                        _authStatus.value = "Sign Up Successful"
                        // Update user name after successful signup
                        updateProfile(name, email)
                    } else {
                        _authStatus.value = "Sign Up Failed"
                        setError(
                            "Sign Up Failed",
                            "Failed to create account. Email may already be in use.",
                            ErrorHandler.ErrorCategory.AUTHENTICATION,
                            showAsEvent = true
                        )
                    }
                    _isAuthenticating.value = false
                    setLoading(false)
                }
            true
        } catch (e: Exception) {
            handleException(e, "Failed to create account", showAsEvent = true)
            _isAuthenticating.value = false
            setLoading(false)
            false
        }
    }
    
    /**
     * Sign out current user.
     * SKIE will convert this to Swift async function.
     */
    suspend fun signOut(): Boolean {
        setLoading(true)
        
        return try {
            repository.signOut()
                .catch { e ->
                    handleException(e, "Failed to sign out")
                    setLoading(false)
                }
                .collect { success ->
                    if (success) {
                        _user.value = null
                        _isLoggedIn.value = false
                        _authError.value = null
                        _authStatus.value = "Signed Out"
                    } else {
                        _authStatus.value = "Sign Out Failed"
                        setError(
                            "Sign Out Failed",
                            "Failed to sign out. Please try again.",
                            ErrorHandler.ErrorCategory.SERVER,
                            showAsEvent = true
                        )
                    }
                    setLoading(false)
                }
            true
        } catch (e: Exception) {
            handleException(e, "Failed to sign out", showAsEvent = true)
            setLoading(false)
            false
        }
    }
    
    /**
     * Reset password for given email.
     * SKIE will convert this to Swift async function.
     */
    suspend fun resetPassword(email: String): Boolean {
        if (!isEmailValid(email)) {
            setError(
                "Invalid Email",
                "Please enter a valid email address",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        setLoading(true)
        
        return try {
            repository.resetPassword(email)
                .catch { e ->
                    handleException(e, "Failed to send password reset email")
                    setLoading(false)
                }
                .collect { success ->
                    if (success) {
                        setError(
                            "Password Reset Sent",
                            "Check your email for password reset instructions",
                            ErrorHandler.ErrorCategory.UNKNOWN,
                            showAsEvent = true
                        )
                    } else {
                        setError(
                            "Reset Failed",
                            "Failed to send password reset email",
                            ErrorHandler.ErrorCategory.SERVER,
                            showAsEvent = true
                        )
                    }
                    setLoading(false)
                }
            true
        } catch (e: Exception) {
            handleException(e, "Failed to send password reset email", showAsEvent = true)
            setLoading(false)
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
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        if (!isEmailValid(email)) {
            setError(
                "Invalid Email",
                "Please enter a valid email address",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        setLoading(true)
        
        return try {
            // Update name first
            repository.updateUserName(name)
                .catch { e ->
                    handleException(e, "Failed to update profile")
                    setLoading(false)
                }
                .collect { nameSuccess ->
                    if (nameSuccess) {
                        // Reload user data
                        loadCurrentUser()
                    } else {
                        setError(
                            "Update Failed",
                            "Failed to update profile information",
                            ErrorHandler.ErrorCategory.SERVER,
                            showAsEvent = true
                        )
                    }
                    setLoading(false)
                }
            true
        } catch (e: Exception) {
            handleException(e, "Failed to update profile", showAsEvent = true)
            setLoading(false)
            false
        }
    }
    
    /**
     * Update user address.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateAddress(address: Address): Boolean {
        setLoading(true)
        
        return try {
            repository.updateUserAddress(address)
                .catch { e ->
                    handleException(e, "Failed to update address")
                    setLoading(false)
                }
                .collect { success ->
                    if (success) {
                        loadCurrentUser()
                    } else {
                        setError(
                            "Update Failed",
                            "Failed to update address",
                            ErrorHandler.ErrorCategory.SERVER,
                            showAsEvent = true
                        )
                    }
                    setLoading(false)
                }
            true
        } catch (e: Exception) {
            handleException(e, "Failed to update address", showAsEvent = true)
            setLoading(false)
            false
        }
    }
    
    /**
     * Change user password.
     * SKIE will convert this to Swift async function.
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        if (!isPasswordValid(oldPassword) || !isPasswordValid(newPassword)) {
            setError(
                "Invalid Password",
                "Passwords must be at least 6 characters long",
                ErrorHandler.ErrorCategory.DATA,
                showAsEvent = true
            )
            return false
        }
        
        setLoading(true)
        
        return try {
            repository.changePassword(oldPassword, newPassword)
                .catch { e ->
                    handleException(e, "Failed to change password")
                    setLoading(false)
                }
                .collect { success ->
                    if (success) {
                        setError(
                            "Password Changed",
                            "Your password has been updated successfully",
                            ErrorHandler.ErrorCategory.UNKNOWN,
                            showAsEvent = true
                        )
                    } else {
                        setError(
                            "Change Failed",
                            "Failed to change password. Check your current password.",
                            ErrorHandler.ErrorCategory.AUTHENTICATION,
                            showAsEvent = true
                        )
                    }
                    setLoading(false)
                }
            true
        } catch (e: Exception) {
            handleException(e, "Failed to change password", showAsEvent = true)
            setLoading(false)
            false
        }
    }
    
    // MARK: - Synchronous Helper Methods
    
    /**
     * Validate email format.
     */
    fun isEmailValid(email: String): Boolean {
        return email.isNotBlank() &&
               email.contains("@") &&
               email.contains(".") &&
               email.length >= 5
    }
    
    /**
     * Validate email format (alias for SKIE compatibility).
     */
    fun isValidEmail(email: String): Boolean {
        return isEmailValid(email)
    }
    
    /**
     * Validate password strength.
     */
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
    
    /**
     * Validate password strength (alias for SKIE compatibility).
     */
    fun isValidPassword(password: String): Boolean {
        return isPasswordValid(password)
    }
    
    /**
     * Get password strength score.
     */
    fun getPasswordStrength(password: String): Int {
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score
    }
    
    /**
     * Get display name for UI.
     */
    fun getDisplayName(): String {
        return _user.value?.name?.takeIf { it.isNotBlank() } ?: "User"
    }
    
    /**
     * Get user initials for avatar.
     */
    fun getInitials(): String {
        val name = _user.value?.name ?: return "U"
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercase()}${parts[1].firstOrNull()?.uppercase()}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "U"
        }
    }
    
    /**
     * Get user initials for avatar (alias for SKIE compatibility).
     */
    fun getUserInitials(): String {
        return getInitials()
    }
    
    /**
     * Check if user has complete profile.
     */
    fun hasCompleteProfile(): Boolean {
        val user = _user.value ?: return false
        return user.name.isNotBlank() && user.email.isNotBlank()
    }
    
    /**
     * Get user email.
     */
    fun getUserEmail(): String {
        return _user.value?.email ?: ""
    }
    
    /**
     * Check if email is verified.
     */
    fun isEmailVerified(): Boolean {
        return _user.value?.isEmailVerified ?: false
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
            repository.getCurrentUser()
                .catch { /* Silent fail */ }
                .collect { user ->
                    _user.value = user
                }
        } catch (e: Exception) {
            // Silent fail for user loading
        }
    }
    
    private suspend fun loadUserPreferences() {
        try {
            repository.getUserPreferences()
                .catch { /* Silent fail */ }
                .collect { preferences ->
                    _userPreferences.value = preferences
                }
        } catch (e: Exception) {
            // Silent fail for preferences loading
        }
    }
}