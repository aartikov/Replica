package me.aartikov.replica.advanced_sample.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.mobileup.kmm_form_validation.control.InputControl

@OptIn(FlowPreview::class)
fun InputControl.debouncedValue(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Eagerly,
    initialValue: String = value.value,
    timeoutMillis: (String) -> Long = { 500L },
    transform: (String) -> String = { it },
): StateFlow<String> = value
    .debounce(timeoutMillis)
    .map(transform)
    .stateIn(
        scope = scope,
        started = started,
        initialValue = initialValue
    )
