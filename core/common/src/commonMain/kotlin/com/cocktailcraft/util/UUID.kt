package com.cocktailcraft.util

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Multiplatform UUID generator
 */
object UUID {
    /**
     * Generates a random UUID v4 string (lowercase, hyphenated). Backed by
     * the stdlib's [Uuid.random], which draws from the platform's
     * cryptographically secure RNG — ids double as unguessable keys (user
     * ids, order ids), so the non-crypto kotlin.random.Random is not enough.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun randomUUID(): String = Uuid.random().toString()
}
