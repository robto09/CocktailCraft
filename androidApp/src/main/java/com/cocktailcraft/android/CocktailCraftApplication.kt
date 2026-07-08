package com.cocktailcraft.android

import android.app.Application
import com.cocktailcraft.di.appModule
import com.cocktailcraft.di.platformModule
import com.cocktailcraft.android.di.widgetModule
import com.cocktailcraft.android.sync.BackgroundSyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CocktailCraftApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.ERROR)
            androidContext(this@CocktailCraftApplication)
            modules(appModule + platformModule() + widgetModule)
        }

        // Mirror of iOS's BGTaskScheduler registration — the shared
        // BackgroundSyncService defines what the sync does.
        BackgroundSyncWorker.schedule(this)
    }
}