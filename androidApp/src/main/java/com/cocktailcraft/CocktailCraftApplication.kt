package com.cocktailcraft

import android.app.Application
import com.cocktailcraft.di.appModule
import com.cocktailcraft.di.platformModule
import com.cocktailcraft.di.recommendationModule
import com.cocktailcraft.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Main application class for CocktailCraft.
 * This class initializes Koin for dependency injection.
 */
class CocktailCraftApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Use Level.ERROR for production to avoid excessive logging
            androidLogger(Level.ERROR)
            androidContext(this@CocktailCraftApplication)

            // Load all modules
            // - appModule: Common, Network, Data, Domain, and UseCase modules
            // - platformModule(): Platform-specific dependencies
            // - recommendationModule: Recommendation engine and related components
            // - viewModelModule: All ViewModels
            modules(appModule + platformModule() + recommendationModule + viewModelModule)
        }
    }
}