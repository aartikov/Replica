package me.aartikov.replica.simple_sample.core.error_handling

import me.aartikov.replica.simple_sample.BuildConfig
import me.aartikov.replica.simple_sample.R
import me.aartikov.sesame.localizedstring.LocalizedString
import java.io.IOException

/**
 * Returns human readable messages for exceptions.
 */
val Exception.errorMessage: LocalizedString
    get() = when (this) {

        is IOException -> LocalizedString.resource(R.string.error_no_internet_connection)

        else -> {
            val description = this.message
            if (description != null && BuildConfig.DEBUG) {
                LocalizedString.resource(R.string.error_unknown_with_description, description)
            } else {
                LocalizedString.resource(R.string.error_unknown)
            }
        }
    }