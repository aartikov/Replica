package me.aartikov.replica.devtools.internal

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter

internal class IpAddressProvider(context: Context) {

    @SuppressLint("WifiManagerPotentialLeak")
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun getLocalIpAddress(): String {
        return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }
}