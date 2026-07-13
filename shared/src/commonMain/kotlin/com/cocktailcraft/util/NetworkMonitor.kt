package com.cocktailcraft.util

import kotlin.native.HiddenFromObjC

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
@HiddenFromObjC
abstract class BaseNetworkMonitor {
    // Optimistically true only until the platform monitor reports (SH-5):
    // Android overwrites it synchronously inside startMonitoring(); the iOS
    // path monitor fires its update handler with the CURRENT path immediately
    // after start, which is the real seed. A pessimistic (false) seed was
    // rejected deliberately — a false "offline" blip at launch would trip the
    // ViewModels' auto-enable-offline-mode side effect, which is sticky.
    protected val _isOnline = MutableStateFlow(true)
    open val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    abstract fun startMonitoring()
    abstract fun stopMonitoring()
}
