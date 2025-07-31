package com.cocktailcraft.android

import android.app.Application
import com.cocktailcraft.di.dataModule
import com.cocktailcraft.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Test Application class for Android instrumented tests.
 * Configures Koin dependency injection for testing environment.
 * Note: Hilt is excluded to avoid compatibility issues in test environment.
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Koin for testing
        startKoin {
            androidContext(this@TestApplication)
            modules(dataModule, domainModule)
        }
    }
}
