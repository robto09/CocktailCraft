package com.cocktailcraft.di

import org.koin.core.module.Module

/**
 * Bindings each platform supplies natively (Settings, the secure store, the
 * app database's SqlDriver, NetworkMonitor, the debug-aware AppConfig). They
 * are all data-layer adapters, so the module itself lives in :core:data;
 * this keeps the name androidApp and initKoin() use.
 */
fun platformModule(): Module = dataPlatformModule()
