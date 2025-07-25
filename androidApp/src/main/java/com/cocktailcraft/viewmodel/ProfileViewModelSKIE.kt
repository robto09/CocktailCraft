package com.cocktailcraft.viewmodel

import androidx.lifecycle.viewModelScope
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.viewmodel.SharedProfileViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android ViewModel wrapper for SharedProfileViewModel using SKIE.
 * This replaces the old ProfileViewModel with a thin wrapper around the shared implementation.
 * 
 * Key SKIE benefits:
 * - StateFlows work directly without conversion
 * - Suspend functions work with Android coroutines
 * - Shared business logic reduces code duplication
 * - Type safety preserved across platforms
 */
class ProfileViewModelSKIE : BaseViewModel(), KoinComponent {
    
    // Inject the shared ViewModel
    private val sharedViewModel: SharedProfileViewModel by inject()
    
    // Expose StateFlows from shared ViewModel (SKIE handles the interop)
    val user: StateFlow<User?> = sharedViewModel.user
    val isLoggedIn: StateFlow<Boolean> = sharedViewModel.isLoggedIn
    val authStatus: StateFlow<String> = sharedViewModel.authStatus
    
    // Expose loading and error from shared base class
    val loadingState: StateFlow<Boolean> = sharedViewModel.isLoading
    val errorState = sharedViewModel.error
    val errorEventState = sharedViewModel.errorEvent
    
    // Computed properties
    val isGuest: Boolean
        get() = sharedViewModel.isGuest
    
    val userName: String
        get() = sharedViewModel.userName
    
    val userEmail: String
        get() = sharedViewModel.getUserEmail()
    
    // MARK: - Public Methods (SKIE converts these to proper suspend functions)
    
    /**
     * Sign in with email and password using SKIE async interop
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            sharedViewModel.signIn(email, password)
        }
    }
    
    /**
     * Sign up with name, email and password using SKIE async interop
     */
    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            sharedViewModel.signUp(name, email, password)
        }
    }
    
    /**
     * Sign out using SKIE async interop
     */
    fun signOut() {
        viewModelScope.launch {
            sharedViewModel.signOut()
        }
    }
    
    /**
     * Update user profile using SKIE async interop
     */
    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            sharedViewModel.updateProfile(name, email)
        }
    }
    
    /**
     * Change password using SKIE async interop
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            sharedViewModel.changePassword(currentPassword, newPassword)
        }
    }
    
    /**
     * Reset password using SKIE async interop
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            sharedViewModel.resetPassword(email)
        }
    }
    
    // MARK: - Synchronous Methods (direct delegation)
    
    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return sharedViewModel.isValidEmail(email)
    }
    
    /**
     * Validate password strength
     */
    fun isValidPassword(password: String): Boolean {
        return sharedViewModel.isValidPassword(password)
    }
    
    /**
     * Get password strength score
     */
    fun getPasswordStrength(password: String): Int {
        return sharedViewModel.getPasswordStrength(password)
    }
    
    /**
     * Get user initials for avatar
     */
    fun getUserInitials(): String {
        return sharedViewModel.getUserInitials()
    }
    
    /**
     * Check if user has completed profile
     */
    fun hasCompleteProfile(): Boolean {
        return sharedViewModel.hasCompleteProfile()
    }
    
    /**
     * Clear the current error
     */
    fun clearErrorState() {
        sharedViewModel.clearError()
    }
    
    /**
     * Refresh user data
     */
    fun refresh() {
        sharedViewModel.refresh()
    }
    
    /**
     * Check authentication status
     */
    fun checkAuthStatus() {
        viewModelScope.launch {
            sharedViewModel.checkAuthStatus()
        }
    }
    
    /**
     * Clean up when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        sharedViewModel.onCleared()
    }
}