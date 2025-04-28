package com.cocktailcraft

import android.app.Application
import com.cocktailcraft.di.appModule
import com.cocktailcraft.di.platformModule
import com.cocktailcraft.di.recommendationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CocktailCraftApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@CocktailCraftApplication)
            modules(appModule + platformModule() + recommendationModule)
        }
    }
}