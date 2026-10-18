package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.util.AuthInputValidator
import com.cocktailcraft.domain.util.getOrDefault
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.error_change_failed_title
import com.cocktailcraft.resources.error_change_password_check_current
import com.cocktailcraft.resources.error_change_password_failed
import com.cocktailcraft.resources.error_check_auth_failed
import com.cocktailcraft.resources.error_create_account_failed
import com.cocktailcraft.resources.error_current_password_required
import com.cocktailcraft.resources.error_invalid_credentials
import com.cocktailcraft.resources.error_invalid_email_message
import com.cocktailcraft.resources.error_invalid_email_title
import com.cocktailcraft.resources.error_invalid_name_title
import com.cocktailcraft.resources.error_invalid_password_title
import com.cocktailcraft.resources.error_name_required
import com.cocktailcraft.resources.error_password_policy
import com.cocktailcraft.resources.error_password_required
import com.cocktailcraft.resources.error_sign_in_failed
import com.cocktailcraft.resources.error_sign_in_failed_title
import com.cocktailcraft.resources.error_sign_out_failed
import com.cocktailcraft.resources.error_sign_out_failed_message
import com.cocktailcraft.resources.error_sign_out_failed_title
import com.cocktailcraft.resources.error_sign_up_failed_message
import com.cocktailcraft.resources.error_sign_up_failed_title
import com.cocktailcraft.resources.error_update_address_failed
import com.cocktailcraft.resources.error_update_failed_title
import com.cocktailcraft.resources.error_update_profile_failed
import com.cocktailcraft.resources.error_update_profile_info_failed
import com.cocktailcraft.resources.password_changed_message
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.viewmodel.state.ProfileUiState
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Shared ViewModel for Profile functionality.
 * Uses consolidated [ProfileUiState] for atomic state updates; [uiState] is
 * the only public state surface. The remaining synchronous helpers are pure
 * input validators (they take the input, never read state).
 */
