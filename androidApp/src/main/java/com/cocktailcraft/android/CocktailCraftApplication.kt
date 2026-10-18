package com.cocktailcraft.android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.cocktailcraft.android.di.widgetModule
import com.cocktailcraft.android.util.ImageLoaderSingleton
import com.cocktailcraft.initKoin
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class CocktailCraftApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // Shared app init: sets the log floor from this build's flag (release
        // keeps only warnings/errors), starts Koin with the shared modules and
        // starts process-wide connectivity monitoring (SH-2).
        initKoin(isDebug = BuildConfig.DEBUG) {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.ERROR)
            androidContext(this@CocktailCraftApplication)
            modules(widgetModule)
        }

        // Widget refresh is scheduled by the widget receivers (onEnabled),
        // never at launch: no widgets placed means no background work.
    }

    // Coil asks for its singleton loader on first image load (any thread,
    // once). Built over the OkHttpClient the Ktor engine shares
    // (dataPlatformModule), so the app runs one HTTP stack.
    override fun newImageLoader(): ImageLoader =
        ImageLoaderSingleton.create(this, okHttpClient = get())
}
