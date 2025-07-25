package com.cocktailcraft.di

import com.cocktailcraft.util.IOSNetworkMonitor
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings
import org.koin.dsl.module

actual fun platformModule() = module {
    single<Settings> {
        NSUserDefaultsSettings(platform.Foundation.NSUserDefaults.standardUserDefaults)
    }

    // Network monitoring
    single<NetworkMonitor> { IOSNetworkMonitor() }
}