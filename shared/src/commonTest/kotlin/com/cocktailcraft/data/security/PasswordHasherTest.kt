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
        assertTrue(PasswordHasher.isHashed(stored))
        assertTrue(PasswordHasher.verify("hunter2!", stored))
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
        assertFalse(PasswordHasher.verify("anything", "v2:10000:00ff:00ff"))
    }

    @Test
    fun isHashedDistinguishesLegacyPlaintext() {
        assertFalse(PasswordHasher.isHashed("password123"))
        assertTrue(PasswordHasher.isHashed(PasswordHasher.hash("password123")))
    }
}
