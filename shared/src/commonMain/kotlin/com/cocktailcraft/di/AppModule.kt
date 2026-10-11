package com.cocktailcraft.di

import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/**
 * Main application module that combines all other modules.
 * This modular approach improves maintainability and testability.
 */
@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper (AR-12)
val appModule = listOf(
    networkModule,
    dataModule,
    domainModule,
    presentationModule
)
// The platformModule function is declared in PlatformModule.kt