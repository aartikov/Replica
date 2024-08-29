package me.aartikov.replica.single.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.ObserverScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CancelTimeTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `loading cancels if cancel time is passed`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 2)
                "test"
            }
        )

        val observerScope = ObserverScope()
        replica.observe(observerScope, MutableStateFlow(true))
        replica.refresh()
        runCurrent()
        observerScope.cancel()
        delay(DEFAULT_DELAY + 1) // waiting until cancel time passed
        runCurrent()

        assertFalse(replica.currentState.loading)
        assertNull(replica.currentState.data)
    }

    @Test
    fun `loading doesn't cancel when cancel time is not passed yet`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 2)
                "test"
            }
        )

        val observerScope = ObserverScope()
        replica.observe(observerScope, MutableStateFlow(true))
        replica.refresh()
        runCurrent()
        observerScope.cancel()
        delay(DEFAULT_DELAY - 1)
        runCurrent()

        assertTrue(replica.currentState.loading)
        assertNull(replica.currentState.data)
    }

    @Test
    fun `loading doesn't cancel when cancel time is passed and was preloading`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 2)
                "test"
            }
        )

        replica.refresh()
        delay(DEFAULT_DELAY + 1) // waiting until cancel time passed
        runCurrent()

        assertTrue(replica.currentState.loading)
        assertTrue(replica.currentState.preloading)
        assertNull(replica.currentState.data)
    }

    @Test
    fun `loading doesn't cancel when cancel time is passed and active observer became inactive`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        val observerScope = ObserverScope()
        val observerActive = MutableStateFlow(true)
        replica.observe(observerScope, observerActive)
        replica.refresh()
        observerActive.update { false }
        delay(DEFAULT_DELAY + 1) // waiting until cancel time passed
        runCurrent()

        assertTrue(replica.currentState.loading)
    }

    @Test
    fun `loading cancels when cancel time is passed and inactive observer cancels`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 3)
                "test"
            }
        )

        val observer = replica.observe(ObserverScope(), MutableStateFlow(false))
        replica.refresh()
        runCurrent()
        observer.cancelObserving()
        delay(DEFAULT_DELAY + 1) // waiting until cancel time passed
        runCurrent()

        assertFalse(replica.currentState.loading)
    }

    @Test
    fun `loading doesn't cancels when cancel time isn't passed and second observer starts observing`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds,
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 2)
                "test"
            }
        )

        val observerScope = ObserverScope()
        replica.observe(observerScope, MutableStateFlow(true))
        replica.refresh()
        runCurrent()
        observerScope.cancel()
        delay(DEFAULT_DELAY - 1) // cancel time is not passed yet
        replica.observe(ObserverScope(), MutableStateFlow(true))
        delay(2) // in sum, cancel time is passed
        runCurrent()

        assertTrue(replica.currentState.loading)
    }

    @Test
    fun `loading doesn't cancel if data requested`() = runTest {
        val replica = replicaProvider.replica(
            replicaSettings = ReplicaSettings(
                staleTime = 30.seconds,
                cancelTime = DEFAULT_DELAY.milliseconds
            ),
            fetcher = {
                delay(DEFAULT_DELAY * 2)
                "test"
            }
        )

        val observer = replica.observe(ObserverScope(), MutableStateFlow(true))
        replica.refresh()
        launch { replica.getData(forceRefresh = true) }
        observer.cancelObserving()
        delay(DEFAULT_DELAY + 1) // waiting until cancel time passed
        runCurrent()

        assertTrue(replica.currentState.loading)
    }
}