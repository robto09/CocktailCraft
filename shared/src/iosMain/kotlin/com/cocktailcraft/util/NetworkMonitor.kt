package com.cocktailcraft.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.*
import platform.darwin.*

/**
 * iOS implementation of NetworkMonitor using NWPathMonitor for real network status monitoring.
 */
actual class NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private var pathMonitor: nw_path_monitor_t? = null
    private var monitorQueue: dispatch_queue_t? = null
    
    actual fun startMonitoring() {
        // Create the network path monitor
        pathMonitor = nw_path_monitor_create()
        
        // Create a dispatch queue for the monitor
        monitorQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)
        
        // Set up the update handler
        val monitor = pathMonitor
        if (monitor != null) {
            nw_path_monitor_set_update_handler(monitor) { path ->
                val status = nw_path_get_status(path)
                _isOnline.value = (status == nw_path_status_satisfied || status == nw_path_status_satisfiable)
            }
            
            // Start monitoring on the queue
            val queue = monitorQueue
            if (queue != null) {
                nw_path_monitor_set_queue(monitor, queue)
                nw_path_monitor_start(monitor)
            }
        }
    }
    
    actual fun stopMonitoring() {
        val monitor = pathMonitor
        if (monitor != null) {
            nw_path_monitor_cancel(monitor)
            pathMonitor = null
        }
        monitorQueue = null
    }
}