package com.cocktailcraft

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * Custom test runner for instrumented tests.
 * This ensures Koin is properly initialized for tests.
 */
class CocktailCraftTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}

/**
 * Test application class for instrumented tests.
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Stop Koin if it's already started
        try {
            stopKoin()
        } catch (e: Exception) {
            // Ignore if Koin wasn't started
        }
        
        // Start Koin with minimal configuration for tests
        startKoin {
            androidContext(this@TestApplication)
            // No modules needed for UI component tests
        }
    }
}
