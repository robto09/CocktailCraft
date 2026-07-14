package com.cocktailcraft.data.security

import com.russhwolf.settings.Settings

/**
 * Every Settings key the auth store owns. AuthRepositoryImpl writes these to
 * the dedicated secure store (EncryptedSharedPreferences on Android, the
 * Keychain on iOS); the platform modules use [migrateAuthStorage] to move
 * values written by older app versions out of the plain store.
 */
internal object AuthStorageKeys {
    const val USERS = "users"
    const val CURRENT_USER_ID = "current_user_id"
    const val GUEST_PREFERENCES = "guest_preferences"
    const val PASSWORD_PREFIX = "password_"
    const val SIGN_IN_ATTEMPTS_PREFIX = "signin_attempts_"
    const val SIGN_IN_LOCKOUT_PREFIX = "signin_lockout_until_"

    private val fixedKeys = setOf(USERS, CURRENT_USER_ID, GUEST_PREFERENCES)
    private val prefixes = listOf(PASSWORD_PREFIX, SIGN_IN_ATTEMPTS_PREFIX, SIGN_IN_LOCKOUT_PREFIX)

    fun isAuthKey(key: String): Boolean = key in fixedKeys || prefixes.any(key::startsWith)
}

/**
 * Moves auth data out of [from] (the plain platform store) into [to] (the
 * secure store). Idempotent — migrated keys are removed from [from], and a
 * value already present in [to] is never overwritten, so a stale plain-store
 * copy can't clobber newer secure data.
 */
internal fun migrateAuthStorage(from: Settings, to: Settings) {
    from.keys.filter(AuthStorageKeys::isAuthKey).forEach { key ->
        if (!to.hasKey(key)) {
            when {
                key.startsWith(AuthStorageKeys.SIGN_IN_ATTEMPTS_PREFIX) ->
                    from.getIntOrNull(key)?.let { to.putInt(key, it) }
                key.startsWith(AuthStorageKeys.SIGN_IN_LOCKOUT_PREFIX) ->
                    from.getLongOrNull(key)?.let { to.putLong(key, it) }
                else -> from.getStringOrNull(key)?.let { to.putString(key, it) }
            }
        }
        from.remove(key)
    }
}
