package me.aartikov.replica.network

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Provides network connectivity status for Android applications.
 */
class AndroidNetworkConnectivityProvider(
    application: Application,
    private val disconnectionDebounce: Duration = 500.milliseconds
) : NetworkConnectivityProvider {

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val availableNetworks = mutableSetOf<Network>()

    private val _connectedFlow = MutableStateFlow(isConnected())
    override val connectedFlow: StateFlow<Boolean> get() = _connectedFlow

    private val handler = Handler(Looper.getMainLooper())

    private val processLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            availableNetworks.clear()
            setConnected(isConnected(), debounceIfDisconnected = false)
            registerNetworkCallback()
        }

        override fun onStop(owner: LifecycleOwner) {
            unregisterNetworkCallback()
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            availableNetworks.add(network)
            setConnected(value = availableNetworks.isNotEmpty(), debounceIfDisconnected = true)
        }

        override fun onLost(network: Network) {
            availableNetworks.remove(network)
            setConnected(value = availableNetworks.isNotEmpty(), debounceIfDisconnected = true)
        }
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(processLifecycleObserver)
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private val setDisconnectedRunnable = Runnable { _connectedFlow.value = false }

    private fun setConnected(value: Boolean, debounceIfDisconnected: Boolean) {
        handler.removeCallbacks(setDisconnectedRunnable)

        if (value || !debounceIfDisconnected) {
            _connectedFlow.value = value
        } else {
            handler.postDelayed(setDisconnectedRunnable, disconnectionDebounce.inWholeMilliseconds)
        }
    }

    private fun isConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }
}