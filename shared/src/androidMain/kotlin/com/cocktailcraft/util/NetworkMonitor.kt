package com.cocktailcraft.util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of NetworkMonitor.
 */
actual class NetworkMonitor(
    private val context: Context
) : BaseNetworkMonitor() {

    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val prefs: SharedPreferences = context.getSharedPreferences("network_prefs", Context.MODE_PRIVATE)

    init {
        // Initialize offline mode from preferences
        _offlineModeEnabled = prefs.getBoolean("offline_mode_enabled", false)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            // Only set offline if there are no other networks available
            if (!isNetworkAvailable()) {
                _isOnline.value = false
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            // Update online status based on internet capability
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (_isOnline.value != hasInternet) {
                _isOnline.value = hasInternet
            }
        }
    }

    override fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Initialize with current status
        _isOnline.value = isNetworkAvailable()
    }

    override fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }

    override fun isOfflineModeEnabled(): Boolean {
        return _offlineModeEnabled
    }

    override fun setOfflineMode(enabled: Boolean) {
        _offlineModeEnabled = enabled
        // Save to preferences
        prefs.edit().putBoolean("offline_mode_enabled", enabled).apply()
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
