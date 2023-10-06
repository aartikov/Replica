package me.aartikov.replica.advanced_sample.core.network

import kotlinx.serialization.SerializationException
import me.aartikov.replica.advanced_sample.core.error_handling.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.HttpURLConnection.HTTP_UNAVAILABLE
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

/**
 * Converts platform exceptions to [ApplicationException]s.
 */
class ErrorHandlingCall<T>(
    private val sourceCall: Call<T>
) : Call<T> by sourceCall {

    override fun enqueue(callback: Callback<T>) {
        sourceCall.enqueue(getEnqueuedCallback(callback))
    }

    private fun getEnqueuedCallback(callback: Callback<T>) = object : Callback<T> {

        override fun onResponse(call: Call<T>, response: Response<T>) {
            when (response.isSuccessful) {
                true -> callback.onResponse(call, response)
                else -> {
                    val exception = mapToFailureException(response)
                    callback.onFailure(call, exception)
                }
            }
        }

        override fun onFailure(call: Call<T>, throwable: Throwable) {
            val exception = mapToFailureException(throwable)
            callback.onFailure(call, exception)
        }

        private fun mapToFailureException(response: Response<T>) = when (response.code()) {
            HTTP_GATEWAY_TIMEOUT, HTTP_UNAVAILABLE -> NoServerResponseException(
                HttpException(response)
            )
            HTTP_UNAUTHORIZED -> UnauthorizedException(HttpException(response))
            else -> ServerException(HttpException(response))
        }

        private fun mapToFailureException(throwable: Throwable) = when (throwable) {
            is ApplicationException -> throwable
            is SerializationException -> DeserializationException(throwable)
            is SocketTimeoutException -> NoServerResponseException(throwable)
            is SSLHandshakeException -> SSLHandshakeException(throwable)
            is IOException -> (throwable.cause as? ApplicationException)
                ?: NoInternetException(throwable)
            else -> UnknownException(throwable, throwable.message ?: "Unknown")
        }
    }
}