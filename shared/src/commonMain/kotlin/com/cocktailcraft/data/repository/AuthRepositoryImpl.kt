package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.repository.AuthRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class AuthRepositoryImpl(
    private val settings: Settings,
    private val json: Json
) : AuthRepository {

    // Authentication methods
    override suspend fun signUp(email: String, password: String): Flow<Boolean> = flow {
        try {
            // Check if user already exists
            if (getUserByEmail(email) != null) {
                emit(false)
                return@flow
            }

            // Create new user
            val user = User(
                id = UUID.randomUUID().toString(),
                email = email,
                name = "",
                preferences = emptyMap()
            )

            // Save user
            val users = getCurrentUsers().toMutableList()
            users.add(user)
            saveUsers(users)

            // Save credentials
            saveCredentials(email, password)

            // Set as current user
            settings.putString(CURRENT_USER_ID_KEY, user.id)

            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun signIn(email: String, password: String): Flow<Boolean> = flow {
        try {
            // Check credentials
            val storedPassword = settings.getStringOrNull("password_$email")
            if (storedPassword != password) {
                emit(false)
                return@flow
            }

            // Get user
            val user = getUserByEmail(email)
            if (user == null) {
                emit(false)
                return@flow
            }

            // Set as current user
            settings.putString(CURRENT_USER_ID_KEY, user.id)

            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun signOut(): Flow<Boolean> = flow {
        try {
            // Clear current user
            settings.remove(CURRENT_USER_ID_KEY)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun resetPassword(email: String): Flow<Boolean> = flow {
        try {
            // In a real app, this would send an email with a reset link
            // For this demo, we'll just reset the password to a default value
            val user = getUserByEmail(email)
            if (user == null) {
                emit(false)
                return@flow
            }

            // Reset password to "password123"
            saveCredentials(email, "password123")
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Flow<Boolean> = flow {
        try {
            val currentUser = getCurrentUserSync()
            if (currentUser == null) {
                emit(false)
                return@flow
            }

            // Check old password
            val storedPassword = settings.getStringOrNull("password_${currentUser.email}")
            if (storedPassword != oldPassword) {
                emit(false)
                return@flow
            }

            // Update password
            saveCredentials(currentUser.email, newPassword)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun isUserSignedIn(): Flow<Boolean> = flow {
        val currentUserId = settings.getStringOrNull(CURRENT_USER_ID_KEY)
        emit(currentUserId != null)
    }

    override suspend fun getCurrentUser(): Flow<User?> = flow {
        emit(getCurrentUserSync())
    }

    // Profile management methods
    override suspend fun updateUserName(name: String): Flow<Boolean> = flow {
        try {
            val currentUser = getCurrentUserSync()
            if (currentUser == null) {
                emit(false)
                return@flow
            }

            // Update user
            val updatedUser = currentUser.copy(name = name)
            updateUser(updatedUser)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun updateUserEmail(email: String, password: String): Flow<Boolean> = flow {
        try {
            val currentUser = getCurrentUserSync()
            if (currentUser == null) {
                emit(false)
                return@flow
            }

            // Check if email already exists
            if (getUserByEmail(email) != null && email != currentUser.email) {
                emit(false)
                return@flow
            }

            // Get old credentials
            val oldPassword = settings.getStringOrNull("password_${currentUser.email}")
            if (oldPassword != password) {
                emit(false)
                return@flow
            }

            // Remove old credentials
            settings.remove("password_${currentUser.email}")

            // Update user
            val updatedUser = currentUser.copy(email = email)
            updateUser(updatedUser)

            // Save new credentials
            saveCredentials(email, password)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun updateUserAddress(address: Address): Flow<Boolean> = flow {
        try {
            val currentUser = getCurrentUserSync()
            if (currentUser == null) {
                emit(false)
                return@flow
            }

            // Update user
            val updatedUser = currentUser.copy(address = address)
            updateUser(updatedUser)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    // Preferences management
    override suspend fun updateUserPreferences(preferences: UserPreferences): Flow<Boolean> = flow {
        try {
            val currentUser = getCurrentUserSync()
            if (currentUser == null) {
                emit(false)
                return@flow
            }

            // Convert UserPreferences to Map<String, String>
            val preferencesMap = mapOf(
                "darkMode" to preferences.darkMode.toString(),
                "notificationsEnabled" to preferences.notificationsEnabled.toString(),
                "language" to preferences.language
            )

            // Update user
            val updatedUser = currentUser.copy(preferences = preferencesMap)
            updateUser(updatedUser)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    override suspend fun getUserPreferences(): Flow<UserPreferences> = flow {
        val currentUser = getCurrentUserSync()
        if (currentUser != null) {
            // Convert Map<String, String> to UserPreferences
            val prefs = currentUser.preferences
            val darkMode = prefs["darkMode"]?.toBoolean() ?: false
            val notificationsEnabled = prefs["notificationsEnabled"]?.toBoolean() ?: true
            val language = prefs["language"] ?: "en"
            
            emit(UserPreferences(
                darkMode = darkMode,
                notificationsEnabled = notificationsEnabled,
                language = language
            ))
        } else {
            emit(UserPreferences())
        }
    }

    // Helper methods
    private fun getCurrentUserSync(): User? {
        val currentUserId = settings.getStringOrNull(CURRENT_USER_ID_KEY) ?: return null
        val users = getCurrentUsers()
        return users.find { it.id == currentUserId }
    }

    private fun getUserByEmail(email: String): User? {
        val users = getCurrentUsers()
        return users.find { it.email == email }
    }

    private fun updateUser(user: User) {
        val users = getCurrentUsers().toMutableList()
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
            saveUsers(users)
        }
    }

    private fun getCurrentUsers(): List<User> {
        val usersJson = settings.getStringOrNull(USERS_KEY) ?: "[]"
        return try {
            json.decodeFromString(usersJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveUsers(users: List<User>) {
        val usersJson = json.encodeToString(users)
        settings.putString(USERS_KEY, usersJson)
    }

    private fun saveCredentials(email: String, password: String) {
        settings.putString("password_$email", password)
    }

    companion object {
        private const val USERS_KEY = "users"
        private const val CURRENT_USER_ID_KEY = "current_user_id"
    }
} 
