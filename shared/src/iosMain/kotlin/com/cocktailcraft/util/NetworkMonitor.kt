package com.cocktailcraft.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * iOS implementation of NetworkMonitor.
 * For now, this uses a simplified approach with URLSession to check connectivity.
 * TODO: Implement proper network monitoring using NWPathMonitor or SCNetworkReachability
 * when Kotlin/Native interop issues are resolved.
 */
actual class NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private var isMonitoring = false
    
    actual fun startMonitoring() {
        isMonitoring = true
        
        // Initial check
        checkConnectivity()
        
        // Periodic monitoring
        GlobalScope.launch {
            while (isMonitoring) {
                kotlinx.coroutines.delay(10000L) // Check every 10 seconds
                checkConnectivity()
            }
        }
    }
    
    private fun checkConnectivity() {
        // Try to reach a reliable endpoint
        val url = NSURL.URLWithString("https://www.google.com")
        if (url != null) {
            val request = NSMutableURLRequest(uRL = url)
            request.setHTTPMethod("HEAD")
            request.setTimeoutInterval(5.0)
            request.setCachePolicy(NSURLRequestReloadIgnoringLocalCacheData)
            
            val session = NSURLSession.sharedSession
            val task = session.dataTaskWithRequest(request) { _, response, error ->
                val httpResponse = response as? NSHTTPURLResponse
                val isConnected = error == null && httpResponse?.statusCode?.let { 
                    it >= 200L && it < 300L 
                } ?: false
                
                _isOnline.value = isConnected
            }
            
            task.resume()
        }
    }
    
    actual fun stopMonitoring() {
        isMonitoring = false
    }
}