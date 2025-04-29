package com.cocktailcraft.util

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of NetworkMonitor.
 * This is a simplified implementation that doesn't actually monitor network connectivity.
 * A real implementation would use iOS's Reachability API.
 */
actual class NetworkMonitor : BaseNetworkMonitor() {
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    init {
        // Initialize offline mode from user defaults
        _offlineModeEnabled = userDefaults.boolForKey("offline_mode_enabled")
    }
    
    override fun startMonitoring() {
        // In a real implementation, this would set up Reachability monitoring
        // For now, we just assume the device is online
        _isOnline.value = true
    }
    
    override fun stopMonitoring() {
        // In a real implementation, this would tear down Reachability monitoring
    }
    
    override fun isOfflineModeEnabled(): Boolean {
        return _offlineModeEnabled
    }
    
    override fun setOfflineMode(enabled: Boolean) {
        _offlineModeEnabled = enabled
        // Save to user defaults
        userDefaults.setBool(enabled, "offline_mode_enabled")
    }
}
