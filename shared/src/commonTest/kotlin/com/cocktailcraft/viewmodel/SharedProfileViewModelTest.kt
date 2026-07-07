package com.cocktailcraft.viewmodel

import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.testutil.MainDispatcherTest
import com.cocktailcraft.util.ErrorHandler
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** An auth store that is completely unavailable, for error-propagation tests. */
private class FailingAuthRepository(
    private val message: String = "auth storage unavailable"
) : AuthRepository {
    override suspend fun signUp(email: String, password: String): Result<Boolean> = Result.Error(message)
    override suspend fun signIn(email: String, password: String): Result<Boolean> = Result.Error(message)
    override suspend fun signOut(): Result<Boolean> = Result.Error(message)
    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> =
        Result.Error(message)
    override suspend fun isUserSignedIn(): Result<Boolean> = Result.Error(message)
    override suspend fun getCurrentUser(): Result<User?> = Result.Error(message)
    override suspend fun updateUserName(name: String): Result<Boolean> = Result.Error(message)
    override suspend fun updateUserEmail(email: String, password: String): Result<Boolean> = Result.Error(message)
    override suspend fun updateUserAddress(address: Address): Result<Boolean> = Result.Error(message)
    override suspend fun updateUserPreferences(preferences: UserPreferences): Result<Boolean> = Result.Error(message)
    override suspend fun getUserPreferences(): Result<UserPreferences> = Result.Error(message)
}

@OptIn(ExperimentalCoroutinesApi::class)
class SharedProfileViewModelTest : MainDispatcherTest() {

    /** Real repository over in-memory settings, so tests exercise VM + domain + persistence together. */
    private class Harness(ioDispatcher: CoroutineDispatcher) {
        val settings = MapSettings()
        val repository = AuthRepositoryImpl(settings, Json { ignoreUnknownKeys = true }, ioDispatcher)

        fun viewModel(authRepository: AuthRepository = repository) =
            SharedProfileViewModel(ManageProfileUseCase(authRepository))

        /** Create an account with a profile name, then sign out so tests start from a clean session. */
        suspend fun seedAccount(name: String = "Ada Lovelace", email: String = "ada@example.com", password: String = "secret123") {
            repository.signUp(email, password)
            repository.updateUserName(name)
            repository.signOut()
        }
    }

