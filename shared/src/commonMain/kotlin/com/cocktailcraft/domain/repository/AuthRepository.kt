package com.cocktailcraft.domain.repository

import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.model.Address
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Authentication methods
    suspend fun signUp(email: String, password: String): Flow<Boolean>
    suspend fun signIn(email: String, password: String): Flow<Boolean>
    suspend fun signOut(): Flow<Boolean>
    suspend fun resetPassword(email: String): Flow<Boolean>
    suspend fun changePassword(oldPassword: String, newPassword: String): Flow<Boolean>
    suspend fun isUserSignedIn(): Flow<Boolean>
    suspend fun getCurrentUser(): Flow<User?>
    
    // Profile management methods
    suspend fun updateUserName(name: String): Flow<Boolean>
    suspend fun updateUserEmail(email: String, password: String): Flow<Boolean>
    suspend fun updateUserAddress(address: Address): Flow<Boolean>
    
    // Preferences management
    suspend fun updateUserPreferences(preferences: UserPreferences): Flow<Boolean>
    suspend fun getUserPreferences(): Flow<UserPreferences>
} 