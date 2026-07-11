package com.cocktailcraft.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(name = "cocktailcraft_preferences")

@OptIn(ExperimentalSettingsImplementation::class, ExperimentalSettingsApi::class)
actual fun platformModule() = module {
    single { 
        get<Context>().dataStore 
    }
    
    single<FlowSettings> {
        DataStoreSettings(get<DataStore<Preferences>>())
    }
    
    single<Settings> {
        val context = get<Context>()
        val sharedPrefs = context.getSharedPreferences("cocktailcraft_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(sharedPrefs)
    }
} 