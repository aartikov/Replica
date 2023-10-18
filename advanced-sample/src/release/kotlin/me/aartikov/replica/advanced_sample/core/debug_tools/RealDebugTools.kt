package me.aartikov.replica.advanced_sample.core.debug_tools

import android.content.Context
import me.aartikov.replica.client.ReplicaClient
import okhttp3.Interceptor

@Suppress("UNUSED_PARAMETER")
class RealDebugTools(
    context: Context,
    replicaClient: ReplicaClient
) : DebugTools {

    override val interceptors: List<Interceptor> = emptyList()

    override fun launch() {
        // do nothing
    }
}