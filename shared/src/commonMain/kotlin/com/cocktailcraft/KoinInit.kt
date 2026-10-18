package com.cocktailcraft

import com.cocktailcraft.di.appModule
import com.cocktailcraft.di.platformModule
import com.cocktailcraft.util.NetworkMonitor
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import kotlin.native.HiddenFromObjC

/**
 * Starts the app's Koin graph. [overrides] load last, so their bindings
 * replace the platform ones — the iOS test mode (doInitKoinForTesting)
 * swaps in its isolated storage and fake network this way.
 */
@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper (AR-12)
fun initKoin(
    overrides: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(appModule + platformModule() + overrides)
    }.also {
        // Process-wide connectivity monitoring starts once with the DI graph.
        // ViewModels only observe isOnline; they never start/stop the monitor (SH-2).
        it.koin.get<NetworkMonitor>().startMonitoring()
    }
}