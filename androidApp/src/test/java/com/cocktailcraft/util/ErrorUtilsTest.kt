package com.cocktailcraft.util

import com.cocktailcraft.domain.util.ErrorCode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ErrorUtilsTest {

    @Test
    fun `createUserFriendlyError should create error with all parameters`() {
        // Given
        val title = "Test Error"
        val message = "This is a test error message"
        val category = ErrorUtils.ErrorCategory.NETWORK
        val recoveryAction = ErrorUtils.RecoveryAction("Retry") { /* no-op */ }
        
        // When
        val error = ErrorUtils.createUserFriendlyError(
            title = title,
            message = message,
            category = category,
            recoveryAction = recoveryAction
        )
        
        // Then
        assertNotNull(error)
        assertEquals(title, error.title)
        assertEquals(message, error.message)
        assertEquals(category, error.category)
        assertEquals(recoveryAction.actionLabel, error.recoveryAction?.actionLabel)
    }
    
    @Test
    fun `createUserFriendlyError should create error with minimal parameters`() {
        // Given
        val title = "Minimal Error"
        val message = "Minimal error message"
        
        // When
        val error = ErrorUtils.createUserFriendlyError(
            title = title,
            message = message
        )
        
        // Then
        assertNotNull(error)
        assertEquals(title, error.title)
        assertEquals(message, error.message)
        assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, error.category)
        assertNull(error.recoveryAction)
    }
    
    @Test
    fun `createNetworkError should create network error`() {
        // When
        val error = ErrorUtils.createNetworkError()
        
        // Then
        assertNotNull(error)
        assertEquals("Network Error", error.title)
        assertEquals("Unable to connect to the server. Please check your internet connection and try again.", error.message)
        assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
        assertNotNull(error.recoveryAction)
        assertEquals("Retry", error.recoveryAction?.actionLabel)
    }
    
    @Test
    fun `createServerError should create server error`() {
        // When
        val error = ErrorUtils.createServerError()
        
        // Then
        assertNotNull(error)
        assertEquals("Server Error", error.title)
        assertEquals("The server encountered an error. Please try again later.", error.message)
        assertEquals(ErrorUtils.ErrorCategory.SERVER, error.category)
        assertNotNull(error.recoveryAction)
        assertEquals("Retry", error.recoveryAction?.actionLabel)
    }
    
    @Test
    fun `createErrorFromException should handle network exception`() {
        // Given
        val exception = Exception("Failed to connect to server")
        
        // When
        val error = ErrorUtils.createErrorFromException(
            exception = exception,
            defaultTitle = "Default Error",
            defaultMessage = "An error occurred"
        )
        
        // Then
        assertNotNull(error)
        assertEquals("Network Error", error.title)
        assertEquals("Unable to connect to the server. Please check your internet connection and try again.", error.message)
        assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
    }
    
    @Test
    fun `createErrorFromException should handle timeout exception`() {
        // Given
        val exception = Exception("timeout")
        
        // When
        val error = ErrorUtils.createErrorFromException(
            exception = exception,
            defaultTitle = "Default Error",
            defaultMessage = "An error occurred"
        )
        
        // Then
        assertNotNull(error)
        assertEquals("Network Error", error.title)
        assertEquals("The request timed out. Please try again.", error.message)
        assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
    }
    
    @Test
    fun `createErrorFromException should use default for unknown exception`() {
        // Given
        val exception = Exception("Some unknown error")
        
        // When
        val error = ErrorUtils.createErrorFromException(
            exception = exception,
            defaultTitle = "Default Error",
            defaultMessage = "An error occurred"
        )
        
        // Then
        assertNotNull(error)
        assertEquals("Default Error", error.title)
        assertEquals("An error occurred: Some unknown error", error.message)
        assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, error.category)
    }
    
    @Test
    fun `createErrorFromErrorCode should handle network error code`() {
        // When
        val error = ErrorUtils.createErrorFromErrorCode(
            errorCode = ErrorCode.NETWORK,
            defaultTitle = "Default Error",
            defaultMessage = "An error occurred"
        )
        
        // Then
        assertNotNull(error)
        assertEquals("Network Error", error.title)
        assertEquals("Unable to connect to the server. Please check your internet connection and try again.", error.message)
        assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
    }
    
    @Test
    fun `createErrorFromErrorCode should handle timeout error code`() {
        // When
        val error = ErrorUtils.createErrorFromErrorCode(
            errorCode = ErrorCode.TIMEOUT,
            defaultTitle = "Default Error",
            defaultMessage = "An error occurred"
        )
        
        // Then
        assertNotNull(error)
        assertEquals("Network Error", error.title)
        assertEquals("The request timed out. Please try again.", error.message)
        assertEquals(ErrorUtils.ErrorCategory.NETWORK, error.category)
    }
    
    @Test
    fun `createErrorFromErrorCode should handle unauthorized error code`() {
        // When
        val error = ErrorUtils.createErrorFromErrorCode(
            errorCode = ErrorCode.UNAUTHORIZED,
            defaultTitle = "Default Error",
            defaultMessage = "An error occurred"
        )
        
        // Then
        assertNotNull(error)
        assertEquals("Authentication Error", error.title)
        assertEquals("You are not authorized to perform this action. Please sign in and try again.", error.message)
        assertEquals(ErrorUtils.ErrorCategory.AUTHENTICATION, error.category)
    }
    
    @Test
    fun `createErrorFromErrorCode should handle invalid data error code`() {
        // When
        val error = ErrorUtils.createErrorFromErrorCode(
            errorCode = ErrorCode.INVALID_DATA,
            defaultTitle = "Default Error",
            defaultMessage = "An error occurred"
        )
        
        // Then
        assertNotNull(error)
        assertEquals("Data Error", error.title)
        assertEquals("The data provided is invalid. Please check your input and try again.", error.message)
        assertEquals(ErrorUtils.ErrorCategory.DATA, error.category)
    }
    
    @Test
    fun `createErrorFromErrorCode should use default for unknown error code`() {
        // When
        val error = ErrorUtils.createErrorFromErrorCode(
            errorCode = ErrorCode.UNKNOWN,
            defaultTitle = "Default Error",
            defaultMessage = "An error occurred"
        )
        
        // Then
        assertNotNull(error)
        assertEquals("Default Error", error.title)
        assertEquals("An error occurred", error.message)
        assertEquals(ErrorUtils.ErrorCategory.UNKNOWN, error.category)
    }
}
