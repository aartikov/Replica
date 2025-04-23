package me.aartikov.replica.devtools

import android.app.Application
import android.content.Context
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.internal.ReplicaDevToolsImpl

/**
 * Debugging tool for Replica library.
 *
 * To use ReplicaDevTools:
 *
 * 1) Integrate [ReplicaDevTools] to mobile application that uses Replica.
 * To do it, create [ReplicaDevTools] and [launch] it in [Application.onCreate].
 * 2) Make sure that a mobile phone and a desktop are connected to the same wifi-network.
 * 3) Launch the mobile application.
 * 4) Find string "ReplicaDevTools is available with address: <some_url>" in a Logcat and open "<some_url>" in a web-browser.
 * 5) Navigate in the app and observe replica states in a web-browser.
 *
 * If the device is connected via USB and connection is unavailable,
 * you can still use ReplicaDevTools by forwarding the port via ADB:
 *
 *    adb -s <device_name> forward tcp:8080 tcp:8080
 *
 * Then open http://localhost:8080 in a desktop browser.
 */
interface ReplicaDevTools {
    /**
     * Launches ReplicaDevTools
     */
    fun launch()
}

/**
 * Creates [ReplicaDevTools].
 */
fun ReplicaDevTools(
    replicaClient: ReplicaClient,
    applicationContext: Context,
    settings: DevToolsSettings = DevToolsSettings()
): ReplicaDevTools {
    return ReplicaDevToolsImpl(replicaClient, settings, applicationContext)
}