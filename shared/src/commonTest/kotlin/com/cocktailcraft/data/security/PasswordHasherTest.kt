package com.cocktailcraft.data.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    @Test
    fun hashIsNotPlaintextAndVerifies() {
        val stored = PasswordHasher.hash("hunter2!")

        assertNotEquals("hunter2!", stored)
        assertFalse(stored.contains("hunter2!"))
        assertTrue(stored.startsWith("v2:"), "new hashes must use the PBKDF2 v2 format")
        assertFalse(PasswordHasher.needsRehash(stored))
        assertTrue(PasswordHasher.verify("hunter2!", stored))
    }

    @Test
    fun pbkdf2MatchesKnownAnswerVectors() {
        // Published PBKDF2-HMAC-SHA256 test vectors (password="password",
        // salt="salt", dkLen=32), exercised through the stored-value format so
        // both platform actuals are checked against the standard.
        val saltHex = "73616c74" // "salt"
        assertTrue(
            PasswordHasher.verify(
                "password",
                "v2:1:$saltHex:120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b"
            )
        )
        assertTrue(
            PasswordHasher.verify(
                "password",
                "v2:4096:$saltHex:c5e478d59288c841aa530db6845c4c8d962893a001ce4e11a4963873aa98134a"
            )
        )
    }

    @Test
    fun legacyV1ValuesStillVerifyAndReportNeedsRehash() {
        // Generated with the v1 algorithm (iterated SHA-256) before it was
        // retired: password="secret123", 1000 iterations.
        val v1Stored =
            "v1:1000:000102030405060708090a0b0c0d0e0f:7537a36f9a26bf682b613d7421d583aadafca51b0fa3cd9e7352511c71963e8a"

        assertTrue(PasswordHasher.needsRehash(v1Stored))
        assertTrue(PasswordHasher.verify("secret123", v1Stored))
        assertFalse(PasswordHasher.verify("wrong-password", v1Stored))
    }

    @Test
    fun wrongPasswordFailsVerification() {
        val stored = PasswordHasher.hash("correct horse")

        assertFalse(PasswordHasher.verify("battery staple", stored))
        assertFalse(PasswordHasher.verify("", stored))
    }

    @Test
    fun eachHashUsesAFreshSalt() {
        val first = PasswordHasher.hash("same password")
        val second = PasswordHasher.hash("same password")

        assertNotEquals(first, second)
        assertTrue(PasswordHasher.verify("same password", first))
        assertTrue(PasswordHasher.verify("same password", second))
    }

    @Test
    fun malformedStoredValuesFailClosed() {
        assertFalse(PasswordHasher.verify("anything", ""))
        assertFalse(PasswordHasher.verify("anything", "plaintext-legacy-value"))
        assertFalse(PasswordHasher.verify("anything", "v1:notanumber:00ff:00ff"))
        assertFalse(PasswordHasher.verify("anything", "v1:10000:zz:00ff"))
        assertFalse(PasswordHasher.verify("anything", "v2:notanumber:00ff:00ff"))
        assertFalse(PasswordHasher.verify("anything", "v2:1:zz:00ff"))
        assertFalse(PasswordHasher.verify("anything", "v2:1:00ff:00ff")) // wrong digest
        assertFalse(PasswordHasher.verify("anything", "v3:1:00ff:00ff")) // unknown version
    }

}
