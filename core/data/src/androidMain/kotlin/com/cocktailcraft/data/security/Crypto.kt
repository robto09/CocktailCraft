package com.cocktailcraft.data.security

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private val secureRandom = SecureRandom()

internal actual fun secureRandomBytes(count: Int): ByteArray =
    ByteArray(count).also(secureRandom::nextBytes)

/**
 * The platform's native PBKDF2 (SecretKeyFactory "PBKDF2WithHmacSHA256",
 * API 26+) when it can produce the same bytes, else the hand-written RFC 8018
 * construction for API 24/25. Both are checked against each other and the
 * published vectors in AndroidPbkdf2Test, so hashes stored by either verify
 * with the other.
 */
internal actual fun pbkdf2Sha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int,
): ByteArray {
    require(iterations > 0) { "iterations must be positive" }
    require(keyLengthBytes > 0) { "keyLengthBytes must be positive" }
    require(salt.isNotEmpty()) { "salt must not be empty" }
    return platformPbkdf2Sha256(password, salt, iterations, keyLengthBytes)
        ?: manualPbkdf2Sha256(password, salt, iterations, keyLengthBytes)
}

private const val PLATFORM_PBKDF2 = "PBKDF2WithHmacSHA256"

// Probed once: the algorithm is missing below API 26 (minSdk is 24).
private val platformPbkdf2Available: Boolean by lazy {
    try {
        SecretKeyFactory.getInstance(PLATFORM_PBKDF2)
        true
    } catch (e: GeneralSecurityException) {
        false
    }
}

/**
 * SecretKeyFactory PBKDF2, or null when unavailable or when it might not
 * match [manualPbkdf2Sha256] byte for byte: providers UTF-8 encode the
 * password's chars, but empty passwords and unpaired surrogates are handled
 * inconsistently across providers, so those take the manual path.
 */
internal fun platformPbkdf2Sha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int,
): ByteArray? {
    if (!platformPbkdf2Available || password.isEmpty() || !password.isWellFormedUtf16()) return null
    val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBytes * 8)
    return try {
        SecretKeyFactory.getInstance(PLATFORM_PBKDF2).generateSecret(spec).encoded
    } catch (e: GeneralSecurityException) {
        null
    } finally {
        spec.clearPassword()
    }
}

private fun String.isWellFormedUtf16(): Boolean {
    var i = 0
    while (i < length) {
        val c = this[i]
        if (c.isHighSurrogate()) {
            if (i + 1 >= length || !this[i + 1].isLowSurrogate()) return false
            i += 2
        } else {
            if (c.isLowSurrogate()) return false
            i++
        }
    }
    return true
}

// The RFC 8018 construction over HmacSHA256 (available since API 1), for
// devices without SecretKeyFactory("PBKDF2WithHmacSHA256") (API 24/25).
// Verified against the published PBKDF2-HMAC-SHA256 test vectors (see
// PasswordHasherTest.pbkdf2MatchesKnownAnswerVectors and AndroidPbkdf2Test).
internal fun manualPbkdf2Sha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int,
): ByteArray {
    val passwordBytes = password.encodeToByteArray()
    val mac = Mac.getInstance("HmacSHA256")
    // HMAC zero-pads keys to the block size, so one zero byte is equivalent
    // to the empty key SecretKeySpec refuses to represent.
    mac.init(SecretKeySpec(if (passwordBytes.isEmpty()) ByteArray(1) else passwordBytes, "HmacSHA256"))
    val hLen = mac.macLength
    val blocks = (keyLengthBytes + hLen - 1) / hLen
    val derived = ByteArray(blocks * hLen)
    for (block in 1..blocks) {
        val blockIndex = byteArrayOf(
            (block ushr 24).toByte(),
            (block ushr 16).toByte(),
            (block ushr 8).toByte(),
            block.toByte(),
        )
        var u = mac.doFinal(salt + blockIndex)
        val t = u.copyOf()
        repeat(iterations - 1) {
            u = mac.doFinal(u)
            for (k in t.indices) t[k] = (t[k].toInt() xor u[k].toInt()).toByte()
        }
        t.copyInto(derived, (block - 1) * hLen)
    }
    return derived.copyOf(keyLengthBytes)
}
