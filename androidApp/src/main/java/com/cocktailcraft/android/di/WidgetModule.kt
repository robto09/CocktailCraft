package com.cocktailcraft.android.di

import com.cocktailcraft.android.widget.WidgetDataProvider
import org.koin.dsl.module

/**
 * Koin module for widget-related dependencies. WidgetDataProvider is only
 * ever obtained from here (the widget worker and refresh actions inject it).
 */
val widgetModule = module {
    single {
        WidgetDataProvider(
            detailRepository = get(),
            offlineRepository = get(),
            favoritesRepository = get()
        )
    }
}
