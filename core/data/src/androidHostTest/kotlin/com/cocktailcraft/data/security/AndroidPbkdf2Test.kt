package com.cocktailcraft.data.security

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The Android actual switches from the hand-written PBKDF2 to
 * SecretKeyFactory where available. Stored v2 hashes were made by the
 * hand-written one, so both must agree byte for byte or existing accounts
 * stop signing in.
 *
 * Runs on the host JVM, whose SunJCE provider stands in for the device's.
 */
class AndroidPbkdf2Test {

    private class Vector(val password: String, val salt: String, val iterations: Int, val keyLength: Int, val hex: String)

    // Published PBKDF2-HMAC-SHA256 test vectors (incl. a multi-block key)
    private val vectors = listOf(
        Vector("password", "salt", 1, 32, "120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b"),
        Vector("password", "salt", 2, 32, "ae4d0c95af6b46d32d0adff928f06dd02a303f8ef3c251dfd6e2d85a95474c43"),
        Vector("password", "salt", 4096, 32, "c5e478d59288c841aa530db6845c4c8d962893a001ce4e11a4963873aa98134a"),
        Vector(
            "passwordPASSWORDpassword", "saltSALTsaltSALTsaltSALTsaltSALTsalt", 4096, 40,
            "348c89dbcbd32b2f32d814b8116e84cf2b17347ebc1800181c4e2a1fb8dd53e1c635518c7dac47e9"
        ),
    )

    private fun String.hexBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun platformAndManualImplementationsMatchThePublishedVectors() {
        vectors.forEach { v ->
            val salt = v.salt.encodeToByteArray()
            val expected = v.hex.hexBytes()
            val platform = assertNotNull(
                platformPbkdf2Sha256(v.password, salt, v.iterations, v.keyLength),
                "the JVM provides PBKDF2WithHmacSHA256, so the platform path must be taken"
            )
            assertContentEquals(expected, platform, "platform, ${v.iterations} iterations")
            assertContentEquals(expected, manualPbkdf2Sha256(v.password, salt, v.iterations, v.keyLength))
            assertContentEquals(expected, pbkdf2Sha256(v.password, salt, v.iterations, v.keyLength))
        }
    }

    @Test
    fun platformMatchesManualForNonAsciiPasswordsAndOddKeyLengths() {
        val salt = "0123456789abcdef".encodeToByteArray()
        listOf("pässwörd", "密码🔒🍸", "a", " leading and trailing ").forEach { password ->
            listOf(1, 16, 31, 33, 64).forEach { keyLength ->
                assertContentEquals(
                    manualPbkdf2Sha256(password, salt, 1000, keyLength),
                    platformPbkdf2Sha256(password, salt, 1000, keyLength),
                    "password=$password keyLength=$keyLength"
                )
            }
        }
    }

    @Test
    fun productionParametersMatch() {
        // PasswordHasher's settings: 600k iterations, 16-byte salt, 32-byte key
        val salt = ByteArray(16) { it.toByte() }
        assertContentEquals(
            manualPbkdf2Sha256("correct horse battery staple", salt, 600_000, 32),
            platformPbkdf2Sha256("correct horse battery staple", salt, 600_000, 32)
        )
    }

    @Test
    fun hashesStoredByTheManualImplementationStillVerify() {
        // A v2 value as an install before this change would have stored it
        val salt = ByteArray(16) { (it * 7).toByte() }
        val digest = manualPbkdf2Sha256("hunter2!", salt, 600_000, 32)
        fun ByteArray.hex() = joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
        val stored = "v2:600000:${salt.hex()}:${digest.hex()}"

        assertTrue(PasswordHasher.verify("hunter2!", stored))
    }

    @Test
    fun inputsProvidersHandleInconsistentlyTakeTheManualPath() {
        val salt = "salt".encodeToByteArray()
        assertNull(platformPbkdf2Sha256("", salt, 1, 32), "empty password")
        assertNull(platformPbkdf2Sha256("bad\uD800surrogate", salt, 1, 32), "unpaired surrogate")
        // ...and still derive a key through the fallback
        assertContentEquals(manualPbkdf2Sha256("", salt, 1, 32), pbkdf2Sha256("", salt, 1, 32))
    }
}
