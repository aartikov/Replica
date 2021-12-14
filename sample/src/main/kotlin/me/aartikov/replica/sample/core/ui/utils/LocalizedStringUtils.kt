package me.aartikov.replica.sample.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import me.aartikov.sesame.localizedstring.LocalizedString

@Composable
fun LocalizedString.resolve(): String {
    LocalConfiguration.current
    return resolve(LocalContext.current).toString()
}