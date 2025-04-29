package com.cocktailcraft.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Main application module that combines all other modules.
 * This modular approach improves maintainability and testability.
 *
 * The modules are organized following Clean Architecture principles:
 *
 * 1. Infrastructure/Framework Layer:
 * - commonModule: Common dependencies shared across all modules (JSON, logging, etc.)
 * - networkModule: Network-related dependencies (HTTP client, API interfaces)
 * - platformModule: Platform-specific implementations (added separately when initializing Koin)
 *
 * 2. Data Layer:
 * - dataModule: Repository implementations and data sources
 *
 * 3. Domain Layer:
 * - domainModule: Domain models, interfaces, and business rules
 * - useCaseModule: Use cases that orchestrate the flow of data between UI and data layers
 *
 * This organization ensures proper separation of concerns and dependency flow
 * from outer layers (UI, infrastructure) to inner layers (domain).
 *
 * For iOS compatibility, all modules use the expect/actual pattern where needed
 * and avoid platform-specific dependencies in shared code.
 */
val appModule = listOf(
    // Infrastructure/Framework Layer
    commonModule,
    networkModule,

    // Data Layer
    dataModule,

    // Domain Layer
    domainModule,
    useCaseModule
)
// The platformModule function is declared in PlatformModule.kt and added separately