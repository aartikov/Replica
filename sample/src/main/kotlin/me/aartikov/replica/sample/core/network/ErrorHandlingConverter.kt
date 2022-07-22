package me.aartikov.replica.sample.core.network

import me.aartikov.replica.sample.core.error_handling.DeserializationException
import retrofit2.Converter

/**
 * Converts exceptions of JSON-framework to [DeserializationException].
 */
class ErrorHandlingConverter<F : Any, T : Any>(private val converter: Converter<F, T>) :
    Converter<F, T> {

    override fun convert(value: F): T? {
        return try {
            converter.convert(value)
        } catch (e: Exception) {
            throw DeserializationException(e)
        }
    }
}
