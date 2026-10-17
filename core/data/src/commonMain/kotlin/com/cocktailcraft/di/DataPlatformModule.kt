package com.cocktailcraft.di

import org.koin.core.module.Module

/**
 * The data layer's platform bindings: Settings and the secure store, the
 * app database's SqlDriver, the NetworkMonitor and the debug-aware AppConfig.
 * :shared exposes them to the apps as platformModule().
 */
expect fun dataPlatformModule(): Module
