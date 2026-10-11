package com.cocktailcraft.android

import android.app.Application
import com.cocktailcraft.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import io.mockk.MockKAnnotations

/**
 * Test Application class for unit tests.
 * Configures Koin dependency injection for testing environment.
 */
class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@TestApplication)
            // The shared graph minus platformModule() (androidApp sees the
            // data layer only through :shared's appModule)
            modules(appModule)
        }
        
        MockKAnnotations.init(this)
    }
}
