package com.cocktailcraft.util

import kotlin.random.Random

/**
 * Multiplatform UUID generator
 */
object UUID {
    /**
     * Generates a random UUID v4 string
     */
    fun randomUUID(): String {
        val random = Random.Default
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        
        // Set version (4) and variant bits
        bytes[6] = (bytes[6].toInt() and 0x0f or 0x40).toByte() // Version 4
        bytes[8] = (bytes[8].toInt() and 0x3f or 0x80).toByte() // Variant 10
        
        // Convert to standard UUID string format
        return buildString {
            for (i in 0..15) {
                if (i == 4 || i == 6 || i == 8 || i == 10) {
                    append('-')
                }
                append(String.format("%02x", bytes[i]))
            }
        }
    }
}

/**
 * Extension function to format bytes as hex string
 */
private fun String.Companion.format(format: String, byte: Byte): String {
    val hex = byte.toUByte().toString(16).padStart(2, '0')
    return hex
}