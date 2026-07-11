package com.cocktailcraft.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.*
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSLog
import platform.darwin.dispatch_get_global_queue
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_queue_t

/**
 * iOS implementation of NetworkMonitor using Network framework
 */
class IOSNetworkMonitor : BaseNetworkMonitor(), NetworkMonitor {
    private var monitor: nw_path_monitor_t? = null
    private var queue: dispatch_queue_t? = null
    
    override fun startMonitoring() {
        NSLog("NetworkMonitor: Starting network monitoring")
        
        // Create monitor and queue
        monitor = nw_path_monitor_create()
        queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)
        
        monitor?.let { mon ->
            queue?.let { q ->
                // Set up path update handler
                nw_path_monitor_set_update_handler(mon) { path: nw_path_t? ->
                    val status = nw_path_get_status(path)
                    _isOnline.value = (status == nw_path_status_satisfied || status == nw_path_status_satisfiable)
                    NSLog("NetworkMonitor: Network status changed - isOnline: ${_isOnline.value}")
                }
                
                // Start monitoring
                nw_path_monitor_set_queue(mon, q)
                nw_path_monitor_start(mon)
            }
        }
    }
    
    override fun stopMonitoring() {
        NSLog("NetworkMonitor: Stopping network monitoring")
        monitor?.let {
            nw_path_monitor_cancel(it)
            monitor = null
        }
        queue = null
    }
}