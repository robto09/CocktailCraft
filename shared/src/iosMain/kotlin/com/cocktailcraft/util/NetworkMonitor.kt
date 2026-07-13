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
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
@kotlin.native.HiddenFromObjC // Swift resolves NetworkMonitor via KoinHelper; the concrete type stays Kotlin-side (AR-12)
class IOSNetworkMonitor : BaseNetworkMonitor(), NetworkMonitor {
    private var monitor: nw_path_monitor_t? = null
    private var queue: dispatch_queue_t? = null

    override fun startMonitoring() {
        // Idempotent: the monitor is a Koin single shared by several consumers;
        // a second start must not spawn a duplicate path monitor (SH-2).
        if (monitor != null) return

        log("NetworkMonitor: Starting network monitoring")

        // Create monitor and queue
        monitor = nw_path_monitor_create()
        queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)

        monitor?.let { mon ->
            queue?.let { q ->
                // Set up path update handler. It also fires immediately after
                // start with the CURRENT path, seeding BaseNetworkMonitor's
                // optimistic default with the real status (SH-5).
                nw_path_monitor_set_update_handler(mon) { path: nw_path_t? ->
                    val status = nw_path_get_status(path)
                    // Only `satisfied` is a usable connection. `satisfiable`
                    // means a connection *could* be established but no path is
                    // up right now — treating it as online produced
                    // false-positive connectivity and defeated the offline
                    // auto-switch (SH-5).
                    _isOnline.value = (status == nw_path_status_satisfied)
                    log("NetworkMonitor: Network status changed - isOnline: ${_isOnline.value}")
                }

                // Start monitoring
                nw_path_monitor_set_queue(mon, q)
                nw_path_monitor_start(mon)
            }
        }
    }

    override fun stopMonitoring() {
        log("NetworkMonitor: Stopping network monitoring")
        monitor?.let {
            nw_path_monitor_cancel(it)
            monitor = null
        }
        queue = null
    }

    // Connectivity chatter has no place in the release system log (SH-5).
    private fun log(message: String) {
        if (Platform.isDebugBinary) NSLog(message)
    }
}