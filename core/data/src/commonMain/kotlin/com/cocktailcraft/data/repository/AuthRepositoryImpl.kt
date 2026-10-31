package com.cocktailcraft.data.repository

import com.cocktailcraft.data.security.AuthStorageKeys
import com.cocktailcraft.data.security.PasswordHasher
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.util.UUID
import com.cocktailcraft.util.runCatchingResult
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * Local account store in [Settings].
 *
 * Emails are matched case-insensitively and ignoring surrounding spaces
 * ([normalizeEmail]); new accounts store the normalized form. Accounts
 * created before that keep their stored spelling — and the per-email
 * password/throttle keys derived from it — so they still sign in under any
 * casing.
 *
 * Every write runs under [mutex]: the users list (and the sign-in attempt
 * counters) are read-modify-write over Settings, so two concurrent writers
 * would otherwise drop each other's update. Plain reads skip the lock so they
 * never queue behind a slow password hash.
 */
internal class AuthRepositoryImpl(
    private val settings: Settings,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    // Injectable for tests: coroutine virtual time does not move Clock.System.
    private val nowMs: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) : AuthRepository {

    private val mutex = Mutex()

    /** Runs [block] on [ioDispatcher] under [mutex], converting failures to Result.Error. */
    private suspend fun <T> locked(defaultMessage: String, block: () -> Result<T>): Result<T> =
        withContext(ioDispatcher) {
            mutex.withLock { runCatchingResult(defaultMessage, block) }
        }

    /** A read-only [block] on [ioDispatcher], converting failures to Result.Error. */
    private suspend fun <T> reading(defaultMessage: String, block: () -> Result<T>): Result<T> =
        withContext(ioDispatcher) { runCatchingResult(defaultMessage, block) }

    // Authentication methods
    override suspend fun signUp(email: String, password: String): Result<Boolean> =
        locked("Failed to sign up") {
            if (getUserByEmail(email) != null) return@locked Result.Success(false)

            val user = User(
                id = UUID.randomUUID(),
                email = normalizeEmail(email),
                name = "",
                preferences = emptyMap()
            )

            val users = getCurrentUsers().toMutableList()
            users.add(user)
            saveUsers(users)
            saveCredentials(user.email, password)
            settings.putString(CURRENT_USER_ID_KEY, user.id)
            Result.Success(true)
        }

    override suspend fun signIn(email: String, password: String): Result<Boolean> =
        locked("Failed to sign in") {
            val user = getUserByEmail(email)
            // Throttle state is keyed by the account's stored email, so every
            // casing of it shares one counter
            val throttleEmail = user?.email ?: normalizeEmail(email)

            lockoutError(throttleEmail)?.let { return@locked it }

            if (user == null || !verifyCredentials(user.email, password)) {
                if (user == null) makeRoomToThrottleUnknownEmail(throttleEmail)
                recordFailedSignIn(throttleEmail)
                return@locked Result.Success(false)
            }

            clearFailedSignIns(throttleEmail)
            settings.putString(CURRENT_USER_ID_KEY, user.id)
            Result.Success(true)
        }

    override suspend fun signOut(): Result<Boolean> =
        locked("Failed to sign out") {
            settings.remove(CURRENT_USER_ID_KEY)
            Result.Success(true)
        }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> =
        locked("Failed to change password") {
            val currentUser = getCurrentUserSync() ?: return@locked Result.Success(false)
            // Checking the current password is a password guess like any
            // sign-in (an unlocked phone left signed in), so it shares the
            // account's sign-in counter and lockout.
            lockoutError(currentUser.email)?.let { return@locked it }
            if (!verifyCredentials(currentUser.email, oldPassword)) {
                recordFailedSignIn(currentUser.email)
                return@locked Result.Success(false)
            }
            clearFailedSignIns(currentUser.email)
            saveCredentials(currentUser.email, newPassword)
            Result.Success(true)
        }

    override suspend fun isUserSignedIn(): Result<Boolean> =
        reading("Failed to check sign-in status") {
            val currentUserId = settings.getStringOrNull(CURRENT_USER_ID_KEY)
            Result.Success(currentUserId != null)
        }

    override suspend fun getCurrentUser(): Result<User?> =
        reading("Failed to get current user") {
            Result.Success(getCurrentUserSync())
        }

    // Profile management methods
    override suspend fun updateUserName(name: String): Result<Boolean> =
        locked("Failed to update user name") {
            val currentUser = getCurrentUserSync() ?: return@locked Result.Success(false)
            val updatedUser = currentUser.copy(name = name)
            updateUser(updatedUser)
            Result.Success(true)
        }

    // Preferences management
    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Boolean> =
        locked("Failed to update preferences") {
            val currentUser = getCurrentUserSync()
            if (currentUser == null) {
                // Guests keep preferences in a local store so theme and
                // settings choices survive relaunches without an account.
                settings.putString(GUEST_PREFERENCES_KEY, json.encodeToString(preferences))
                return@locked Result.Success(true)
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
        }

    override suspend fun getUserPreferences(): Result<UserPreferences> =
        reading("Failed to get user preferences") {
            val currentUser = getCurrentUserSync()
            if (currentUser != null) {
                val prefs = currentUser.preferences
                val defaults = UserPreferences()
                Result.Success(
                    UserPreferences(
                        darkMode = prefs["darkMode"]?.toBoolean() ?: defaults.darkMode,
                        followSystemTheme = prefs["followSystemTheme"]?.toBoolean() ?: defaults.followSystemTheme,
                        notificationsEnabled =
                        prefs["notificationsEnabled"]?.toBoolean() ?: defaults.notificationsEnabled,
                        language = prefs["language"] ?: defaults.language,
                        accentColor = prefs["accentColor"] ?: defaults.accentColor,
                        fontSize = prefs["fontSize"] ?: defaults.fontSize,
                        isHighContrast = prefs["isHighContrast"]?.toBoolean() ?: defaults.isHighContrast,
                        isReducedMotion = prefs["isReducedMotion"]?.toBoolean() ?: defaults.isReducedMotion
                    )
                )
            } else {
                val stored = settings.getStringOrNull(GUEST_PREFERENCES_KEY)
                Result.Success(
                    stored?.let { json.decodeFromString<UserPreferences>(it) } ?: UserPreferences()
                )
            }
        }

    // Helper methods (callers hold [mutex])
    private fun getCurrentUserSync(): User? {
        val currentUserId = settings.getStringOrNull(CURRENT_USER_ID_KEY) ?: return null
        val users = getCurrentUsers()
        return users.find { it.id == currentUserId }
    }

    /**
     * The account for [email], matched after [normalizeEmail]. An exact match
     * on the stored spelling wins, in case an older install holds accounts
     * that differ only by case.
     */
    private fun getUserByEmail(email: String): User? {
        val users = getCurrentUsers()
        val normalized = normalizeEmail(email)
        return users.find { it.email == email.trim() }
            ?: users.find { normalizeEmail(it.email) == normalized }
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

    // [email] in the credential/throttle helpers below is the account's
    // STORED email (see the class doc), never raw user input.

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

    // Sign-in throttling: after FREE_SIGN_IN_ATTEMPTS consecutive failures
    // (sign-ins and change-password checks of the current password count
    // alike) the email locks for BASE_LOCKOUT_MS, doubling per further failure up to
    // BASE_LOCKOUT_MS << MAX_LOCKOUT_DOUBLINGS. State clears on success.
    // Deadlines use the wall clock — the only persistent cross-platform time
    // source available here — so advancing the device clock skips one wait;
    // the attempt counter survives that, and the next failure re-locks with a
    // doubled duration, so backoff degrades rather than disappears.

    private fun lockoutRemainingMs(email: String): Long {
        val until = settings.getLongOrNull(lockoutUntilKey(email)) ?: return 0
        return until - nowMs()
    }

    /**
     * The error to return while [email] is locked out, or null. An Error (not
     * Success(false)) so the lockout message reaches the UI instead of the
     * generic invalid-credentials one; the UI words it from [Result.Error.retryAfterMs].
     */
    private fun lockoutError(email: String): Result.Error? {
        val lockoutMs = lockoutRemainingMs(email)
        if (lockoutMs <= 0) return null
        return Result.Error(
            message = "Locked for ${lockoutMs}ms after repeated failed password attempts",
            code = ErrorCode.FORBIDDEN,
            retryAfterMs = lockoutMs
        )
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

    /**
     * Emails with no account are throttled too — skipping them would let a
     * caller tell accounts apart by which addresses ever lock — but each one
     * costs secure-store entries (a Keychain item apiece on iOS), so made-up
     * addresses must not grow the store without bound. Before [email] gets
     * its first entry, at most MAX_UNKNOWN_THROTTLED_EMAILS - 1 other unknown
     * emails keep theirs: the least protective go first (no or expired
     * lockout, then the soonest deadline). Accounts' entries are never
     * evicted, so flooding made-up addresses cannot reset a real lockout.
     */
    private fun makeRoomToThrottleUnknownEmail(email: String) {
        if (settings.hasKey(attemptsKey(email))) return
        val accountEmails = getCurrentUsers().mapTo(HashSet()) { it.email }
        val unknown = throttledEmails() - accountEmails
        val excess = unknown.size - (MAX_UNKNOWN_THROTTLED_EMAILS - 1)
        if (excess <= 0) return
        unknown.sortedBy { settings.getLongOrNull(lockoutUntilKey(it)) ?: 0L }
            .take(excess)
            .forEach(::clearFailedSignIns)
    }

    /** Every email that currently has throttle state, account or not. */
    private fun throttledEmails(): Set<String> =
        settings.keys.mapNotNullTo(HashSet()) { key ->
            when {
                key.startsWith(AuthStorageKeys.SIGN_IN_ATTEMPTS_PREFIX) ->
                    key.removePrefix(AuthStorageKeys.SIGN_IN_ATTEMPTS_PREFIX)

                key.startsWith(AuthStorageKeys.SIGN_IN_LOCKOUT_PREFIX) ->
                    key.removePrefix(AuthStorageKeys.SIGN_IN_LOCKOUT_PREFIX)

                else -> null
            }
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

        /** Most emails without an account that keep throttle state at once. */
        internal const val MAX_UNKNOWN_THROTTLED_EMAILS = 20

        /** Canonical form for matching and storing emails: trimmed, lowercase. */
        internal fun normalizeEmail(email: String): String = email.trim().lowercase()
    }
}
