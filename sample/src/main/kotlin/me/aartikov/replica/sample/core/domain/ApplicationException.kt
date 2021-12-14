package me.aartikov.replica.sample.core.domain

import java.io.IOException

abstract class ApplicationException(cause: Throwable) : IOException(cause)

class ServerException(cause: Throwable) : ApplicationException(cause)

abstract class TransportException(cause: Throwable) : ApplicationException(cause)

class NoInternetException(cause: Throwable) : TransportException(cause)

class NoServerResponseException(cause: Throwable) : TransportException(cause)

class DeserializationException(cause: Throwable) : TransportException(cause)

class MatchingAppNotFoundException(cause: Throwable) : ApplicationException(cause)

class UnknownException(cause: Throwable, override val message: String) : ApplicationException(cause)