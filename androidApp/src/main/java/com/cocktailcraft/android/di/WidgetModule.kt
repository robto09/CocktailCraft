package com.cocktailcraft.android.di

import com.cocktailcraft.android.widget.WidgetDataProvider
import org.koin.dsl.module

/**
 * Koin module for widget-related dependencies.
 * Provides WidgetDataProvider for widget access to repositories.
 */
val widgetModule = module {
    // Widget data provider - singleton to ensure consistent data access
    single { WidgetDataProvider.getInstance() }
}

