package me.aartikov.replica.advanced_sample.core.network

import me.aartikov.replica.advanced_sample.core.debug_tools.DebugTools
import me.aartikov.replica.advanced_sample.core.error_handling.ApplicationException
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Converts platform exceptions to [ApplicationException]s. See [ErrorHandlingCall] for more details.
 */
class ErrorHandlingCallAdapterFactory(private val debugTools: DebugTools) : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (Call::class.java != getRawType(returnType)) return null
        check(returnType is ParameterizedType) {}
        return ErrorHandlingCallAdapter<Any>(
            responseType = getParameterUpperBound(0, returnType),
            debugTools = debugTools
        )
    }
}