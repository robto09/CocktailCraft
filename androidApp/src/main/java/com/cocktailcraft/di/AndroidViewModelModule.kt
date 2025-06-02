package com.cocktailcraft.di

import org.koin.dsl.module

/**
 * Android-specific Koin module.
 * With kmp-viewmodel, we now use the shared ViewModels directly.
 * This module is kept for any future Android-specific dependencies.
 */
val androidViewModelModule = module {
    // All ViewModels are now provided by the shared viewModelModule
    // using kmp-viewmodel which works seamlessly with Android
}