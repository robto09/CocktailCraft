package com.cocktailcraft.data.repository

import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
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
        assertTrue(stored.startsWith("v2:"))
        assertTrue(repository.signIn("a@b.com", "secret123").getOrThrow())
    }

    @Test
    fun signInRejectsWrongPassword() = runTest {
        repository.signUp("a@b.com", "secret123")

        assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow())
        assertFalse(repository.signIn("nobody@b.com", "secret123").getOrThrow())
    }

    @Test
    fun plaintextStoredCredentialFailsClosed() = runTest {
        repository.signUp("a@b.com", "secret123")
        // The pre-hashing plaintext acceptance shim is gone: a raw stored
        // password must no longer authenticate (or be silently upgraded).
        settings.putString("password_a@b.com", "secret123")

        assertFalse(repository.signIn("a@b.com", "secret123").getOrThrow())
        assertEquals(
            "secret123",
            settings.getStringOrNull("password_a@b.com"),
            "a rejected credential must not be rewritten"
        )
    }

    @Test
    fun legacyV1HashUpgradesToV2OnSignIn() = runTest {
        repository.signUp("a@b.com", "secret123")
        // Simulate an install from before the PBKDF2 (v2) format landed:
        // "secret123" hashed with the retired v1 algorithm at 1000 iterations.
        settings.putString(
            "password_a@b.com",
            "v1:1000:000102030405060708090a0b0c0d0e0f:7537a36f9a26bf682b613d7421d583aadafca51b0fa3cd9e7352511c71963e8a"
        )

        assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow())
        assertTrue(
            settings.getStringOrNull("password_a@b.com")!!.startsWith("v1:"),
            "a failed sign-in must not rewrite the stored credential"
        )

        assertTrue(repository.signIn("a@b.com", "secret123").getOrThrow())

        val stored = settings.getStringOrNull("password_a@b.com")
        assertNotNull(stored)
        assertTrue(stored.startsWith("v2:"), "v1 hash should be re-hashed to v2 on successful sign-in")
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

    @Test
    fun signInLocksOutAfterRepeatedFailuresAndRecoversAfterBackoff() = runTest {
        var now = 0L
        val settings = MapSettings()
        val repository = AuthRepositoryImpl(settings, Json { ignoreUnknownKeys = true }, nowMs = { now })
        repository.signUp("a@b.com", "secret123")

        repeat(5) {
            assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow())
        }

        // Locked now — even the correct password is refused with a lockout error.
        val locked = repository.signIn("a@b.com", "secret123")
        assertIs<Result.Error>(locked)
        assertEquals(ErrorCode.FORBIDDEN, locked.code)
        assertTrue(locked.message.contains("Too many failed attempts"))

        // Base lockout is 30s; once it elapses the correct password works and
        // the counters reset.
        now += 31_000
        assertTrue(repository.signIn("a@b.com", "secret123").getOrThrow())
        assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow())
        assertTrue(repository.signIn("a@b.com", "secret123").getOrThrow())
    }

    @Test
    fun signInLockoutBacksOffExponentially() = runTest {
        var now = 0L
        val settings = MapSettings()
        val repository = AuthRepositoryImpl(settings, Json { ignoreUnknownKeys = true }, nowMs = { now })
        repository.signUp("a@b.com", "secret123")

        repeat(5) { assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow()) }

        // 30s lockout elapses, then a 6th failure doubles it to 60s.
        now += 31_000
        assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow())
        now += 31_000
        assertIs<Result.Error>(repository.signIn("a@b.com", "secret123"), "60s lockout must still hold at +31s")
        now += 30_000
        assertTrue(repository.signIn("a@b.com", "secret123").getOrThrow())
    }

    @Test
    fun signInThrottleIsPerEmail() = runTest {
        var now = 0L
        val settings = MapSettings()
        val repository = AuthRepositoryImpl(settings, Json { ignoreUnknownKeys = true }, nowMs = { now })
        repository.signUp("a@b.com", "secret123")
        repository.signUp("b@c.com", "secret456")

        repeat(5) { assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow()) }

        assertIs<Result.Error>(repository.signIn("a@b.com", "secret123"))
        assertTrue(
            repository.signIn("b@c.com", "secret456").getOrThrow(),
            "a lockout on one email must not affect another"
        )
    }
}
