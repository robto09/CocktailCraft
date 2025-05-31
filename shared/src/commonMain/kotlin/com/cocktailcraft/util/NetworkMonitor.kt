package com.cocktailcraft.util

import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Network monitor to observe network connectivity status using the Connectivity library.
 */
class NetworkMonitor {
    private val connectivity = Connectivity()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    fun startMonitoring() {
        connectivity.start()
        
        scope.launch {
            connectivity.statusUpdates.collect { status ->
                _isOnline.value = when (status) {
                    is Connectivity.Status.Connected -> true
                    is Connectivity.Status.Disconnected -> false
                }
            }
        }
    }
    
    fun stopMonitoring() {
        connectivity.stop()
    }
}
