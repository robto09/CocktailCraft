package com.cocktailcraft.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.native.HiddenFromObjC

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
@HiddenFromObjC
abstract class BaseNetworkMonitor {
    // Optimistically true only until the platform monitor reports:
    // Android overwrites it synchronously inside startMonitoring(); the iOS
    // path monitor fires its update handler with the CURRENT path immediately
    // after start, which is the real seed. A pessimistic (false) seed would
    // flash the offline banner at every launch and mark Home's first load
    // as served offline, which forces a second fetch once the real
    // (online) state arrives.
    protected val _isOnline = MutableStateFlow(true)
    open val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    abstract fun startMonitoring()
    abstract fun stopMonitoring()
}
