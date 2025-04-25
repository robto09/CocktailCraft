package com.cocktailcraft.viewmodel

import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class BaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: TestBaseViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TestBaseViewModel()
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have no error and not loading`() = runTest {
        // Then
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `setLoading should update loading state`() = runTest {
        // When
        viewModel.setLoading(true)
        
        // Then
        assertTrue(viewModel.isLoading.value)
        
        // When
        viewModel.setLoading(false)
        
        // Then
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `setError should update error state`() = runTest {
        // Given
        val title = "Test Error"
        val message = "This is a test error message"
        val category = ErrorUtils.ErrorCategory.NETWORK
        val recoveryAction = ErrorUtils.RecoveryAction("Retry") { /* no-op */ }
        
        // When
        viewModel.setError(title, message, category, recoveryAction)
        
        // Then
        assertNotNull(viewModel.error.value)
        assertEquals(title, viewModel.error.value?.title)
        assertEquals(message, viewModel.error.value?.message)
        assertEquals(category, viewModel.error.value?.category)
        assertEquals(recoveryAction.actionLabel, viewModel.error.value?.recoveryAction?.actionLabel)
    }
    
    @Test
    fun `clearError should reset error state`() = runTest {
        // Given
        viewModel.setError(
            title = "Test Error",
            message = "This is a test error message",
            category = ErrorUtils.ErrorCategory.NETWORK
        )
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.error.value)
    }
    
    @Test
    fun `setError with minimal parameters should set defaults`() = runTest {
        // When
        viewModel.setError(
            title = "Minimal Error",
            message = "Minimal error message"
        )
        
        // Then
        assertNotNull(viewModel.error.value)
        assertEquals("Minimal Error", viewModel.error.value?.title)
        assertEquals("Minimal error message", viewModel.error.value?.message)
        assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, viewModel.error.value?.category)
        assertNull(viewModel.error.value?.recoveryAction)
    }
    
    // Test implementation of BaseViewModel for testing
    private class TestBaseViewModel : BaseViewModel() {
        // No additional implementation needed
    }
}
