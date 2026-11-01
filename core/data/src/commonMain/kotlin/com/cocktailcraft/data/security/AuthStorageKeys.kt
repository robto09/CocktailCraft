package com.cocktailcraft.data.security

/**
 * Every Settings key the auth store owns. AuthRepositoryImpl writes these to
 * the dedicated secure store (Tink-encrypted, Keystore-keyed on Android, the
 * Keychain on iOS).
 */
internal object AuthStorageKeys {
    const val USERS = "users"
    const val CURRENT_USER_ID = "current_user_id"
    const val GUEST_PREFERENCES = "guest_preferences"
    const val PASSWORD_PREFIX = "password_"
    const val SIGN_IN_ATTEMPTS_PREFIX = "signin_attempts_"
    const val SIGN_IN_LOCKOUT_PREFIX = "signin_lockout_until_"
}
