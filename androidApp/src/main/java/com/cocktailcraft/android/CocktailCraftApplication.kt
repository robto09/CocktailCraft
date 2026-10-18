package com.cocktailcraft.android

import android.app.Application
import android.os.Looper
import com.cocktailcraft.android.di.widgetModule
import com.cocktailcraft.android.widget.WidgetUpdateWorker
import com.cocktailcraft.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class CocktailCraftApplication : Application() {

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

        // WorkManager scheduling touches its database — keep that off the
        // cold-start critical path and run it when the main thread first goes
        // idle (roughly after first frame). Koin stays eager above: everything
        // else depends on it (AN-11).
        Looper.getMainLooper().queue.addIdleHandler {
            // Widget refresh had zero callers before AN-2 — data went stale
            // indefinitely. KEEP policy makes this idempotent; the worker
            // no-ops (no network) when no widgets are placed, and the widget
            // receivers handle enable/disable lifecycle.
            WidgetUpdateWorker.schedule(this)
            false // run once
        }
    }
}