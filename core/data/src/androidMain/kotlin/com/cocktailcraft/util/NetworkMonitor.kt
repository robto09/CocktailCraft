package com.cocktailcraft.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of NetworkMonitor.
 *
 * Tracks only the system's DEFAULT network (the one app traffic actually
 * uses) and counts it as online only once it is VALIDATED — the system has
 * confirmed it reaches the internet, so a captive portal or a Wi-Fi without
 * upstream doesn't read as online. Callbacks about any other network (a
 * stale one after a switch) are ignored, so they can't flip the state.
 */
class AndroidNetworkMonitor(private val context: Context) :
    BaseNetworkMonitor(),
    NetworkMonitor {

    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // The current default network; callbacks arrive on a binder thread.
    @Volatile
    private var defaultNetwork: Network? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            defaultNetwork = network
            // API 26+ follows up with onCapabilitiesChanged; on 24/25 that
            // isn't guaranteed, so read the capabilities now.
            _isOnline.value = connectivityManager.getNetworkCapabilities(network).isValidatedInternet()
        }

        override fun onLost(network: Network) {
            if (network != defaultNetwork) return
            defaultNetwork = null
            // A replacement default network, if any, arrives via onAvailable
            _isOnline.value = false
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            if (network != defaultNetwork) return
            _isOnline.value = networkCapabilities.isValidatedInternet()
        }
    }

    // Registering the same NetworkCallback twice throws IllegalArgumentException,
    // so start/stop are idempotent: the monitor is a Koin single shared by
    // several consumers and must tolerate repeated lifecycle calls.
    private var registered = false

    override fun startMonitoring() {
        if (registered) return
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        registered = true

        // Initialize with current status
        _isOnline.value = isNetworkAvailable()
    }

    override fun stopMonitoring() {
        if (!registered) return
        registered = false
        defaultNetwork = null
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        return connectivityManager.getNetworkCapabilities(activeNetwork).isValidatedInternet()
    }

    private fun NetworkCapabilities?.isValidatedInternet(): Boolean =
        this != null &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
