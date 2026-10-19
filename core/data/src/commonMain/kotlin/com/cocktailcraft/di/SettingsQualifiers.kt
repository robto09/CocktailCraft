package com.cocktailcraft.di

import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

/**
 * Qualifier for the encrypted Settings instance holding credentials and
 * profile PII (a Tink-encrypted file with a Keystore-wrapped key on Android,
 * the Keychain on iOS). The unqualified Settings binding remains the plain store
 * for non-sensitive data (cache, cart, favorites, orders).
 */
val secureSettingsQualifier: StringQualifier = named("secureSettings")
