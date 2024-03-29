package me.aartikov.replica.advanced_sample.core.debug_tools

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import me.aartikov.replica.advanced_sample.core.error_handling.ServerException
import me.aartikov.replica.client.ReplicaClient
import me.aartikov.replica.devtools.ReplicaDevTools
import me.nemiron.hyperion.networkemulation.NetworkEmulatorInterceptor
import okhttp3.Interceptor
import java.io.IOException

class RealDebugTools(
    context: Context,
    replicaClient: ReplicaClient
) : DebugTools {

    private val networkEmulatorInterceptor = NetworkEmulatorInterceptor(
        context,
        failureExceptionProvider = {
            IOException(ServerException(message = "Emulated failure"))
        }
    )

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
}