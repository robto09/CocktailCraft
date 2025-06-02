package com.cocktailcraft.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.*
import platform.darwin.dispatch_get_global_queue
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT

/**
 * iOS implementation of NetworkMonitor using native Network framework.
 */
class IOSNetworkMonitor : NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)
    
    override fun startMonitoring() {
        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            val isConnected = (status == nw_path_status_satisfied) || (status == nw_path_status_satisfiable)
            _isOnline.value = isConnected
        }
        
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }
    
    override fun stopMonitoring() {
        nw_path_monitor_cancel(monitor)
    }
}

/**
 * Factory function to create iOS NetworkMonitor.
 */
actual fun createNetworkMonitor(): NetworkMonitor = IOSNetworkMonitor()