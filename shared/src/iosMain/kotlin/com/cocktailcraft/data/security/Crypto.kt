package com.cocktailcraft.data.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCKeyDerivationPBKDF
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCPBKDF2
import platform.CoreCrypto.kCCPRFHmacAlgSHA256
import platform.CoreCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
internal actual fun sha256(data: ByteArray): ByteArray {
    val digest = ByteArray(CC_SHA256_DIGEST_LENGTH)
    digest.usePinned { out ->
        if (data.isEmpty()) {
            CC_SHA256(null, 0u, out.addressOf(0).reinterpret())
        } else {
            data.usePinned { pinned ->
                CC_SHA256(pinned.addressOf(0), data.size.convert(), out.addressOf(0).reinterpret())
            }
        }
    }
    return digest
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun secureRandomBytes(count: Int): ByteArray {
    val bytes = ByteArray(count)
    if (count > 0) {
        bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, count.convert(), pinned.addressOf(0))
        }
    }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun pbkdf2Sha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int,
): ByteArray {
    require(iterations > 0) { "iterations must be positive" }
    require(keyLengthBytes > 0) { "keyLengthBytes must be positive" }
    require(salt.isNotEmpty()) { "salt must not be empty" }
    // The binding takes the password as a String and marshals it as UTF-8,
    // matching encodeToByteArray() on the other platforms; the explicit
    // byte length keeps multi-byte characters counted correctly.
    val passwordByteCount = password.encodeToByteArray().size
    val derived = ByteArray(keyLengthBytes)
    val status = derived.usePinned { out ->
        salt.usePinned { s ->
            CCKeyDerivationPBKDF(
                kCCPBKDF2,
                password,
                passwordByteCount.convert(),
                s.addressOf(0).reinterpret(),
                salt.size.convert(),
                kCCPRFHmacAlgSHA256,
                iterations.convert(),
                out.addressOf(0).reinterpret(),
                keyLengthBytes.convert(),
            )
        }
    }
    check(status == kCCSuccess) { "CCKeyDerivationPBKDF failed with status $status" }
    return derived
}
