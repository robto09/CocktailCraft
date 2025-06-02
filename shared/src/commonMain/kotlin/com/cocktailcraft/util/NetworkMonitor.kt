package com.cocktailcraft.util

import kotlinx.coroutines.flow.StateFlow

/**
 * Network monitor to observe network connectivity status.
 * Platform-specific implementations handle the actual monitoring.
 */
interface NetworkMonitor {
    val isOnline: StateFlow<Boolean>
    
    fun startMonitoring()
    fun stopMonitoring()
}

/**
 * Factory function to create platform-specific NetworkMonitor instances.
 */
expect fun createNetworkMonitor(): NetworkMonitor