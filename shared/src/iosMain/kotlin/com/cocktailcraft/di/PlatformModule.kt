package com.cocktailcraft.di

import com.cocktailcraft.util.IOSNetworkMonitor
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule() = module {
    single<Settings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }

    // Network monitoring
    single<NetworkMonitor> { IOSNetworkMonitor() }
}