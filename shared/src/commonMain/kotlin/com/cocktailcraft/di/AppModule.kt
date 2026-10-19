package com.cocktailcraft.di

import com.cocktailcraft.widget.WidgetDataSource
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/** What the home-screen widgets show, on both platforms. */
@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper (AR-12)
val widgetDataModule = module {
    single {
        WidgetDataSource(detailRepository = get(), offlineRepository = get(), favoritesRepository = get())
    }
}

/**
 * Main application module that combines all other modules.
 * This modular approach improves maintainability and testability.
 */
@HiddenFromObjC // Kotlin-side wiring; Swift only calls doInitKoin()/KoinHelper (AR-12)
val appModule = listOf(
    networkModule,
    dataModule,
    domainModule,
    presentationModule,
    widgetDataModule
)
// The platformModule function is declared in PlatformModule.kt