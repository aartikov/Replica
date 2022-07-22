package me.aartikov.replica.sample.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import me.aartikov.sesame.localizedstring.LocalizedString

/**
 * Resolves [LocalizedString] in Jetpack Compose UI.
 */
@Composable
fun LocalizedString.resolve(): String {
    LocalConfiguration.current // required to resolve a string again on configuration change
    return resolve(LocalContext.current).toString()
}