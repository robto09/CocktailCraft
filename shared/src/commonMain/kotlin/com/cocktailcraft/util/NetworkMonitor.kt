package com.cocktailcraft.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Common interface for monitoring network connectivity.
 * This is implemented differently for each platform using the expect/actual pattern.
 */
expect class NetworkMonitor {
    fun startMonitoring()
    fun stopMonitoring()
    val isOnline: StateFlow<Boolean>
    fun isOfflineModeEnabled(): Boolean
    fun setOfflineMode(enabled: Boolean)
}

/**
 * Base implementation with common functionality.
 * This provides shared implementation details for all platform-specific implementations.
 */
abstract class BaseNetworkMonitor {
    protected val _isOnline = MutableStateFlow(true)
    open val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    protected var _offlineModeEnabled = false

    abstract fun startMonitoring()
    abstract fun stopMonitoring()

    open fun isOfflineModeEnabled(): Boolean = _offlineModeEnabled

    open fun setOfflineMode(enabled: Boolean) {
        _offlineModeEnabled = enabled
    }
}
