package com.cocktailcraft.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Common interface for monitoring network connectivity.
 */
interface NetworkMonitor {
    fun startMonitoring()
    fun stopMonitoring()
    val isOnline: StateFlow<Boolean>
}

/**
 * Base implementation with common functionality.
 */
abstract class BaseNetworkMonitor {
    protected val _isOnline = MutableStateFlow(true)
    open val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    abstract fun startMonitoring()
    abstract fun stopMonitoring()
}
