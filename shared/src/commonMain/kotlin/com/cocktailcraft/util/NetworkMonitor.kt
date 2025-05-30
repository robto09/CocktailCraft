package com.cocktailcraft.util

import kotlinx.coroutines.flow.StateFlow

/**
 * Common interface for monitoring network connectivity.
 */
expect class NetworkMonitor {
    fun startMonitoring()
    fun stopMonitoring()
    val isOnline: StateFlow<Boolean>
}
