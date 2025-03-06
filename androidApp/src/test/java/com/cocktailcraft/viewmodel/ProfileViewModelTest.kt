package com.cocktailcraft.viewmodel

import app.cash.turbine.test
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.repository.AuthRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    
    private lateinit var viewModel: ProfileViewModel
    private val authRepository: AuthRepository = mock()
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        
        // Mock the initial state - now within a coroutine context
        whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(false))
        whenever(authRepository.getCurrentUser()).thenReturn(flowOf(null))
        
        // Create the view model with the mocked repository
        viewModel = ProfileViewModel(authRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be not signed in`() = runTest {
        viewModel.isSignedIn.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `initial user should be null`() = runTest {
        viewModel.user.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `sign in success should update state`() = runTest {
        // Mock successful sign in
        val testUser = User(id = "1", name = "Test User", email = "test@example.com")
        whenever(authRepository.signIn("test@example.com", "password")).thenReturn(flowOf(true))
        whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(true))
        whenever(authRepository.getCurrentUser()).thenReturn(flowOf(testUser))
        
        // Call sign in
        viewModel.signIn("test@example.com", "password")
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify states
        viewModel.isSignedIn.test(timeout = 5.seconds) {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.user.test(timeout = 5.seconds) {
            val user = awaitItem()
            assertEquals("Test User", user?.name)
            assertEquals("test@example.com", user?.email)
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.isLoading.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `sign in failure should show error`() = runTest {
        // Mock failed sign in
        whenever(authRepository.signIn("wrong@example.com", "password")).thenReturn(flowOf(false))
        
        // Call sign in
        viewModel.signIn("wrong@example.com", "password")
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify states
        viewModel.isSignedIn.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.error.test {
            assertEquals("Invalid email or password", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.isLoading.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `sign up success should update state`() = runTest {
        // Mock successful sign up
        whenever(authRepository.signUp("test@example.com", "password")).thenReturn(flowOf(true))
        whenever(authRepository.updateUserName("Test User")).thenReturn(flowOf(true))
        whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(true))
        whenever(authRepository.getCurrentUser()).thenReturn(
            flowOf(User(id = "1", name = "Test User", email = "test@example.com"))
        )
        
        // Call sign up
        viewModel.signUp("Test User", "test@example.com", "password")
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify states
        viewModel.isSignedIn.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.user.test {
            val user = awaitItem()
            assertEquals("Test User", user?.name)
            assertEquals("test@example.com", user?.email)
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.isLoading.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        // Verify updateUserName was called
        verify(authRepository).updateUserName("Test User")
    }
    
    @Test
    fun `clear error should set error to null`() = runTest {
        // Make sure signIn is properly mocked
        whenever(authRepository.signIn("wrong@example.com", "password")).thenReturn(flowOf(false))
        
        // Set an error
        viewModel.signIn("wrong@example.com", "password")
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify error is set
        viewModel.error.test {
            assertEquals("Invalid email or password", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        // Clear error
        viewModel.clearError()
        
        // Verify error is cleared
        viewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `sign out success should update state`() = runTest {
        // Setup signed in state first
        val testUser = User(id = "1", name = "Test User", email = "test@example.com")
        whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(true))
        whenever(authRepository.getCurrentUser()).thenReturn(flowOf(testUser))
        
        // Create a new view model with the signed-in state
        val signedInViewModel = ProfileViewModel(authRepository)
        
        // Advance the dispatcher to allow the init block to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Mock successful sign out
        whenever(authRepository.signOut()).thenReturn(flowOf(true))
        whenever(authRepository.isUserSignedIn()).thenReturn(flowOf(false))
        
        // Call sign out
        signedInViewModel.signOut()
        
        // Advance the dispatcher to allow coroutines to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify states
        signedInViewModel.isSignedIn.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        signedInViewModel.user.test(timeout = 5.seconds) {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        signedInViewModel.isLoading.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        
        signedInViewModel.error.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
} 