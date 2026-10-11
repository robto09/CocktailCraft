package com.cocktailcraft.viewmodel

import com.cocktailcraft.domain.model.ThemeMode
import com.cocktailcraft.domain.repository.AuthRepository
import com.cocktailcraft.domain.usecase.ManageProfileUseCase
import com.cocktailcraft.domain.util.getOrThrow
import com.cocktailcraft.resources.MR
import com.cocktailcraft.resources.theme_mode_dark
import com.cocktailcraft.resources.theme_mode_light
import com.cocktailcraft.resources.theme_mode_system
import com.cocktailcraft.testutil.DataLayerTest
import com.russhwolf.settings.MapSettings
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SharedThemeViewModelTest : DataLayerTest() {

    /**
     * Real repository + use case over an in-memory Settings store, so
     * recreating the ViewModel simulates an app restart against the same
     * persisted data.
     */
    private inner class Harness(ioDispatcher: CoroutineDispatcher) {
        val settings = MapSettings()
        // The real AuthRepositoryImpl (internal to :core:data), via dataModule
        val repository: AuthRepository = dataLayer(ioDispatcher, settings).get()

        fun viewModel() = SharedThemeViewModel(ManageProfileUseCase(repository))
    }

    @Test
    fun themeSettingsSurviveViewModelRecreationForGuest() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setThemeMode(ThemeMode.DARK)
        vm.setAccentColor("purple")
        vm.setFontSize("large")
        vm.setHighContrast(true)
        vm.setReducedMotion(true)

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
        val harness = Harness(StandardTestDispatcher(testScheduler))
        assertTrue(harness.repository.signUp("a@b.com", "secret123").getOrThrow())

        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setAccentColor("green")
        vm.setFontSize("xlarge")
        vm.setHighContrast(true)
        vm.setReducedMotion(true)

        val restarted = harness.viewModel()
        advanceUntilIdle()

        val state = restarted.uiState.value
        assertEquals("green", state.accentColor)
        assertEquals("xlarge", state.fontSize)
        assertTrue(state.isHighContrast)
        assertTrue(state.isReducedMotion)
    }

    @Test
    fun accessibilitySettersAreValueBasedAndIdempotent() = runTest {
        // A re-fired UI callback must re-assert the same value, never flip it.
        val harness = Harness(StandardTestDispatcher(testScheduler))
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setHighContrast(true)
        vm.setHighContrast(true)
        vm.setReducedMotion(true)
        vm.setReducedMotion(true)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isHighContrast)
        assertTrue(vm.uiState.value.isReducedMotion)

        vm.setHighContrast(false)
        vm.setReducedMotion(false)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isHighContrast)
        assertFalse(vm.uiState.value.isReducedMotion)
    }

    @Test
    fun themeModeDisplayNameIsDerivedFromThemeMode() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        val vm = harness.viewModel()
        advanceUntilIdle()

        vm.setThemeMode(ThemeMode.DARK)
        assertEquals(MR.strings.theme_mode_dark.desc(), vm.uiState.value.themeModeDisplayName)
        vm.setThemeMode(ThemeMode.LIGHT)
        assertEquals(MR.strings.theme_mode_light.desc(), vm.uiState.value.themeModeDisplayName)
        vm.setThemeMode(ThemeMode.SYSTEM)
        assertEquals(MR.strings.theme_mode_system.desc(), vm.uiState.value.themeModeDisplayName)
    }

    @Test
    fun legacyPreferencesJsonWithoutThemeFieldsDecodesWithDefaults() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        // JSON persisted before accentColor/fontSize/contrast/motion existed.
        harness.settings.putString(
            "guest_preferences",
            """{"darkMode":true,"followSystemTheme":false,"notificationsEnabled":true,"language":"en"}"""
        )

        val vm = harness.viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(ThemeMode.DARK, state.themeMode)
        assertEquals("coral", state.accentColor)
        assertEquals("medium", state.fontSize)
        assertFalse(state.isHighContrast)
        assertFalse(state.isReducedMotion)
    }

    @Test
    fun legacyNeverHonoredAccentColorKeyResetsToBrandDefault() = runTest {
        val harness = Harness(StandardTestDispatcher(testScheduler))
        // "accentColor" was persisted while the setting was non-functional
        // (a "blue" default no user ever saw honored). The field now
        // serializes as "accent", so the stale key is ignored and everyone
        // starts from the brand coral default once accents actually work.
        harness.settings.putString(
            "guest_preferences",
            """{"darkMode":false,"followSystemTheme":true,"notificationsEnabled":true,"language":"en","accentColor":"blue"}"""
        )

        val vm = harness.viewModel()
        advanceUntilIdle()

        assertEquals("coral", vm.uiState.value.accentColor)
    }
}
