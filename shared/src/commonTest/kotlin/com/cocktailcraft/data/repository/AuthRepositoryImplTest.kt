package com.cocktailcraft.data.repository

import com.cocktailcraft.data.security.PasswordHasher
import com.cocktailcraft.domain.util.getOrThrow
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    private val settings = MapSettings()
    private val repository = AuthRepositoryImpl(settings, Json { ignoreUnknownKeys = true })

    @Test
    fun signUpStoresHashedCredentialNotPlaintext() = runTest {
        assertTrue(repository.signUp("a@b.com", "secret123").getOrThrow())

        val stored = settings.getStringOrNull("password_a@b.com")
        assertNotNull(stored)
        assertNotEquals("secret123", stored)
        assertTrue(PasswordHasher.isHashed(stored))
        assertTrue(repository.signIn("a@b.com", "secret123").getOrThrow())
    }

    @Test
    fun signInRejectsWrongPassword() = runTest {
        repository.signUp("a@b.com", "secret123")

        assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow())
        assertFalse(repository.signIn("nobody@b.com", "secret123").getOrThrow())
    }

    @Test
    fun legacyPlaintextCredentialMigratesOnSignIn() = runTest {
        repository.signUp("a@b.com", "secret123")
        // Simulate an install from before hashing landed
        settings.putString("password_a@b.com", "secret123")

        assertTrue(repository.signIn("a@b.com", "secret123").getOrThrow())

        val stored = settings.getStringOrNull("password_a@b.com")
        assertNotNull(stored)
        assertTrue(PasswordHasher.isHashed(stored), "legacy credential should be upgraded in place")
        assertTrue(repository.signIn("a@b.com", "secret123").getOrThrow())
        assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow())
    }

    @Test
    fun changePasswordRequiresCurrentPassword() = runTest {
        repository.signUp("a@b.com", "secret123")

        assertFalse(repository.changePassword("wrong-pass", "newpass456").getOrThrow())
        assertTrue(repository.changePassword("secret123", "newpass456").getOrThrow())
        assertTrue(repository.signIn("a@b.com", "newpass456").getOrThrow())
        assertFalse(repository.signIn("a@b.com", "secret123").getOrThrow())
    }
}
