package me.aartikov.replica.sample.core.ui.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.ValueObserver
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun <T : Any> createFakeRouterState(instance: T): RouterState<*, T> {
    return RouterState(
        activeChild = Child.Created(
            configuration = "<fake>",
            instance = instance
        )
    )
}

fun LifecycleOwner.componentCoroutineScope(): CoroutineScope {
    return lifecycle.coroutineScope()
}

fun Lifecycle.coroutineScope(): CoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    if (this.state != Lifecycle.State.DESTROYED) {
        this.doOnDestroy {
            scope.cancel()
        }
    } else {
        scope.cancel()
    }

    return scope
}

fun Lifecycle.activeFlow(): StateFlow<Boolean> {
    val flow = MutableStateFlow(
        this.state == Lifecycle.State.STARTED || this.state == Lifecycle.State.RESUMED
    )

    this.subscribe(object : Lifecycle.Callbacks {

        override fun onCreate() {
            flow.value = false
        }

        override fun onStart() {
            flow.value = true
        }

        override fun onResume() {
            flow.value = true
        }

        override fun onPause() {
            flow.value = false
        }

        override fun onStop() {
            flow.value = false
        }

        override fun onDestroy() {
            flow.value = false
        }
    })

    return flow
}

fun <T : Any> Value<T>.toComposeState(lifecycle: Lifecycle): State<T> {
    val state: MutableState<T> = mutableStateOf(this.value)

    if (lifecycle.state != Lifecycle.State.DESTROYED) {
        val observer: ValueObserver<T> = { state.value = it }
        subscribe(observer)
        lifecycle.doOnDestroy {
            unsubscribe(observer)
        }
    }

    return state
}