package me.aartikov.replica.sample.core.domain

import java.io.IOException

abstract class ApplicationException(message: String? = null, cause: Throwable?) :
    IOException(message, cause)

class ServerException(message: String?, cause: Throwable?) : ApplicationException(message, cause)

abstract class TransportException(cause: Throwable) : ApplicationException(null, cause)

class NoInternetException(cause: Throwable) : TransportException(cause)

class NoServerResponseException(cause: Throwable) : TransportException(cause)

class DeserializationException(cause: Throwable) : TransportException(cause)

class MatchingAppNotFoundException(cause: Throwable) : ApplicationException(null, cause)

class UnknownException(message: String?, cause: Throwable) : ApplicationException(message, cause)