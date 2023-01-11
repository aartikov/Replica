package me.aartikov.replica.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Provides network connectivity status
 */
interface NetworkConnectivityProvider {
    val connectedFlow: StateFlow<Boolean>
}

val NetworkConnectivityProvider.connected: Boolean get() = connectedFlow.value