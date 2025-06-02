package com.cocktailcraft

import android.app.Application
import com.cocktailcraft.di.androidViewModelModule
import com.cocktailcraft.di.dataModule
import com.cocktailcraft.di.domainModule
import com.cocktailcraft.di.networkModule
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
            modules(
                listOf(
                    networkModule,
                    dataModule,
                    domainModule,
                    platformModule(),
                    recommendationModule,
                    androidViewModelModule  // Android ViewModels with proper lifecycle
                )
            )
        }
    }
}