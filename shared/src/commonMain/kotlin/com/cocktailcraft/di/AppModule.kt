package com.cocktailcraft.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Main application module that combines all other modules.
 * This modular approach improves maintainability and testability.
 */
val appModule = listOf(
    networkModule,
    dataModule,
    domainModule
)
// The platformModule function is declared in PlatformModule.kt