package me.aartikov.replica.devtools.internal

import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter

internal class IpAddressProvider(private val context: Context) {

    fun getLocalIpAddress(): String {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
    }
}