class SharedProfileViewModel internal constructor(
    private val manageProfileUseCase: ManageProfileUseCase
) : SharedViewModel() {

    // Consolidated UI State
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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
            handleException(e, MR.strings.error_check_auth_failed)
        }
    }
    
    /**
     * Sign in with email and password.
     * SKIE will convert this to Swift async function.
     */
    suspend fun signIn(email: String, password: String): Boolean {
        clearError()
        // Sign-in stays lenient on purpose: accounts created under older,
        // looser rules must still be able to authenticate. Strict format
        // rules apply where the email is first stored (signUp/updateProfile).
        if (email.isBlank() || !email.contains('@')) {
            setError(
                MR.strings.error_invalid_email_title,
                MR.strings.error_invalid_email_message,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        // Like the email gate above: existing accounts may hold passwords set
        // under an older policy, so sign-in only rejects empty input.
        if (password.isBlank()) {
            setError(
                MR.strings.error_invalid_password_title,
                MR.strings.error_password_required,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        _uiState.update { it.copy(isAuthenticating = true, isLoading = true) }

        return try {
            val success = manageProfileUseCase.signIn(email, password).getOrThrow()
            if (success) {
                _uiState.update { it.copy(authStatus = "Sign In Successful") }
                checkAuthStatus()
            } else {
                _uiState.update { it.copy(authStatus = "Sign In Failed") }
                setError(
                    MR.strings.error_sign_in_failed_title,
                    MR.strings.error_invalid_credentials,
                    ErrorHandler.ErrorCategory.AUTHENTICATION
                )
            }
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            success
        } catch (e: Exception) {
            handleException(e, MR.strings.error_sign_in_failed)
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            false
        }
    }
    
    /**
     * Sign up with name, email and password.
     * SKIE will convert this to Swift async function.
     */
    suspend fun signUp(name: String, email: String, password: String): Boolean {
        clearError()
        if (name.isBlank()) {
            setError(
                MR.strings.error_invalid_name_title,
                MR.strings.error_name_required,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        if (!isValidEmail(email)) {
            setError(
                MR.strings.error_invalid_email_title,
                MR.strings.error_invalid_email_message,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        if (!meetsPasswordPolicy(password)) {
            setError(
                MR.strings.error_invalid_password_title,
                passwordPolicyMessage(),
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        _uiState.update { it.copy(isAuthenticating = true, isLoading = true) }

        return try {
            val success = manageProfileUseCase.signUp(email, password).getOrThrow()
            if (success) {
                _uiState.update { it.copy(authStatus = "Sign Up Successful") }
                updateProfile(name, email)
                checkAuthStatus()
            } else {
                _uiState.update { it.copy(authStatus = "Sign Up Failed") }
                setError(
                    MR.strings.error_sign_up_failed_title,
                    MR.strings.error_sign_up_failed_message,
                    ErrorHandler.ErrorCategory.AUTHENTICATION
                )
            }
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            success
        } catch (e: Exception) {
            handleException(e, MR.strings.error_create_account_failed)
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            false
        }
    }
    
    /**
     * Sign out current user.
     * SKIE will convert this to Swift async function.
     */
    suspend fun signOut(): Boolean {
        clearError()
        return try {
            val success = manageProfileUseCase.signOut().getOrThrow()
            if (success) {
                _uiState.update { it.copy(user = null, isLoggedIn = false, authStatus = "Signed Out") }
            } else {
                _uiState.update { it.copy(authStatus = "Sign Out Failed") }
                setError(
                    MR.strings.error_sign_out_failed_title,
                    MR.strings.error_sign_out_failed_message,
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
            success
        } catch (e: Exception) {
            handleException(e, MR.strings.error_sign_out_failed)
            false
        }
    }
    
    /**
     * Update user profile information.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateProfile(name: String, email: String): Boolean {
        clearError()
        if (name.isBlank()) {
            setError(
                MR.strings.error_invalid_name_title,
                MR.strings.error_name_required,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }
        
        if (!isValidEmail(email)) {
            setError(
                MR.strings.error_invalid_email_title,
                MR.strings.error_invalid_email_message,
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
                    MR.strings.error_update_failed_title,
                    MR.strings.error_update_profile_info_failed,
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
            nameSuccess
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_profile_failed)
            false
        }
    }
    
    /**
     * Update user address.
     * SKIE will convert this to Swift async function.
     */
    suspend fun updateAddress(address: Address): Boolean {
        clearError()
        return try {
            val success = manageProfileUseCase.updateUserAddress(address).getOrThrow()
            if (success) {
                loadCurrentUser()
            } else {
                setError(
                    MR.strings.error_update_failed_title,
                    MR.strings.error_update_address_failed,
                    ErrorHandler.ErrorCategory.SERVER
                )
            }
            success
        } catch (e: Exception) {
            handleException(e, MR.strings.error_update_address_failed)
            false
        }
    }
    
    /**
     * Change user password. Success is announced on [events] (it used to be
     * pushed through the error channel, so it rendered as an error alert).
     * SKIE will convert this to Swift async function.
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        clearError()
        // The old password may predate the current policy — only the new one
        // has to meet it.
        if (oldPassword.isBlank()) {
            setError(
                MR.strings.error_invalid_password_title,
                MR.strings.error_current_password_required,
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }

        if (!meetsPasswordPolicy(newPassword)) {
            setError(
                MR.strings.error_invalid_password_title,
                passwordPolicyMessage(),
                ErrorHandler.ErrorCategory.DATA
            )
            return false
        }


        return try {
            val success = manageProfileUseCase.changePassword(oldPassword, newPassword).getOrThrow()
            if (success) {
                sendEvent(UiEvent.Success(MR.strings.password_changed_message.desc()))
            } else {
                setError(
                    MR.strings.error_change_failed_title,
                    MR.strings.error_change_password_check_current,
                    ErrorHandler.ErrorCategory.AUTHENTICATION
                )
            }
            success
        } catch (e: Exception) {
            handleException(e, MR.strings.error_change_password_failed)
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

    private fun passwordPolicyMessage(): StringDesc =
        MR.strings.error_password_policy.format(AuthInputValidator.MIN_PASSWORD_LENGTH)
}