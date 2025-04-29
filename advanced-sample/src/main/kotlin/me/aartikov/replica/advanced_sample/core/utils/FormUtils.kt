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
