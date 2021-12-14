package me.aartikov.replica.sample.core.data.network

import me.aartikov.replica.sample.core.domain.DeserializationException
import retrofit2.Converter

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
