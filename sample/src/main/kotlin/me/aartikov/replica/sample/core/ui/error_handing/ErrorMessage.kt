package me.aartikov.replica.sample.core.ui.error_handing

import me.aartikov.replica.sample.BuildConfig
import me.aartikov.replica.sample.R
import me.aartikov.replica.sample.core.domain.*
import me.aartikov.sesame.localizedstring.LocalizedString

val Exception.errorMessage: LocalizedString
    get() = when (this) {
        is ServerException, is DeserializationException -> LocalizedString.resource(R.string.error_invalid_response)

        is NoServerResponseException -> LocalizedString.resource(R.string.error_no_server_response)

        is NoInternetException -> LocalizedString.resource(R.string.error_no_internet_connection)

        is MatchingAppNotFoundException -> LocalizedString.resource(R.string.error_matching_app_not_found)

        else -> {
            val description = this.message
            if (description != null && BuildConfig.DEBUG) {
                LocalizedString.resource(R.string.error_unexpected_with_description, description)
            } else {
                LocalizedString.resource(R.string.error_unexpected)
            }
        }
    }