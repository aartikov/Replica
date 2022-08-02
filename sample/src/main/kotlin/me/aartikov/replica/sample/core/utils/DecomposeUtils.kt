package me.aartikov.replica.sample.core.utils

import android.os.Parcelable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import com.arkivanov.essenty.statekeeper.consume
import kotlinx.coroutines.CoroutineScope
import me.aartikov.replica.decompose.coroutineScope

/**
 * Creates a [ChildStack] with a single active component. Should be used to create a stack for Jetpack Compose preview.
 */
fun <T : Any> createFakeChildStack(instance: T): ChildStack<*, T> {
    return ChildStack(
        configuration = "<fake>",
        instance = instance
    )
}

/**
 * Converts [Value] from Decompose to [State] from Jetpack Compose.
 */
fun <T : Any> Value<T>.toComposeState(lifecycle: Lifecycle): State<T> {
    val state: MutableState<T> = mutableStateOf(this.value)

    if (lifecycle.state != Lifecycle.State.DESTROYED) {
        val observer = { value: T -> state.value = value }
        subscribe(observer)
        lifecycle.doOnDestroy {
            unsubscribe(observer)
        }
    }

    return state
}

/**
 * Creates a coroutine scope tied to Decompose lifecycle. A scope is canceled when a component is destroyed.
 */
fun LifecycleOwner.componentCoroutineScope(): CoroutineScope {
    return lifecycle.coroutineScope()
}

/**
 * A helper function to save and restore component state.
 */
inline fun <reified T : Parcelable> StateKeeperOwner.persistent(
    key: String = "PersistentState",
    noinline save: () -> T,
    restore: (T) -> Unit
) {
    stateKeeper.consume<T>(key)?.run(restore)
    stateKeeper.register(key, save)
}