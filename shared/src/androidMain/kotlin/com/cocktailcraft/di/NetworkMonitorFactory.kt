package com.cocktailcraft.di

import android.content.Context
import com.cocktailcraft.util.NetworkMonitor
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun createNetworkMonitor(): NetworkMonitor {
    val koinComponent = object : KoinComponent {}
    return NetworkMonitor(koinComponent.get<Context>())
}