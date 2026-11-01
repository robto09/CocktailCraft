package com.cocktailcraft.viewmodel.state

import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences

/**
 * Consolidated UI state for the Profile screen.
 */
data class ProfileUiState(
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    // Diagnostic status for tests/logging, never displayed — so plain
    // English rather than a shared string
    val authStatus: String = "Unknown",
    val isAuthenticating: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
)
