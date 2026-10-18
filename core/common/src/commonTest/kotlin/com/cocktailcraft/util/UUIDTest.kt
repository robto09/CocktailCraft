package com.cocktailcraft.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UUIDTest {

    private val v4 = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")

    @Test
    fun randomUUIDIsAVersion4UuidString() {
        repeat(100) {
            val id = UUID.randomUUID()
            assertTrue(v4.matches(id), "not a lowercase v4 UUID: $id")
        }
    }

    @Test
    fun randomUUIDsDoNotRepeat() {
        val ids = List(1_000) { UUID.randomUUID() }
        assertEquals(ids.size, ids.toSet().size)
    }
}
