package com.cocktailcraft.data.security

/** [count] bytes from the platform's cryptographically secure RNG. */
internal expect fun secureRandomBytes(count: Int): ByteArray

/**
 * PBKDF2-HMAC-SHA256 (RFC 8018) over [password] (as UTF-8 bytes) and a
 * non-empty [salt], deriving [keyLengthBytes] bytes with [iterations] rounds.
 */
internal expect fun pbkdf2Sha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int,
): ByteArray
