package me.aartikov.replica.advanced_sample.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.mobileup.kmm_form_validation.control.InputControl
import ru.mobileup.kmm_form_validation.options.ImeAction
import ru.mobileup.kmm_form_validation.options.KeyboardCapitalization
import ru.mobileup.kmm_form_validation.options.KeyboardOptions
import ru.mobileup.kmm_form_validation.options.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions as ComposeKeyboardOptions
import androidx.compose.ui.text.input.ImeAction as ComposeImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization as ComposeKeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType as ComposeKeyboardType


@OptIn(DelicateCoroutinesApi::class)
fun fakeInputControl(initialValue: String = "") = InputControl(GlobalScope, initialValue)

@OptIn(FlowPreview::class)
fun InputControl.debouncedValue(
    scope: CoroutineScope,
    timeoutMillis: Long = 500L,
    transform: (String) -> String = { it },
): StateFlow<String> = value
    .debounce { if (it.isBlank()) 0L else timeoutMillis }
    .map(transform)
    .stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = value.value
    )

fun KeyboardOptions.toCompose(): ComposeKeyboardOptions {
    return ComposeKeyboardOptions(
        capitalization = capitalization.toCompose(),
        autoCorrectEnabled = autoCorrect,
        keyboardType = keyboardType.toCompose(),
        imeAction = imeAction.toCompose()
    )
}

private fun KeyboardCapitalization.toCompose(): ComposeKeyboardCapitalization {
    return when (this) {
        KeyboardCapitalization.None -> ComposeKeyboardCapitalization.None
        KeyboardCapitalization.Characters -> ComposeKeyboardCapitalization.Characters
        KeyboardCapitalization.Words -> ComposeKeyboardCapitalization.Words
        KeyboardCapitalization.Sentences -> ComposeKeyboardCapitalization.Sentences
    }
}

private fun KeyboardType.toCompose(): ComposeKeyboardType {
    return when (this) {
        KeyboardType.Text -> ComposeKeyboardType.Text
        KeyboardType.Ascii -> ComposeKeyboardType.Ascii
        KeyboardType.Email -> ComposeKeyboardType.Email
        KeyboardType.Uri -> ComposeKeyboardType.Uri
        KeyboardType.Number -> ComposeKeyboardType.Number
        KeyboardType.NumberPassword -> ComposeKeyboardType.NumberPassword
        KeyboardType.Password -> ComposeKeyboardType.Password
        KeyboardType.Phone -> ComposeKeyboardType.Phone
    }
}

private fun ImeAction.toCompose(): ComposeImeAction {
    return when (this) {
        ImeAction.Default -> ComposeImeAction.Default
        ImeAction.None -> ComposeImeAction.None
        ImeAction.Search -> ComposeImeAction.Search
        ImeAction.Go -> ComposeImeAction.Go
        ImeAction.Done -> ComposeImeAction.Done
        ImeAction.Next -> ComposeImeAction.Next
        ImeAction.Send -> ComposeImeAction.Send
        ImeAction.Previous -> ComposeImeAction.Previous
    }
}
