package com.cocktailcraft.domain.usecase

import com.cocktailcraft.domain.model.UserPreferences
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Use case for theme management operations.
 * This use case handles the business logic of theme preferences,
 * including error handling and result transformation.
 */
class ThemeUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Get the user's theme preferences.
     * @return Flow of Result containing either UserPreferences or an error
     */
    suspend fun getUserPreferences(): Flow<Result<UserPreferences>> {
        return authRepository.getUserPreferences()
            .map { preferences -> Result.Success(preferences) as Result<UserPreferences> }
            .catch { e ->
                emit(Result.Error(e.message ?: "Failed to get user preferences"))
            }
    }

    /**
     * Update the user's theme preferences.
     * @param preferences The new preferences
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun updateUserPreferences(preferences: UserPreferences): Flow<Result<Boolean>> {
        return authRepository.updateUserPreferences(preferences)
            .map { success -> Result.Success(success) as Result<Boolean> }
            .catch { e ->
                emit(Result.Error(e.message ?: "Failed to update user preferences"))
            }
    }

    /**
     * Set dark mode.
     * @param enabled Whether dark mode should be enabled
     * @param followSystem Whether to follow the system theme
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun setDarkMode(enabled: Boolean, followSystem: Boolean = false): Flow<Result<Boolean>> = flow {
        try {
            emit(Result.Loading)
            val currentPreferences = authRepository.getUserPreferences().first()
            val updatedPreferences = currentPreferences.copy(
                darkMode = enabled,
                followSystemTheme = followSystem
            )
            authRepository.updateUserPreferences(updatedPreferences).collect { success ->
                emit(Result.Success(success))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to set dark mode"))
        }
    }

    /**
     * Set whether to follow the system theme.
     * @param follow Whether to follow the system theme
     * @param systemIsDark Whether the system is currently in dark mode
     * @return Flow of Result containing either a success boolean or an error
     */
    suspend fun setFollowSystemTheme(follow: Boolean, systemIsDark: Boolean): Flow<Result<Boolean>> = flow {
        try {
            emit(Result.Loading)
            val currentPreferences = authRepository.getUserPreferences().first()
            val updatedPreferences = currentPreferences.copy(
                followSystemTheme = follow,
                // If following system, update dark mode to match system
                darkMode = if (follow) systemIsDark else currentPreferences.darkMode
            )
            authRepository.updateUserPreferences(updatedPreferences).collect { success ->
                emit(Result.Success(success))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Failed to set follow system theme"))
        }
    }
}
