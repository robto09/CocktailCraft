package com.cocktailcraft.data.security

/**
 * Salted, iterated SHA-256 hashing for the local account store.
 * Stored format: `v1:<iterations>:<saltHex>:<digestHex>`.
 *
 * Credentials kept in device Settings can never be secret from the device
 * owner; hashing keeps them out of plaintext preference files and backups.
 * Replace with a server-side KDF if auth ever grows a real backend.
 */
internal object PasswordHasher {

    private const val VERSION = "v1"
    private const val ITERATIONS = 10_000
    private const val SALT_BYTES = 16

    fun hash(password: String): String {
        val salt = secureRandomBytes(SALT_BYTES)
        val digest = digest(password, salt, ITERATIONS)
        return "$VERSION:$ITERATIONS:${salt.toHex()}:${digest.toHex()}"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(':')
        if (parts.size != 4 || parts[0] != VERSION) return false
        val iterations = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val salt = parts[2].hexToBytesOrNull() ?: return false
        val expected = parts[3].hexToBytesOrNull() ?: return false
        return constantTimeEquals(digest(password, salt, iterations), expected)
    }

    /** True when [stored] is already hashed (vs a legacy plaintext value). */
    fun isHashed(stored: String): Boolean = stored.startsWith("$VERSION:")

    private fun digest(password: String, salt: ByteArray, iterations: Int): ByteArray {
        var d = sha256(salt + password.encodeToByteArray())
        repeat(iterations - 1) { d = sha256(salt + d) }
        return d
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
