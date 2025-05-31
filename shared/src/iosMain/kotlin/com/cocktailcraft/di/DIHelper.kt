package com.cocktailcraft.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Helper functions for iOS Koin initialization.
 * These functions are called from Swift to set up dependency injection.
 */
object DIHelper {
    /**
     * Initialize Koin with all required modules for iOS.
     * This should be called once during app startup.
     */
    fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
        startKoin {
            appDeclaration()
            modules(appModule + platformModule())
        }
    }
}

/**
 * Top-level function for easier Swift interop.
 * In Swift, this will be available as DIHelperKt.doInitKoin()
 */
fun doInitKoin() {
    DIHelper.initKoin()
}

/**
 * Provides a CoroutineScope for iOS usage
 * This scope is used for collecting flows from Swift
 */
private val iosScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

fun getIOSScope(): CoroutineScope = iosScope