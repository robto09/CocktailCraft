package com.cocktailcraft.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Common interface for monitoring network connectivity.
 * Currently only implemented for Android.
 */
expect class NetworkMonitor(context: Context) {
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
