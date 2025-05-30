package com.cocktailcraft.di

import com.cocktailcraft.util.NetworkMonitor

actual fun createNetworkMonitor(): NetworkMonitor {
    return NetworkMonitor()
}