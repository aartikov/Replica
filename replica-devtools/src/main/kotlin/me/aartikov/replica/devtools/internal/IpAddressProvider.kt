package me.aartikov.replica.devtools.internal

import android.content.Context
import android.net.ConnectivityManager
import java.net.Inet4Address

internal class IpAddressProvider(private val context: Context) {

    fun getLocalIpAddress(): String {
        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val linkProperties =
            connectivityManager.getLinkProperties(connectivityManager.activeNetwork)

        return linkProperties?.linkAddresses
            ?.firstOrNull { it.address is Inet4Address }?.address?.hostAddress ?: "localhost"
    }
}