    @Test
    fun initReflectsSignedOutState() = runTest {
        val vm = Harness(StandardTestDispatcher(testScheduler)).viewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoggedIn)
        assertEquals("Not Authenticated", vm.uiState.value.authStatus)
        assertNull(vm.uiState.value.user)
        assertTrue(vm.isGuest)
        assertEquals("Guest", vm.userName)
    }

    @Test
    fun initRestoresExistingSession() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        harness.repository.signUp("ada@example.com", "secret123")
        harness.repository.updateUserName("Ada Lovelace")

        val vm = harness.viewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isLoggedIn)
        assertEquals("Authenticated", vm.uiState.value.authStatus)
        assertEquals("Ada Lovelace", vm.uiState.value.user?.name)
        assertTrue(vm.hasUser)
        assertFalse(vm.isGuest)
    }

    @Test
    fun signInSuccessLoadsUserAndMarksLoggedIn() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        harness.seedAccount()
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoggedIn)

        assertTrue(vm.signIn("ada@example.com", "secret123"))

        assertTrue(vm.uiState.value.isLoggedIn)
        assertEquals("Authenticated", vm.uiState.value.authStatus)
        assertEquals("Ada Lovelace", vm.uiState.value.user?.name)
        assertEquals("ada@example.com", vm.getUserEmail())
        assertEquals("AL", vm.getUserInitials())
        assertFalse(vm.uiState.value.isAuthenticating)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.error.value)
    }

    @Test
    fun signInRejectsInvalidEmailBeforeTouchingRepository() = runTest {
        val vm = Harness(StandardTestDispatcher(testScheduler)).viewModel()
        advanceUntilIdle()

        assertFalse(vm.signIn("not-an-email", "secret123"))

        val error = assertNotNull(vm.error.value)
        assertEquals("Invalid Email", error.title)
        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
        assertFalse(vm.uiState.value.isAuthenticating, "validation failures must not start authentication")
    }

    @Test
    fun signInRejectsShortPassword() = runTest {
        val vm = Harness(StandardTestDispatcher(testScheduler)).viewModel()
        advanceUntilIdle()

        assertFalse(vm.signIn("ada@example.com", "12345"))

        val error = assertNotNull(vm.error.value)
        assertEquals("Invalid Password", error.title)
        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
    }

    @Test
    fun signInWithWrongCredentialsSetsAuthenticationError() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        harness.seedAccount()
        val vm = harness.viewModel()
        advanceUntilIdle()

        assertFalse(vm.signIn("ada@example.com", "wrong-pass"))

        assertEquals("Sign In Failed", vm.uiState.value.authStatus)
        val error = assertNotNull(vm.error.value)
        assertEquals("Sign In Failed", error.title)
        assertEquals(ErrorHandler.ErrorCategory.AUTHENTICATION, error.category)
        assertFalse(vm.uiState.value.isLoggedIn)
        assertFalse(vm.uiState.value.isAuthenticating)
    }

    @Test
    fun signUpCreatesAccountAndStoresProfileName() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        val vm = harness.viewModel()
        advanceUntilIdle()

        assertTrue(vm.signUp("Ada Lovelace", "ada@example.com", "secret123"))

        assertEquals("Sign Up Successful", vm.uiState.value.authStatus)
        assertTrue(vm.uiState.value.isLoggedIn, "a successful sign-up must refresh auth status")
        assertEquals("Ada Lovelace", vm.uiState.value.user?.name)
        assertTrue(vm.hasCompleteProfile())
        assertFalse(vm.uiState.value.isAuthenticating)
        assertNull(vm.error.value)
        // Credentials really persisted: a fresh sign-in against the repository succeeds
        assertTrue(harness.repository.signOut().isSuccess())
        assertEquals(Result.Success(true), harness.repository.signIn("ada@example.com", "secret123"))
    }

    @Test
    fun signUpWithExistingEmailSetsAuthenticationError() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        harness.seedAccount(email = "ada@example.com")
        val vm = harness.viewModel()
        advanceUntilIdle()

        assertFalse(vm.signUp("Grace Hopper", "ada@example.com", "different456"))

        assertEquals("Sign Up Failed", vm.uiState.value.authStatus)
        val error = assertNotNull(vm.error.value)
        assertEquals("Sign Up Failed", error.title)
        assertEquals(ErrorHandler.ErrorCategory.AUTHENTICATION, error.category)
    }

    @Test
    fun signUpRejectsBlankName() = runTest {
        val vm = Harness(StandardTestDispatcher(testScheduler)).viewModel()
        advanceUntilIdle()

        assertFalse(vm.signUp("   ", "ada@example.com", "secret123"))

        val error = assertNotNull(vm.error.value)
        assertEquals("Invalid Name", error.title)
        assertEquals(ErrorHandler.ErrorCategory.DATA, error.category)
    }

    @Test
    fun signOutClearsUserState() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        harness.repository.signUp("ada@example.com", "secret123")
        harness.repository.updateUserName("Ada Lovelace")
        val vm = harness.viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isLoggedIn)

        assertTrue(vm.signOut())

        assertNull(vm.uiState.value.user)
        assertFalse(vm.uiState.value.isLoggedIn)
        assertEquals("Signed Out", vm.uiState.value.authStatus)
        assertTrue(vm.isGuest)
        assertEquals("Guest", vm.userName)
    }

    @Test
    fun changePasswordUpdatesStoredCredential() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        harness.repository.signUp("ada@example.com", "secret123")
        val vm = harness.viewModel()
        advanceUntilIdle()

        assertTrue(vm.changePassword("secret123", "newpass456"))

        assertEquals(Result.Success(true), harness.repository.signIn("ada@example.com", "newpass456"))
        assertEquals(Result.Success(false), harness.repository.signIn("ada@example.com", "secret123"))
    }

    @Test
    fun changePasswordRejectsShortPasswords() = runTest {
        val vm = Harness(StandardTestDispatcher(testScheduler)).viewModel()
        advanceUntilIdle()

        assertFalse(vm.changePassword("123", "456"))

        val error = assertNotNull(vm.error.value)
        assertEquals("Invalid Password", error.title)
    }

    @Test
    fun repositoryErrorPropagatesToErrorChannel() = runTest {
        val vm = Harness(StandardTestDispatcher(testScheduler)).viewModel(FailingAuthRepository())
        advanceUntilIdle()

        assertFalse(vm.signIn("ada@example.com", "secret123"))

        val error = assertNotNull(vm.error.value)
        assertEquals("auth storage unavailable", error.message)
        assertFalse(vm.uiState.value.isAuthenticating)
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isLoggedIn)
    }
}
