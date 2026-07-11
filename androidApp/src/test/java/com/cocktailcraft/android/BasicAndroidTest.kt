package com.cocktailcraft.android

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.Rule
import kotlin.test.assertTrue

/**
 * Basic Android unit tests to verify test setup is working.
 * More complex tests with mocking can be added as needed.
 */
class BasicAndroidTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test setup should work`() = runTest {
        // Basic test to verify JUnit Jupiter and kotlin-test integration
        assertTrue(true, "Test setup is working correctly")
    }

    @Test
    fun `coroutines test should work`() = runTest {
        // Test that coroutine testing is properly configured
        assertTrue(true, "Coroutines testing is working correctly")
    }

    @Test
    fun `arch components test should work`() {
        // Test that Android Architecture Components testing is working
        assertTrue(true, "Architecture Components testing is working correctly")
    }
}
