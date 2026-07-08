package com.cocktailcraft.data.repository

import com.cocktailcraft.data.security.AuthStorageKeys
import com.cocktailcraft.data.security.PasswordHasher
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cocktailcraft.util.UUID
import kotlin.time.Clock

internal class AuthRepositoryImpl(
    private val settings: Settings,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    // Injectable for tests: coroutine virtual time does not move Clock.System.
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) : AuthRepository {

    // Authentication methods
    override suspend fun signUp(email: String, password: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            if (getUserByEmail(email) != null) return@withContext Result.Success(false)

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

    override suspend fun signIn(email: String, password: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val lockoutMs = lockoutRemainingMs(email)
            if (lockoutMs > 0) {
                // Must be an Error (not Success(false)) so the lockout message
                // reaches the UI instead of the generic invalid-credentials one.
                return@withContext Result.Error(
                    "Too many failed attempts. Try again in ${formatDuration(lockoutMs)}.",
                    ErrorCode.FORBIDDEN
                )
            }

            if (!verifyCredentials(email, password)) {
                recordFailedSignIn(email)
                return@withContext Result.Success(false)
            }

            val user = getUserByEmail(email) ?: return@withContext Result.Success(false)
            clearFailedSignIns(email)
            settings.putString(CURRENT_USER_ID_KEY, user.id)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sign in")
        }
    }

    override suspend fun signOut(): Result<Boolean> = withContext(ioDispatcher) {
        try {
            settings.remove(CURRENT_USER_ID_KEY)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sign out")
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val currentUser = getCurrentUserSync() ?: return@withContext Result.Success(false)
            if (!verifyCredentials(currentUser.email, oldPassword)) return@withContext Result.Success(false)
            saveCredentials(currentUser.email, newPassword)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to change password")
        }
    }

    override suspend fun isUserSignedIn(): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val currentUserId = settings.getStringOrNull(CURRENT_USER_ID_KEY)
            Result.Success(currentUserId != null)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to check sign-in status")
        }
    }

    override suspend fun getCurrentUser(): Result<User?> = withContext(ioDispatcher) {
        try {
            Result.Success(getCurrentUserSync())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get current user")
        }
    }

    // Profile management methods
    override suspend fun updateUserName(name: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val currentUser = getCurrentUserSync() ?: return@withContext Result.Success(false)
            val updatedUser = currentUser.copy(name = name)
            updateUser(updatedUser)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update user name")
        }
    }

    override suspend fun updateUserEmail(email: String, password: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val currentUser = getCurrentUserSync() ?: return@withContext Result.Success(false)
            if (getUserByEmail(email) != null && email != currentUser.email) return@withContext Result.Success(false)
            if (!verifyCredentials(currentUser.email, password)) return@withContext Result.Success(false)

            settings.remove(passwordKey(currentUser.email))
            val updatedUser = currentUser.copy(email = email)
            updateUser(updatedUser)
            saveCredentials(email, password)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update email")
        }
    }

    override suspend fun updateUserAddress(address: Address): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val currentUser = getCurrentUserSync() ?: return@withContext Result.Success(false)
            val updatedUser = currentUser.copy(address = address)
            updateUser(updatedUser)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update address")
        }
    }

    // Preferences management
    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val currentUser = getCurrentUserSync()
            if (currentUser == null) {
                // Guests keep preferences in a local store so theme and
                // settings choices survive relaunches without an account.
                settings.putString(GUEST_PREFERENCES_KEY, json.encodeToString(preferences))
                return@withContext Result.Success(true)
            }
            val preferencesMap = mapOf(
                "darkMode" to preferences.darkMode.toString(),
                "followSystemTheme" to preferences.followSystemTheme.toString(),
                "notificationsEnabled" to preferences.notificationsEnabled.toString(),
                "language" to preferences.language,
                "accentColor" to preferences.accentColor,
                "fontSize" to preferences.fontSize,
                "isHighContrast" to preferences.isHighContrast.toString(),
                "isReducedMotion" to preferences.isReducedMotion.toString()
            )
            val updatedUser = currentUser.copy(preferences = preferencesMap)
            updateUser(updatedUser)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update preferences")
        }
    }

    override suspend fun getUserPreferences(): Result<UserPreferences> = withContext(ioDispatcher) {
        try {
            val currentUser = getCurrentUserSync()
            if (currentUser != null) {
                val prefs = currentUser.preferences
                val defaults = UserPreferences()
                Result.Success(UserPreferences(
                    darkMode = prefs["darkMode"]?.toBoolean() ?: defaults.darkMode,
                    followSystemTheme = prefs["followSystemTheme"]?.toBoolean() ?: defaults.followSystemTheme,
                    notificationsEnabled = prefs["notificationsEnabled"]?.toBoolean() ?: defaults.notificationsEnabled,
                    language = prefs["language"] ?: defaults.language,
                    accentColor = prefs["accentColor"] ?: defaults.accentColor,
                    fontSize = prefs["fontSize"] ?: defaults.fontSize,
                    isHighContrast = prefs["isHighContrast"]?.toBoolean() ?: defaults.isHighContrast,
                    isReducedMotion = prefs["isReducedMotion"]?.toBoolean() ?: defaults.isReducedMotion
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
        settings.putString(passwordKey(email), PasswordHasher.hash(password))
    }

    private fun verifyCredentials(email: String, password: String): Boolean {
        val stored = settings.getStringOrNull(passwordKey(email)) ?: return false
        if (!PasswordHasher.verify(password, stored)) return false
        // Hashes from older formats upgrade to the current KDF on use.
        if (PasswordHasher.needsRehash(stored)) saveCredentials(email, password)
        return true
    }

    private fun passwordKey(email: String) = "${AuthStorageKeys.PASSWORD_PREFIX}$email"

    // Sign-in throttling: after FREE_SIGN_IN_ATTEMPTS consecutive failures the
    // email locks for BASE_LOCKOUT_MS, doubling per further failure up to
    // BASE_LOCKOUT_MS << MAX_LOCKOUT_DOUBLINGS. State clears on success.
    // Deadlines use the wall clock — the only persistent cross-platform time
    // source available here — so advancing the device clock skips one wait;
    // the attempt counter survives that, and the next failure re-locks with a
    // doubled duration, so backoff degrades rather than disappears.

    private fun lockoutRemainingMs(email: String): Long {
        val until = settings.getLongOrNull(lockoutUntilKey(email)) ?: return 0
        return until - nowMs()
    }

    private fun recordFailedSignIn(email: String) {
        val failures = settings.getInt(attemptsKey(email), 0) + 1
        settings.putInt(attemptsKey(email), failures)
        if (failures >= FREE_SIGN_IN_ATTEMPTS) {
            val doublings = minOf(failures - FREE_SIGN_IN_ATTEMPTS, MAX_LOCKOUT_DOUBLINGS)
            settings.putLong(lockoutUntilKey(email), nowMs() + (BASE_LOCKOUT_MS shl doublings))
        }
    }

    private fun clearFailedSignIns(email: String) {
        settings.remove(attemptsKey(email))
        settings.remove(lockoutUntilKey(email))
    }

    private fun formatDuration(ms: Long): String {
        val seconds = (ms + 999) / 1000
        return if (seconds < 60) "$seconds seconds" else "${(seconds + 59) / 60} minutes"
    }

    private fun attemptsKey(email: String) = "${AuthStorageKeys.SIGN_IN_ATTEMPTS_PREFIX}$email"

    private fun lockoutUntilKey(email: String) = "${AuthStorageKeys.SIGN_IN_LOCKOUT_PREFIX}$email"

    companion object {
        // Key names live in AuthStorageKeys so the platform storage migration
        // and this repository can never disagree about what is auth data.
        private const val USERS_KEY = AuthStorageKeys.USERS
        private const val CURRENT_USER_ID_KEY = AuthStorageKeys.CURRENT_USER_ID
        private const val GUEST_PREFERENCES_KEY = AuthStorageKeys.GUEST_PREFERENCES

        private const val FREE_SIGN_IN_ATTEMPTS = 5
        private const val BASE_LOCKOUT_MS = 30_000L
        private const val MAX_LOCKOUT_DOUBLINGS = 6 // caps at 32 minutes
    }
}
