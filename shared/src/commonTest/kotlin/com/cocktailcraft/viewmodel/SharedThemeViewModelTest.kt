package com.cocktailcraft.viewmodel

import com.cocktailcraft.data.repository.AuthRepositoryImpl
import com.cocktailcraft.domain.model.ThemeMode
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.testutil.MainDispatcherTest
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SharedThemeViewModelTest : MainDispatcherTest() {

    /**
     * Real repository + use case over an in-memory Settings store, so
     * recreating the ViewModel simulates an app restart against the same
     * persisted data.
     */
    private class Harness {
        val settings = MapSettings()
        val repository = AuthRepositoryImpl(settings, Json { ignoreUnknownKeys = true })

        fun viewModel() = SharedThemeViewModel(ManageProfileUseCase(repository))
    }

    @Test
    fun themeSettingsSurviveViewModelRecreationForGuest() = runTest {
        val harness = Harness()
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setThemeMode(ThemeMode.DARK)
        vm.setAccentColor("purple")
        vm.setFontSize("large")
        vm.toggleHighContrast()
        vm.toggleReducedMotion()

        // Simulate an app restart: new ViewModel over the same storage.
        val restarted = harness.viewModel()
        advanceUntilIdle()

        val state = restarted.uiState.value
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals("purple", state.accentColor)
        assertEquals("large", state.fontSize)
        assertTrue(state.isHighContrast)
        assertTrue(state.isReducedMotion)
    }

    @Test
    fun themeSettingsSurviveViewModelRecreationForSignedInUser() = runTest {
        val harness = Harness()
        assertTrue(harness.repository.signUp("a@b.com", "secret123").getOrThrow())

        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setAccentColor("green")
        vm.setFontSize("xlarge")
        vm.toggleHighContrast()
        vm.toggleReducedMotion()

        val restarted = harness.viewModel()
        advanceUntilIdle()

        val state = restarted.uiState.value
        assertEquals("green", state.accentColor)
        assertEquals("xlarge", state.fontSize)
        assertTrue(state.isHighContrast)
        assertTrue(state.isReducedMotion)
    }

    @Test
    fun legacyPreferencesJsonWithoutThemeFieldsDecodesWithDefaults() = runTest {
        val harness = Harness()
        // JSON persisted before accentColor/fontSize/contrast/motion existed.
        harness.settings.putString(
            "guest_preferences",
            """{"darkMode":true,"followSystemTheme":false,"notificationsEnabled":true,"language":"en"}"""
        )

        val vm = harness.viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals("blue", state.accentColor)
        assertEquals("medium", state.fontSize)
        assertFalse(state.isHighContrast)
        assertFalse(state.isReducedMotion)
    }
}
