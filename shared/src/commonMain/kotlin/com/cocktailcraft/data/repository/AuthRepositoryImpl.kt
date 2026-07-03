package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.util.Result
import com.russhwolf.settings.Settings
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cocktailcraft.util.UUID

internal class AuthRepositoryImpl(
    private val settings: Settings,
    private val json: Json
) : AuthRepository {

    // Authentication methods
    override suspend fun signUp(email: String, password: String): Result<Boolean> {
        return try {
            if (getUserByEmail(email) != null) return Result.Success(false)

            val user = User(
                id = UUID.randomUUID(),
                email = email,
                name = "",
                preferences = emptyMap()
            )

            val users = getCurrentUsers().toMutableList()
            users.add(user)
            saveUsers(users)
            saveCredentials(email, password)
            settings.putString(CURRENT_USER_ID_KEY, user.id)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sign up")
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Boolean> {
        return try {
            val storedPassword = settings.getStringOrNull("password_$email")
            if (storedPassword != password) return Result.Success(false)

            val user = getUserByEmail(email) ?: return Result.Success(false)
            settings.putString(CURRENT_USER_ID_KEY, user.id)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sign in")
        }
    }

    override suspend fun signOut(): Result<Boolean> {
        return try {
            settings.remove(CURRENT_USER_ID_KEY)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sign out")
        }
    }

    override suspend fun resetPassword(email: String): Result<Boolean> {
        return try {
            val user = getUserByEmail(email) ?: return Result.Success(false)
            saveCredentials(email, "password123")
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to reset password")
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> {
        return try {
            val currentUser = getCurrentUserSync() ?: return Result.Success(false)
            val storedPassword = settings.getStringOrNull("password_${currentUser.email}")
            if (storedPassword != oldPassword) return Result.Success(false)
            saveCredentials(currentUser.email, newPassword)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to change password")
        }
    }

    override suspend fun isUserSignedIn(): Result<Boolean> {
        return try {
            val currentUserId = settings.getStringOrNull(CURRENT_USER_ID_KEY)
            Result.Success(currentUserId != null)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to check sign-in status")
        }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            Result.Success(getCurrentUserSync())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get current user")
        }
    }

    // Profile management methods
    override suspend fun updateUserName(name: String): Result<Boolean> {
        return try {
            val currentUser = getCurrentUserSync() ?: return Result.Success(false)
            val updatedUser = currentUser.copy(name = name)
            updateUser(updatedUser)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update user name")
        }
    }

    override suspend fun updateUserEmail(email: String, password: String): Result<Boolean> {
        return try {
            val currentUser = getCurrentUserSync() ?: return Result.Success(false)
            if (getUserByEmail(email) != null && email != currentUser.email) return Result.Success(false)
            val oldPassword = settings.getStringOrNull("password_${currentUser.email}")
            if (oldPassword != password) return Result.Success(false)

            settings.remove("password_${currentUser.email}")
            val updatedUser = currentUser.copy(email = email)
            updateUser(updatedUser)
            saveCredentials(email, password)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update email")
        }
    }

    override suspend fun updateUserAddress(address: Address): Result<Boolean> {
        return try {
            val currentUser = getCurrentUserSync() ?: return Result.Success(false)
            val updatedUser = currentUser.copy(address = address)
            updateUser(updatedUser)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update address")
        }
    }

    // Preferences management
    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Boolean> {
        return try {
            val currentUser = getCurrentUserSync()
            if (currentUser == null) {
                // Guests keep preferences in a local store so theme and
                // settings choices survive relaunches without an account.
                settings.putString(GUEST_PREFERENCES_KEY, json.encodeToString(preferences))
                return Result.Success(true)
            }
            val preferencesMap = mapOf(
                "darkMode" to preferences.darkMode.toString(),
                "followSystemTheme" to preferences.followSystemTheme.toString(),
                "notificationsEnabled" to preferences.notificationsEnabled.toString(),
                "language" to preferences.language
            )
            val updatedUser = currentUser.copy(preferences = preferencesMap)
            updateUser(updatedUser)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update preferences")
        }
    }

    override suspend fun getUserPreferences(): Result<UserPreferences> {
        return try {
            val currentUser = getCurrentUserSync()
            if (currentUser != null) {
                val prefs = currentUser.preferences
                val darkMode = prefs["darkMode"]?.toBoolean() ?: false
                val followSystemTheme = prefs["followSystemTheme"]?.toBoolean() ?: true
                val notificationsEnabled = prefs["notificationsEnabled"]?.toBoolean() ?: true
                val language = prefs["language"] ?: "en"
                Result.Success(UserPreferences(
                    darkMode = darkMode,
                    followSystemTheme = followSystemTheme,
                    notificationsEnabled = notificationsEnabled,
                    language = language
                ))
            } else {
                val stored = settings.getStringOrNull(GUEST_PREFERENCES_KEY)
                Result.Success(
                    stored?.let { json.decodeFromString<UserPreferences>(it) } ?: UserPreferences()
                )
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get user preferences")
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
        private const val GUEST_PREFERENCES_KEY = "guest_preferences"
    }
}
