package me.aartikov.replica.advanced_sample.core.debug_tools

import okhttp3.Interceptor

interface DebugTools {

    val interceptors: List<Interceptor>

    fun launch()
}