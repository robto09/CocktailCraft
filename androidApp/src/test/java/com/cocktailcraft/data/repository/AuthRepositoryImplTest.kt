package com.cocktailcraft.data.repository

import app.cash.turbine.test
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {
    
    private lateinit var repository: AuthRepositoryImpl
    private val settings: Settings = mock()
    private val json = Json { ignoreUnknownKeys = true }
    
    @Before
    fun setup() = runTest {
        repository = AuthRepositoryImpl(settings, json)
    }
    
    @Test
    fun `signUp should return success when email is not taken`() = runTest {
        // Mock settings behavior
        whenever(settings.getStringOrNull(any())).thenReturn(null)
        
        // Test sign up
        repository.signUp("test@example.com", "password").test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `signUp should return failure when email is already taken`() = runTest {
        // Mock settings behavior to simulate existing user
        val existingUserJson = """
            [{"id":"1","email":"test@example.com","name":"Test User","preferences":{}}]
        """.trimIndent()
        whenever(settings.getStringOrNull("users")).thenReturn(existingUserJson)
        
        // Test sign up with existing email
        repository.signUp("test@example.com", "password").test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `signIn should return success with valid credentials`() = runTest {
        // Mock settings behavior
        whenever(settings.getStringOrNull("password_test@example.com")).thenReturn("password")
        
        val existingUserJson = """
            [{"id":"1","email":"test@example.com","name":"Test User","preferences":{}}]
        """.trimIndent()
        whenever(settings.getStringOrNull("users")).thenReturn(existingUserJson)
        
        // Test sign in
        repository.signIn("test@example.com", "password").test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `signIn should return failure with invalid credentials`() = runTest {
        // Mock settings behavior
        whenever(settings.getStringOrNull("password_test@example.com")).thenReturn("correctpassword")
        
        // Test sign in with wrong password
        repository.signIn("test@example.com", "wrongpassword").test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `signOut should clear current user`() = runTest {
        // Test sign out
        repository.signOut().test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `isUserSignedIn should return true when user is signed in`() = runTest {
        // Mock settings behavior
        whenever(settings.getStringOrNull("current_user_id")).thenReturn("1")
        
        // Test is user signed in
        repository.isUserSignedIn().test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `isUserSignedIn should return false when no user is signed in`() = runTest {
        // Mock settings behavior
        whenever(settings.getStringOrNull("current_user_id")).thenReturn(null)
        
        // Test is user signed in
        repository.isUserSignedIn().test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `getCurrentUser should return user when signed in`() = runTest {
        // Mock settings behavior
        whenever(settings.getStringOrNull("current_user_id")).thenReturn("1")
        
        val existingUserJson = """
            [{"id":"1","email":"test@example.com","name":"Test User","preferences":{}}]
        """.trimIndent()
        whenever(settings.getStringOrNull("users")).thenReturn(existingUserJson)
        
        // Test get current user
        repository.getCurrentUser().test {
            val user = awaitItem()
            assertNotNull(user)
            assertEquals("1", user!!.id)
            assertEquals("test@example.com", user.email)
            assertEquals("Test User", user.name)
            awaitComplete()
        }
    }
    
    @Test
    fun `getCurrentUser should return null when not signed in`() = runTest {
        // Mock settings behavior
        whenever(settings.getStringOrNull("current_user_id")).thenReturn(null)
        
        // Test get current user
        repository.getCurrentUser().test {
            assertEquals(null, awaitItem())
            awaitComplete()
        }
    }
    
    @Test
    fun `updateUserName should update user name`() = runTest {
        // Mock settings behavior
        whenever(settings.getStringOrNull("current_user_id")).thenReturn("1")
        
        val existingUserJson = """
            [{"id":"1","email":"test@example.com","name":"Old Name","preferences":{}}]
        """.trimIndent()
        whenever(settings.getStringOrNull("users")).thenReturn(existingUserJson)
        
        // Test update user name
        repository.updateUserName("New Name").test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
}