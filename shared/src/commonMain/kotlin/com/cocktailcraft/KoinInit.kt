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
 * Process-wide app init for both platforms: the log floor, the DI graph and
 * connectivity monitoring. [isDebug] comes from the calling app's own build
 * configuration — shared code has no reliable debug/release signal of its own.
 * [overrides] load last, so their bindings replace the platform ones — the
 * iOS test mode (doInitKoinForTesting) swaps in its isolated storage and fake
 * network this way.
 */
@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper
fun initKoin(
    isDebug: Boolean,
    overrides: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    // Before anything resolves (and logs): release builds drop Debug/Info.
    configureLogging(isDebug)
    return startKoin {
        appDeclaration()
        modules(appModule + platformModule() + overrides)
    }.also {
        // Process-wide connectivity monitoring starts once with the DI graph.
        // ViewModels only observe isOnline; they never start/stop the monitor.
        it.koin.get<NetworkMonitor>().startMonitoring()
    }
}
