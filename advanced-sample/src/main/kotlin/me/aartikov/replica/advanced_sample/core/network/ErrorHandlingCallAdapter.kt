package me.aartikov.replica.advanced_sample.core.network

import me.aartikov.replica.advanced_sample.core.error_handling.ApplicationException
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

/**
 * Converts platform exceptions to [ApplicationException]s. See [ErrorHandlingCall] for more details.
 */
internal class ErrorHandlingCallAdapter<R>(
    private val responseType: Type
) : CallAdapter<R, Call<R>> {

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<R>): Call<R> = ErrorHandlingCall(
        sourceCall = call
    )
}