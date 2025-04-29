package com.cocktailcraft.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Main application module that combines all other modules.
 * This modular approach improves maintainability and testability.
 *
 * The modules are organized as follows:
 * - commonModule: Common dependencies shared across all modules
 * - networkModule: Network-related dependencies
 * - dataModule: Data-layer dependencies
 * - domainModule: Domain-layer dependencies
 * - useCaseModule: Use case dependencies
 *
 * Platform-specific modules are added separately when initializing Koin.
 */
val appModule = listOf(
    commonModule,
    networkModule,
    dataModule,
    domainModule,
    useCaseModule
)
// The platformModule function is declared in PlatformModule.kt