package com.cocktailcraft.data.security

import java.security.MessageDigest
import java.security.SecureRandom

internal actual fun sha256(data: ByteArray): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(data)

private val secureRandom = SecureRandom()

internal actual fun secureRandomBytes(count: Int): ByteArray =
    ByteArray(count).also(secureRandom::nextBytes)
