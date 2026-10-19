package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
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
import com.cocktailcraft.resources.error_update_failed_title
import com.cocktailcraft.resources.error_update_profile_failed
import com.cocktailcraft.resources.error_update_profile_info_failed
import com.cocktailcraft.resources.password_changed_message
import com.cocktailcraft.util.ErrorHandler
import com.cocktailcraft.util.runCatchingNonCancellation
import com.cocktailcraft.viewmodel.state.ProfileUiState
import dev.icerock.moko.resources.StringResource
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
    // Direct repository dependency (AR-8): there is no logic to wrap — the
    // input rules live here, the auth rules in the repository
    private val authRepository: AuthRepository
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

    /** Check current authentication status. */
    private suspend fun checkAuthStatus() {
        runCatchingNonCancellation(onFailure = { handleException(it, MR.strings.error_check_auth_failed) }) {
            val signedIn = authRepository.isUserSignedIn().getOrThrow()
            _uiState.update {
                it.copy(
                    isLoggedIn = signedIn,
                    authStatus = if (signedIn) "Authenticated" else "Not Authenticated"
                )
            }
            if (signedIn) {
                loadCurrentUser()
            } else {
                _uiState.update { it.copy(user = null) }
            }
        }
    }

    /**
     * Sign in with email and password.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun signIn(email: String, password: String): Boolean =
        runInViewModelScope { attemptSignIn(email, password) }

    private suspend fun attemptSignIn(email: String, password: String): Boolean {
        clearError()
        val inputValid = when {
            // Sign-in stays lenient on purpose: accounts created under older,
            // looser rules must still be able to authenticate. Strict format
            // rules apply where the email is first stored (signUp/updateProfile).
            email.isBlank() || !email.contains('@') -> rejectInvalidEmail()

            // Like the email gate above: existing accounts may hold passwords
            // set under an older policy, so sign-in only rejects empty input.
            password.isBlank() -> rejectPassword(MR.strings.error_password_required.desc())

            else -> true
        }
        if (!inputValid) return false

        _uiState.update { it.copy(isAuthenticating = true, isLoading = true) }

        return runCatchingNonCancellation(onFailure = {
            handleException(it, MR.strings.error_sign_in_failed)
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            false
        }) {
            val success = authRepository.signIn(email, password).getOrThrow()
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
        }
    }

    /**
     * Sign up with name, email and password.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun signUp(name: String, email: String, password: String): Boolean =
        runInViewModelScope { attemptSignUp(name, email, password) }

    private suspend fun attemptSignUp(name: String, email: String, password: String): Boolean {
        clearError()
        val inputValid = profileInputValid(name, email) &&
            (meetsPasswordPolicy(password) || rejectPassword(passwordPolicyMessage()))
        if (!inputValid) return false

        _uiState.update { it.copy(isAuthenticating = true, isLoading = true) }

        return runCatchingNonCancellation(onFailure = {
            handleException(it, MR.strings.error_create_account_failed)
            _uiState.update { it.copy(isAuthenticating = false, isLoading = false) }
            false
        }) {
            val success = authRepository.signUp(email, password).getOrThrow()
            if (success) {
                _uiState.update { it.copy(authStatus = "Sign Up Successful") }
                saveProfile(name, email)
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
        }
    }

    /**
     * Sign out current user.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun signOut(): Boolean = runInViewModelScope { attemptSignOut() }

    private suspend fun attemptSignOut(): Boolean {
        clearError()
        return runCatchingNonCancellation(onFailure = {
            handleException(it, MR.strings.error_sign_out_failed)
            false
        }) {
            val success = authRepository.signOut().getOrThrow()
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
        }
    }

    /**
     * Update user profile information.
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun updateProfile(name: String, email: String): Boolean = runInViewModelScope { saveProfile(name, email) }

    private suspend fun saveProfile(name: String, email: String): Boolean {
        clearError()
        if (!profileInputValid(name, email)) return false

        return runCatchingNonCancellation(onFailure = {
            handleException(it, MR.strings.error_update_profile_failed)
            false
        }) {
            val nameSuccess = authRepository.updateUserName(name).getOrThrow()
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
        }
    }

    /**
     * Change user password. Success is announced on [events] (it used to be
     * pushed through the error channel, so it rendered as an error alert).
     * SKIE will convert this to Swift async function.
     */
    @Throws(Exception::class)
    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean =
        runInViewModelScope { savePassword(oldPassword, newPassword) }

    private suspend fun savePassword(oldPassword: String, newPassword: String): Boolean {
        clearError()
        val inputValid = when {
            // The old password may predate the current policy — only the new
            // one has to meet it.
            oldPassword.isBlank() -> rejectPassword(MR.strings.error_current_password_required.desc())

            !meetsPasswordPolicy(newPassword) -> rejectPassword(passwordPolicyMessage())

            else -> true
        }
        if (!inputValid) return false

        return runCatchingNonCancellation(onFailure = {
            handleException(it, MR.strings.error_change_password_failed)
            false
        }) {
            val success = authRepository.changePassword(oldPassword, newPassword).getOrThrow()
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
        // Silent fail for user loading
        runCatchingNonCancellation(onFailure = {}) {
            val loadedUser = authRepository.getCurrentUser().getOrThrow()
            _uiState.update { it.copy(user = loadedUser) }
        }
    }

    private suspend fun loadUserPreferences() {
        // Silent fail for preferences loading
        runCatchingNonCancellation(onFailure = {}) {
            val prefs = authRepository.getUserPreferences().getOrDefault(UserPreferences())
            _uiState.update { it.copy(preferences = prefs) }
        }
    }

    /** A name and an email as stored on sign-up/profile edits; reports the first problem. */
    private fun profileInputValid(name: String, email: String): Boolean = when {
        name.isBlank() -> rejectInput(MR.strings.error_invalid_name_title, MR.strings.error_name_required.desc())
        !isValidEmail(email) -> rejectInvalidEmail()
        else -> true
    }

    private fun rejectInvalidEmail(): Boolean =
        rejectInput(MR.strings.error_invalid_email_title, MR.strings.error_invalid_email_message.desc())

    private fun rejectPassword(message: StringDesc): Boolean =
        rejectInput(MR.strings.error_invalid_password_title, message)

    /** Reports invalid form input on [error]; always false, for the validators above. */
    private fun rejectInput(title: StringResource, message: StringDesc): Boolean {
        setError(title, message, ErrorHandler.ErrorCategory.DATA)
        return false
    }

    private fun passwordPolicyMessage(): StringDesc =
        MR.strings.error_password_policy.format(AuthInputValidator.MIN_PASSWORD_LENGTH)
}