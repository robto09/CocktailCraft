package com.cocktailcraft.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS implementation of NetworkMonitor.
 * For now, this is a simple implementation that always reports online.
 * TODO: Implement proper network monitoring using NWPathMonitor
 */
actual class NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    actual fun startMonitoring() {
        // For now, always report online
        _isOnline.value = true
    }
    
    actual fun stopMonitoring() {
        // No-op for now
    }
}