package com.cocktailcraft.viewmodel

import app.cash.turbine.test
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {
    
    private lateinit var viewModel: ThemeViewModel
    private val authRepository: AuthRepository = mock()
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        
        // Mock the initial state - default to light mode
        val initialPreferences = UserPreferences(darkMode = false)
        whenever(authRepository.getUserPreferences()).thenReturn(flowOf(initialPreferences))
        
        // Create the view model with the mocked repository
        viewModel = ThemeViewModel(authRepository)
        
        // Advance the dispatcher to allow the init block to complete
        testDispatcher.scheduler.advanceUntilIdle()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should load dark mode preference from repository`() = runTest {
        // Verify the initial state
        viewModel.isDarkMode.test(timeout = 5.seconds) {
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
    
    @Test
    fun `setDarkMode should set the dark mode state to the specified value`() = runTest {
        // Initial state is false (light mode)
        viewModel.isDarkMode.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        // Mock the update preferences call
        val updatedPreferences = UserPreferences(darkMode = true)
        whenever(authRepository.updateUserPreferences(updatedPreferences)).thenReturn(flowOf(true))
        
        // Set dark mode to true
        viewModel.setDarkMode(true)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify the state was set to true
        viewModel.isDarkMode.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify the repository was called to update preferences
        verify(authRepository).updateUserPreferences(updatedPreferences)
    }
}
