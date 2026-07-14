package com.cocktailcraft.android

import android.app.Application
import com.cocktailcraft.di.dataModule
import com.cocktailcraft.di.domainModule
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
            modules(dataModule, domainModule)
        }
        
        MockKAnnotations.init(this)
    }
}
