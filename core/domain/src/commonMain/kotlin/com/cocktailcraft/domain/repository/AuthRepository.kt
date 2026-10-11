package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.util.Result

interface AuthRepository {
    // Authentication methods
    suspend fun signUp(email: String, password: String): Result<Boolean>
    suspend fun signIn(email: String, password: String): Result<Boolean>
    suspend fun signOut(): Result<Boolean>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean>
    suspend fun isUserSignedIn(): Result<Boolean>
    suspend fun getCurrentUser(): Result<User?>

    // Profile management methods
    suspend fun updateUserName(name: String): Result<Boolean>
    suspend fun updateUserEmail(email: String, password: String): Result<Boolean>
    suspend fun updateUserAddress(address: Address): Result<Boolean>

    // Preferences management
    suspend fun updateUserPreferences(preferences: UserPreferences): Result<Boolean>
    suspend fun getUserPreferences(): Result<UserPreferences>
}