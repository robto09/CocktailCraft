package com.cocktailcraft.android

import android.app.Application
import android.os.Looper
import androidx.work.Configuration
import com.cocktailcraft.di.appModule
import com.cocktailcraft.di.platformModule
import com.cocktailcraft.android.di.widgetModule
import com.cocktailcraft.android.sync.BackgroundSyncWorker
import com.cocktailcraft.android.widget.WidgetUpdateWorker
import com.cocktailcraft.android.work.CocktailCraftWorkerFactory
import com.cocktailcraft.util.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CocktailCraftApplication : Application(), Configuration.Provider, KoinComponent {

    // Consulted on WorkManager's first access — always after startKoin below,
    // because the manifest disables the default initializer (AN-10).
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(CocktailCraftWorkerFactory(getKoin().get()))
            .build()

    override fun onCreate() {
        super.onCreate()

        val koinApp = startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.INFO else Level.ERROR)
            androidContext(this@CocktailCraftApplication)
            modules(appModule + platformModule() + widgetModule)
        }

        // Process-wide connectivity monitoring starts once here. ViewModels
        // only observe isOnline; they never start/stop the monitor (SH-2).
        koinApp.koin.get<NetworkMonitor>().startMonitoring()

        // WorkManager scheduling touches its database — keep that off the
        // cold-start critical path and run it when the main thread first goes
        // idle (roughly after first frame). Koin stays eager above: everything
        // else depends on it (AN-11).
        Looper.getMainLooper().queue.addIdleHandler {
            // Mirror of iOS's BGTaskScheduler registration — the shared
            // BackgroundSyncService defines what the sync does.
            BackgroundSyncWorker.schedule(this)

            // Widget refresh had zero callers before AN-2 — data went stale
            // indefinitely. KEEP policy makes this idempotent; the worker
            // no-ops (no network) when no widgets are placed, and the widget
            // receivers handle enable/disable lifecycle.
            WidgetUpdateWorker.schedule(this)
            false // run once
        }
    }
}