package com.cocktailcraft.data.security

/** SHA-256 digest of [data], computed by the platform crypto provider. */
internal expect fun sha256(data: ByteArray): ByteArray

/** [count] bytes from the platform's cryptographically secure RNG. */
internal expect fun secureRandomBytes(count: Int): ByteArray
