package com.example.breadapp2

import android.app.Application
import com.coffee.store.di.appModule
import com.coffee.store.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BreadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@BreadApplication)
            modules(appModule, platformModule())
        }
    }
} 