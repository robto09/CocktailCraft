package com.cocktailcraft.android

import android.app.Application
import com.cocktailcraft.di.appModule
import com.cocktailcraft.di.platformModule
import com.cocktailcraft.android.di.recommendationModule
import com.cocktailcraft.android.di.widgetModule
import com.cocktailcraft.android.sync.BackgroundSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CocktailCraftApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@CocktailCraftApplication)
            modules(appModule + platformModule() + recommendationModule + widgetModule)
        }

        // Mirror of iOS's BGTaskScheduler registration — the shared
        // BackgroundSyncService defines what the sync does.
        BackgroundSyncWorker.schedule(this)
    }
}