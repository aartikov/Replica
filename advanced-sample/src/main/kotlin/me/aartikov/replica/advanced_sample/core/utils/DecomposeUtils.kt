package me.aartikov.replica.advanced_sample.core.utils

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import me.aartikov.replica.decompose.replicaObserverHost

/**
 * Creates a [ChildStack] with a single active component. Should be used to create a stack for Jetpack Compose preview.
 */
fun <T : Any> createFakeChildStack(instance: T): ChildStack<*, T> {
    return ChildStack(
        configuration = "<fake>",
        instance = instance
    )
}

fun <T : Any> createFakeChildStackStateFlow(instance: T): StateFlow<ChildStack<*, T>> {
    return MutableStateFlow(createFakeChildStack(instance))
}

/**
 * Converts [Value] from Decompose to [StateFlow] from Jetpack Compose.
 */
fun <T : Any> Value<T>.toStateFlow(lifecycle: Lifecycle): StateFlow<T> {
    val state: MutableStateFlow<T> = MutableStateFlow(this.value)

    if (lifecycle.state != Lifecycle.State.DESTROYED) {
        val observer = { value: T -> state.value = value }
        val cancellation = subscribe(observer)
        lifecycle.doOnDestroy {
            cancellation.cancel()
        }
    }

    return state
}

/**
 * Creates a coroutine scope tied to Decompose lifecycle. A scope is canceled when a component is destroyed.
 */
val LifecycleOwner.componentCoroutineScope: CoroutineScope
    get() = lifecycle.replicaObserverHost().observerCoroutineScope

/**
 * A helper function to save and restore component state.
 */
inline fun <T : Any> StateKeeperOwner.persistent(
    key: String = "PersistentState",
    serializer: KSerializer<T>,
    noinline save: () -> T,
    restore: (T) -> Unit,
) {
    stateKeeper.consume(key, serializer)?.run(restore)
    stateKeeper.register(key, serializer, save)
}
