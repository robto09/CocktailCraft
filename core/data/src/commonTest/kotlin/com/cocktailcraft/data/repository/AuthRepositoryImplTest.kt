package com.cocktailcraft.data.repository

import com.cocktailcraft.data.security.PasswordHasher
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.util.ErrorCode
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrThrow
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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
    fun changePasswordRequiresCurrentPassword() = runTest {
        repository.signUp("a@b.com", "secret123")

        assertFalse(repository.changePassword("wrong-pass", "newpass456").getOrThrow())
        assertTrue(repository.changePassword("secret123", "newpass456").getOrThrow())
        assertTrue(repository.signIn("a@b.com", "newpass456").getOrThrow())
        assertFalse(repository.signIn("a@b.com", "secret123").getOrThrow())
    }

    @Test
    fun changePasswordLocksOutAfterRepeatedWrongCurrentPasswords() = runTest {
        var now = 0L
        val repository = AuthRepositoryImpl(MapSettings(), Json { ignoreUnknownKeys = true }, nowMs = { now })
        repository.signUp("a@b.com", "secret123")

        repeat(5) { assertFalse(repository.changePassword("wrong-pass", "newpass456").getOrThrow()) }

        // Locked: even the right current password is refused, with the remaining wait
        val locked = repository.changePassword("secret123", "newpass456")
        assertIs<Result.Error>(locked)
        assertEquals(ErrorCode.FORBIDDEN, locked.code)
        assertEquals(30_000L, locked.retryAfterMs)
        // ...and the lockout is the account's, so sign-in is locked too
        assertIs<Result.Error>(repository.signIn("a@b.com", "secret123"))

        now += 31_000
        assertTrue(repository.changePassword("secret123", "newpass456").getOrThrow())
        assertTrue(repository.signIn("a@b.com", "newpass456").getOrThrow())
    }

    @Test
    fun changePasswordFailuresShareTheSignInCounter() = runTest {
        var now = 0L
        val repository = AuthRepositoryImpl(MapSettings(), Json { ignoreUnknownKeys = true }, nowMs = { now })
        repository.signUp("a@b.com", "secret123")

        // Alternating between the two entry points must not reset the count
        repeat(3) { assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow()) }
        repeat(2) { assertFalse(repository.changePassword("wrong-pass", "newpass456").getOrThrow()) }

        assertIs<Result.Error>(repository.signIn("a@b.com", "secret123"))
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
        // The remaining wait, as data: the presentation layer words it
        assertEquals(30_000L, locked.retryAfterMs)

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

    // --- Email normalization ---

    @Test
    fun emailsMatchIgnoringCaseAndSurroundingSpaces() = runTest {
        assertTrue(repository.signUp("  Alice@Example.COM ", "secret123").getOrThrow())

        assertEquals("alice@example.com", repository.getCurrentUser().getOrThrow()?.email)
        assertTrue(repository.signIn("alice@example.com", "secret123").getOrThrow())
        assertTrue(repository.signIn("ALICE@example.com  ", "secret123").getOrThrow())
        assertFalse(
            repository.signUp("alice@EXAMPLE.com", "other-pass").getOrThrow(),
            "a differently cased email is the same account"
        )
    }

    @Test
    fun accountStoredWithMixedCaseBeforeNormalizationStillSignsIn() = runTest {
        // Written by an older version that stored emails exactly as typed
        settings.putString(
            "users",
            """[{"id":"legacy-1","email":"Bob@Example.com","name":"Bob","preferences":{}}]"""
        )
        settings.putString("password_Bob@Example.com", PasswordHasher.hash("secret123"))

        assertTrue(repository.signIn("bob@example.com", "secret123").getOrThrow())
        assertTrue(repository.signIn("Bob@Example.com", "secret123").getOrThrow())
        assertEquals("legacy-1", repository.getCurrentUser().getOrThrow()?.id)
    }

    @Test
    fun throttleIsSharedAcrossCasingsOfOneEmail() = runTest {
        var now = 0L
        val repository = AuthRepositoryImpl(MapSettings(), Json { ignoreUnknownKeys = true }, nowMs = { now })
        repository.signUp("a@b.com", "secret123")

        listOf("a@b.com", "A@b.com", "a@B.com", " a@b.com", "A@B.COM").forEach {
            assertFalse(repository.signIn(it, "wrong-pass").getOrThrow())
        }

        assertIs<Result.Error>(repository.signIn("a@b.com", "secret123"))
    }

    // --- Concurrency ---

    @Test
    fun concurrentProfileUpdatesDoNotDropEachOther() = runTest {
        // Each update rewrites the whole users list; unserialized, one
        // writer's read-modify-write would overwrite the other's change.
        val repository = AuthRepositoryImpl(
            settings,
            Json { ignoreUnknownKeys = true },
            ioDispatcher = Dispatchers.Default
        )
        repository.signUp("a@b.com", "secret123")

        repeat(50) { round ->
            val preferences = UserPreferences(language = "lang-$round")
            withContext(Dispatchers.Default) {
                awaitAll(
                    async { repository.updateUserName("Name $round") },
                    async { repository.updateUserPreferences(preferences) }
                )
            }
            assertEquals("Name $round", repository.getCurrentUser().getOrThrow()?.name)
            assertEquals(preferences, repository.getUserPreferences().getOrThrow())
        }
    }

    // --- Throttle state for emails without an account ---

    private fun MapSettings.throttleKeyCount() =
        keys.count { it.startsWith("signin_attempts_") || it.startsWith("signin_lockout_until_") }

    @Test
    fun failedSignInsForMadeUpEmailsKeepABoundedNumberOfKeys() = runTest {
        val settings = MapSettings()
        val repository = AuthRepositoryImpl(settings, Json { ignoreUnknownKeys = true }, nowMs = { 0L })
        val cap = AuthRepositoryImpl.MAX_UNKNOWN_THROTTLED_EMAILS

        repeat(cap * 5) { i ->
            repeat(5) { assertFalse(repository.signIn("nobody$i@b.com", "wrong-pass").getOrThrow()) }
        }

        // Two keys (attempts + lockout deadline) per throttled email
        assertTrue(settings.throttleKeyCount() <= cap * 2, "got ${settings.throttleKeyCount()} throttle keys")
    }

    @Test
    fun unknownEmailsStillLockOutLikeAccounts() = runTest {
        // Locking only real accounts would reveal which addresses exist
        val repository = AuthRepositoryImpl(MapSettings(), Json { ignoreUnknownKeys = true }, nowMs = { 0L })

        repeat(5) { assertFalse(repository.signIn("nobody@b.com", "wrong-pass").getOrThrow()) }

        assertIs<Result.Error>(repository.signIn("nobody@b.com", "wrong-pass"))
    }

    @Test
    fun floodingMadeUpEmailsDoesNotResetAnAccountLockout() = runTest {
        val repository = AuthRepositoryImpl(MapSettings(), Json { ignoreUnknownKeys = true }, nowMs = { 0L })
        repository.signUp("a@b.com", "secret123")
        repeat(5) { assertFalse(repository.signIn("a@b.com", "wrong-pass").getOrThrow()) }

        repeat(AuthRepositoryImpl.MAX_UNKNOWN_THROTTLED_EMAILS * 3) { i ->
            assertFalse(repository.signIn("nobody$i@b.com", "wrong-pass").getOrThrow())
        }

        assertIs<Result.Error>(repository.signIn("a@b.com", "secret123"), "the account must still be locked")
    }

    @Test
    fun evictionDropsTheLeastProtectiveUnknownEmailFirst() = runTest {
        val repository = AuthRepositoryImpl(MapSettings(), Json { ignoreUnknownKeys = true }, nowMs = { 0L })
        val cap = AuthRepositoryImpl.MAX_UNKNOWN_THROTTLED_EMAILS

        // One locked-out unknown email, then enough single failures to fill the cap and more
        repeat(5) { repository.signIn("locked@b.com", "wrong-pass") }
        repeat(cap * 2) { i -> repository.signIn("nobody$i@b.com", "wrong-pass") }

        assertIs<Result.Error>(
            repository.signIn("locked@b.com", "wrong-pass"),
            "an active lockout outlives one-off failures"
        )
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
