package com.cocktailcraft

import com.cocktailcraft.di.appModule
import com.cocktailcraft.di.platformModule
import com.cocktailcraft.util.NetworkMonitor
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import kotlin.native.HiddenFromObjC

@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper (AR-12)
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(appModule + platformModule())
    }.also {
        // Process-wide connectivity monitoring starts once with the DI graph.
        // ViewModels only observe isOnline; they never start/stop the monitor (SH-2).
        it.koin.get<NetworkMonitor>().startMonitoring()
    }
}