package com.coffee.store.domain.repository

import com.coffee.store.domain.model.User
import com.coffee.store.domain.model.UserPreferences
import com.coffee.store.domain.model.Address
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