package com.cocktailcraft

import com.cocktailcraft.di.testModule
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

/**
 * Base class for tests that need Koin dependency injection.
 * Automatically sets up and tears down Koin with the test module.
 */
abstract class BaseKoinTest : KoinTest {
    
    @Before
    open fun setUp() {
        stopKoin() // Ensure Koin is stopped before starting
        startKoin {
            modules(testModule)
        }
    }
    
    @After
    open fun tearDown() {
        stopKoin()
    }
}
