package me.aartikov.replica.sample.core.data.network

import kotlinx.serialization.SerializationException
import me.aartikov.replica.sample.core.domain.*
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return try {
            val response = chain.proceed(request)
            if (response.isSuccessful) {
                response
            } else {
                throw HttpException(
                    retrofit2.Response.error<ResponseBody>(
                        response.code,
                        (response.body?.string() ?: "").toResponseBody()
                    )
                )
            }
        } catch (e: Exception) {
            throw when (e) {
                is HttpException -> ServerException(e)

                is SerializationException -> DeserializationException(e)

                is UnknownHostException -> NoInternetException(e)

                is SocketTimeoutException -> NoServerResponseException(e)

                is IOException -> NoInternetException(e)

                else -> UnknownException(e, e.message.orEmpty())
            }
        }
    }
}