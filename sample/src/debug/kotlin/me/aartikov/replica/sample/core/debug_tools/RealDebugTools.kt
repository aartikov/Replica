package me.aartikov.replica.sample.core.debug_tools

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.ReplicaDevTools
import me.nemiron.hyperion.networkemulation.NetworkEmulatorInterceptor
import okhttp3.Interceptor

class RealDebugTools(
    context: Context,
    replicaClient: ReplicaClient
) : DebugTools {

    private val networkEmulatorInterceptor = NetworkEmulatorInterceptor(context)
    private val replicaDebugTools = ReplicaDevTools(replicaClient, context)

    private val chuckerCollector = ChuckerCollector(
        context = context,
        showNotification = false,
        retentionPeriod = RetentionManager.Period.ONE_HOUR
    )

    private val chuckerInterceptor = ChuckerInterceptor
        .Builder(context)
        .collector(chuckerCollector)
        .build()

    override val interceptors: List<Interceptor> = listOf(
        networkEmulatorInterceptor,
        chuckerInterceptor
    )

    override fun launch() {
        replicaDebugTools.launch()
    }

    @Suppress("DEPRECATION")
    override fun collectNetworkError(exception: Exception) {
        chuckerCollector.onError("DebugTools", exception)
    }
}