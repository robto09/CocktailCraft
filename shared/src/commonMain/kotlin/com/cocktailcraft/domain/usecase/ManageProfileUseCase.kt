package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.Address
import com.cocktailcraft.domain.model.User
import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.util.Result
import com.cocktailcraft.domain.util.getOrDefault

internal class ManageProfileUseCase(
    private val authRepository: AuthRepository
) {
    suspend fun signIn(email: String, password: String): Result<Boolean> {
        return authRepository.signIn(email, password)
    }

    suspend fun signUp(email: String, password: String): Result<Boolean> {
        return authRepository.signUp(email, password)
    }

    suspend fun signOut(): Result<Boolean> {
        return authRepository.signOut()
    }

    suspend fun isUserSignedIn(): Result<Boolean> {
        return authRepository.isUserSignedIn()
    }

    suspend fun getCurrentUser(): Result<User?> {
        return authRepository.getCurrentUser()
    }

    suspend fun updateUserName(name: String): Result<Boolean> {
        return authRepository.updateUserName(name)
    }

    suspend fun updateUserAddress(address: Address): Result<Boolean> {
        return authRepository.updateUserAddress(address)
    }

    suspend fun resetPassword(email: String): Result<Boolean> {
        return authRepository.resetPassword(email)
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Boolean> {
        return authRepository.changePassword(oldPassword, newPassword)
    }

    suspend fun getUserPreferences(): Result<UserPreferences> {
        return authRepository.getUserPreferences()
    }

    suspend fun updateUserPreferences(preferences: UserPreferences): Result<Boolean> {
        return authRepository.updateUserPreferences(preferences)
    }
}

