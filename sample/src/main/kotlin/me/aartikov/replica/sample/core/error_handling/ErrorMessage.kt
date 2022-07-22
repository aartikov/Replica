package me.aartikov.replica.sample.core.error_handling

import me.aartikov.replica.sample.BuildConfig
import me.aartikov.replica.sample.R
import me.aartikov.sesame.localizedstring.LocalizedString

/**
 * Returns human readable messages for exceptions.
 */
val Exception.errorMessage: LocalizedString
    get() = when (this) {
        is ServerException -> this.message?.let { LocalizedString.raw(it) }
            ?: LocalizedString.resource(R.string.error_invalid_response)

        is DeserializationException -> LocalizedString.resource(R.string.error_invalid_response)

        is NoServerResponseException -> LocalizedString.resource(R.string.error_no_server_response)

        is NoInternetException -> LocalizedString.resource(R.string.error_no_internet_connection)

        is SSLHandshakeException -> LocalizedString.resource(R.string.error_ssl_handshake)

        is ExternalAppNotFoundException -> LocalizedString.resource(R.string.error_matching_application_not_found)

        else -> {
            val description = this.message
            if (description != null && BuildConfig.DEBUG) {
                LocalizedString.resource(R.string.error_unexpected_with_description, description)
            } else {
                LocalizedString.resource(R.string.error_unexpected)
            }
        }
    }