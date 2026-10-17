package com.cocktailcraft.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal actual fun sha256(data: ByteArray): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(data)

private val secureRandom = SecureRandom()

internal actual fun secureRandomBytes(count: Int): ByteArray =
    ByteArray(count).also(secureRandom::nextBytes)

// The RFC 8018 construction over HmacSHA256 (available since API 1) because
// SecretKeyFactory("PBKDF2WithHmacSHA256") needs API 26 and minSdk is 24.
// Verified against the published PBKDF2-HMAC-SHA256 test vectors (see
// PasswordHasherTest.pbkdf2MatchesKnownAnswerVectors).
internal actual fun pbkdf2Sha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int,
): ByteArray {
    require(iterations > 0) { "iterations must be positive" }
    require(keyLengthBytes > 0) { "keyLengthBytes must be positive" }
    require(salt.isNotEmpty()) { "salt must not be empty" }
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
