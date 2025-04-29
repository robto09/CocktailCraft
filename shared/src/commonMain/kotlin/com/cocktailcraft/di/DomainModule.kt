package com.cocktailcraft.di

import com.cocktailcraft.data.config.AppConfigImpl
import com.cocktailcraft.domain.config.AppConfig
import org.koin.dsl.module

/**
 * Koin module for domain-related dependencies including domain configurations.
 * Use cases have been moved to the dedicated UseCaseModule.
 */
val domainModule = module {
    // Config
    single<AppConfig> { AppConfigImpl() }
}
