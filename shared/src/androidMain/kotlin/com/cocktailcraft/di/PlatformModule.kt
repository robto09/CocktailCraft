package com.cocktailcraft.di

import android.content.Context
import com.cocktailcraft.util.AndroidNetworkMonitor
import com.cocktailcraft.util.NetworkMonitor
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module

actual fun platformModule() = module {
    single<Settings> {
        val context = get<Context>()
        val sharedPrefs = context.getSharedPreferences("cocktailcraft_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(sharedPrefs)
    }

    // Network monitoring
    single<NetworkMonitor> { AndroidNetworkMonitor(get<Context>()) }
}
