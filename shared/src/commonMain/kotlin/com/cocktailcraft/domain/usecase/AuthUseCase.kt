package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Use case for authentication operations.
 * This use case handles the business logic of user authentication,
 * including error handling and result transformation.
 */
class AuthUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Sign in a user with email and password.
     * @param email The user's email
     * @param password The user's password
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun signIn(email: String, password: String): Flow<Result<Boolean>> {
        return authRepository.signIn(email, password)
            .map { success -> Result.Success(success) as Result<Boolean> }
            .catch { e ->
                emit(Result.Error(e.message ?: "Failed to sign in"))
            }
    }

    /**
     * Sign up a new user with email and password.
     * @param email The user's email
     * @param password The user's password
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun signUp(email: String, password: String): Flow<Result<Boolean>> {
        return authRepository.signUp(email, password)
            .map { success -> Result.Success(success) as Result<Boolean> }
            .catch { e ->
                emit(Result.Error(e.message ?: "Failed to sign up"))
            }
    }

    /**
     * Sign out the current user.
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun signOut(): Flow<Result<Boolean>> {
        return authRepository.signOut()
            .map { success -> Result.Success(success) as Result<Boolean> }
            .catch { e ->
                emit(Result.Error(e.message ?: "Failed to sign out"))
            }
    }

    /**
     * Check if a user is currently signed in.
     * @return Flow of Result containing either a boolean or an error
     */
    suspend fun isSignedIn(): Flow<Result<Boolean>> = flow {
        try {
            emit(Result.Loading)
            authRepository.isUserSignedIn().collect { isSignedIn ->
                emit(Result.Success(isSignedIn))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to check sign in status"))
        }
    }

    /**
     * Get the current user.
     * @return Flow of Result containing either a User or an error
     */
    suspend fun getCurrentUser(): Flow<Result<User?>> {
        return authRepository.getCurrentUser()
            .map { user -> Result.Success(user) as Result<User?> }
            .catch { e ->
                emit(Result.Error(e.message ?: "Failed to get current user"))
            }
    }

    /**
     * Update the user's name.
     * @param name The new name
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun updateUserName(name: String): Flow<Result<Boolean>> {
        return authRepository.updateUserName(name)
            .map { success -> Result.Success(success) as Result<Boolean> }
            .catch { e ->
                emit(Result.Error(e.message ?: "Failed to update user name"))
            }
    }
}
