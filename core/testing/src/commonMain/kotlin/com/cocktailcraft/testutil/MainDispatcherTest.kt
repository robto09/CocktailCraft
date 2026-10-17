package com.cocktailcraft.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base class for ViewModel tests. SharedViewModel launches its coroutines on
 * Dispatchers.Main.immediate, so tests swap Main for a StandardTestDispatcher;
 * runTest picks up its scheduler automatically, making advanceUntilIdle()
 * drive the ViewModel's init/launch blocks deterministically.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class MainDispatcherTest {

    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }
}
