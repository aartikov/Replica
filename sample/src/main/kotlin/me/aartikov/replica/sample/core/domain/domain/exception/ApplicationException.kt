package me.aartikov.replica.sample.core.domain.domain.exception

import java.io.IOException

abstract class ApplicationException(cause: Throwable? = null) : IOException(cause)

class ServerException(cause: Throwable? = null) : ApplicationException(cause)

abstract class TransportException(cause: Throwable? = null) : ApplicationException(cause)

class NoInternetException : TransportException()

class NoServerResponseException : TransportException()

class DeserializationException(cause: Throwable) : TransportException(cause)

class UnknownException(cause: Throwable, override val message: String) : ApplicationException(cause)