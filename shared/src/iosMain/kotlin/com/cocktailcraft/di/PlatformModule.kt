package com.cocktailcraft.di

import com.cocktailcraft.util.ImageLoader
import com.cocktailcraft.util.ImageLoaderFactory
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule() = module {
    single<Settings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
    
    single<ImageLoaderFactory> {
        ImageLoaderFactory()
    }
    
    single<ImageLoader> {
        get<ImageLoaderFactory>().createImageLoader()
    }
} 