package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.util.ErrorHandler

/**
 * Consolidated UI state for the Profile screen.
 */
data class ProfileUiState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val authStatus: String = "Unknown",
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val error: ErrorHandler.UserFriendlyError? = null
)

