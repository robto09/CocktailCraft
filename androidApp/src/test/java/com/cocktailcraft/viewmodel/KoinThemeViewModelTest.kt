package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.BaseKoinTest
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.component.inject
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Example of a ViewModel test that uses Koin for dependency injection.
 * This demonstrates how to use the BaseKoinTest class for cleaner tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class KoinThemeViewModelTest : BaseKoinTest() {
    
    private lateinit var viewModel: ThemeViewModel
    private val authRepository: AuthRepository by inject()
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    override fun setUp() {
        super.setUp() // Initialize Koin
        Dispatchers.setMain(testDispatcher)
        
        // Mock the initial state - default to light mode
        val initialPreferences = UserPreferences(darkMode = false)
        whenever(authRepository.getUserPreferences()).thenReturn(flowOf(initialPreferences))
        
        // Create the view model using Koin injection
        viewModel = ThemeViewModel()
        
        // Advance the dispatcher to allow the init block to complete
        testDispatcher.scheduler.advanceUntilIdle()
    }
    
    @After
    override fun tearDown() {
        Dispatchers.resetMain()
        super.tearDown() // Clean up Koin
    }
    
    @Test
    fun `initial state should reflect user preferences`() = runTest {
        viewModel.isDarkMode.test(timeout = 2.seconds) {
            assertFalse(awaitItem()) // Should be false (light mode) based on our mock
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `toggleDarkMode should toggle the dark mode state`() = runTest {
        // Initial state is false (light mode)
        viewModel.isDarkMode.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        // Mock the update preferences call
        val updatedPreferences = UserPreferences(darkMode = true)
        whenever(authRepository.updateUserPreferences(updatedPreferences)).thenReturn(flowOf(true))
        
        // Toggle dark mode
        viewModel.toggleDarkMode()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify the state was toggled
        viewModel.isDarkMode.test {
            assertTrue(awaitItem()) // Should now be true (dark mode)
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify the repository was called to update preferences
        verify(authRepository).updateUserPreferences(updatedPreferences)
    }
}
