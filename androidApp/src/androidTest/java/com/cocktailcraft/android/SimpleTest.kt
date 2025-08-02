package com.cocktailcraft.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Simple test to verify that the test runner is working correctly.
 */
@RunWith(AndroidJUnit4::class)
class SimpleTest {

    @Test
    fun simple_test_passes() {
        assertTrue(true, "This test should always pass")
    }
    
    @Test
    fun simple_math_test() {
        val result = 2 + 2
        assertTrue(result == 4, "2 + 2 should equal 4")
    }
}
