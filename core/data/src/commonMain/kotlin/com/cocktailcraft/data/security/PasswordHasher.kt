package com.cocktailcraft.data.security

/**
 * PBKDF2-HMAC-SHA256 hashing for the local account store.
 * Stored format: `v2:<iterations>:<saltHex>:<digestHex>`. Any other
 * format (including the retired `v1`, iterated SHA-256) fails verification.
 *
 * Credentials kept in device Settings can never be secret from the device
 * owner; hashing keeps them out of plaintext preference files and backups.
 * Replace with a server-side KDF if auth ever grows a real backend.
 */
internal object PasswordHasher {

    private const val VERSION_V2 = "v2"

    // OWASP-recommended floor for PBKDF2-HMAC-SHA256.
    private const val PBKDF2_ITERATIONS = 600_000
    private const val SALT_BYTES = 16
    private const val KEY_BYTES = 32

    fun hash(password: String): String {
        val salt = secureRandomBytes(SALT_BYTES)
        val digest = pbkdf2Sha256(password, salt, PBKDF2_ITERATIONS, KEY_BYTES)
        return "$VERSION_V2:$PBKDF2_ITERATIONS:${salt.toHex()}:${digest.toHex()}"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(':')
        if (parts.size != 4) return false
        val iterations = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val salt = parts[2].hexToBytesOrNull() ?: return false
        val expected = parts[3].hexToBytesOrNull() ?: return false
        if (parts[0] != VERSION_V2) return false
        val computed = pbkdf2Sha256(password, salt, iterations, expected.size)
        return constantTimeEquals(computed, expected)
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { it.toUByte().toString(16).padStart(2, '0') }

    private fun String.hexToBytesOrNull(): ByteArray? {
        if (isEmpty() || length % 2 != 0) return null
        val out = ByteArray(length / 2)
        for (i in out.indices) {
            val hi = this[2 * i].digitToIntOrNull(16) ?: return null
            val lo = this[2 * i + 1].digitToIntOrNull(16) ?: return null
            out[i] = ((hi shl 4) or lo).toByte()
        }
        return out
    }
}
