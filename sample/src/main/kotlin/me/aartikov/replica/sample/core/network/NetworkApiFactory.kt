package me.aartikov.replica.sample.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import me.aartikov.replica.sample.BuildConfig
import me.aartikov.replica.sample.core.debug_tools.DebugTools
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Creates implementations of Retrofit APIs.
 */
class NetworkApiFactory(
    private val baseUrl: String,
    private val debugTools: DebugTools
) {

    companion object {
        private const val CONNECT_TIMEOUT_SECONDS = 30L
        private const val READ_TIMEOUT_SECONDS = 60L
        private const val WRITE_TIMEOUT_SECONDS = 60L
    }

    private val json = createJson()
    private val okHttpClient = createOkHttpClient()
    private val retrofit = createRetrofit(okHttpClient)

    inline fun <reified T : Any> createApi(): T = createApi(T::class.java)

    fun <T : Any> createApi(clazz: Class<T>): T {
        return retrofit.create(clazz)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addCallAdapterFactory(ErrorHandlingCallAdapterFactory(debugTools))
            .addConverterFactory(ErrorHandlingConverterFactory(json.asConverterFactory("application/json".toMediaType())))
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

                if (BuildConfig.DEBUG) {
                    addNetworkInterceptor(createLoggingInterceptor())
                    debugTools.interceptors.forEach { addInterceptor(it) }
                }
            }
            .build()
    }

    private fun createLoggingInterceptor(): Interceptor {
        return HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createJson(): Json {
        return Json {
            explicitNulls = false
            encodeDefaults = true
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}