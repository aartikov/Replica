package me.aartikov.replica.sample.core.error_handling

/**
 * ApplicationException is a base class for application specific exceptions.
 * All exceptions occurred on a data are mapped to the application exceptions.
 */
abstract class ApplicationException(cause: Throwable?, message: String? = null) :
    Exception(message, cause)

/**
 * No access to data (access token invalid or expired)
 */
class UnauthorizedException(cause: Throwable) : ApplicationException(cause)

/**
 * Received a response from the server, but it is invalid - 4xx, 5xx
 */
class ServerException(cause: Throwable?, message: String? = null) :
    ApplicationException(cause, message)

/**
 * Data transfer error
 */
abstract class TransportException(cause: Throwable?) : ApplicationException(cause)

/**
 * Failed to connect to the server and the problem is most likely on the client
 */
class NoInternetException(cause: Throwable?) : TransportException(cause)

/**
 * Failed to connect to the server and the problem is most likely on the server
 */
class NoServerResponseException(cause: Throwable) : TransportException(cause)

/**
 *  Problems parsing json or lack of fields
 */
class DeserializationException(cause: Throwable) : TransportException(cause)

/**
 * Indicated that the client and server cannot agree on the desired level of security.
 * The problem may be on the server - the certificate has expired.
 * The problem may be on the client - verification of the date and time of collection is required.
 */
class SSLHandshakeException(cause: Throwable) : TransportException(cause)

/**
 * Could not find app for action
 */
class ExternalAppNotFoundException(cause: Throwable) : ApplicationException(cause)

/**
 * Some unknown issue
 */
class UnknownException(cause: Throwable, message: String) : ApplicationException(cause, message)