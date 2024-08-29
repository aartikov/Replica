package me.aartikov.replica.single.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.aartikov.replica.common.ObservingTime
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.ObserverScope
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObservingTimeTest {

    private val replicaProvider = ReplicaProvider()
    private val fakeTimeProvider = replicaProvider.timeProvider

    companion object {
        private val TEST_TIME = Instant.fromEpochMilliseconds(1000)
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `is initially never`() = runTest {
        val replica = replicaProvider.replica()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is now when active observer observe`() = runTest {
        val replica = replicaProvider.replica()

        replica.observe(ObserverScope(), MutableStateFlow(true))
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is never when inactive observer is added`() = runTest {
        val replica = replicaProvider.replica()

        replica.observe(ObserverScope(), MutableStateFlow(false))
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when observer became inactive`() = runTest {
        val replica = replicaProvider.replica()

        fakeTimeProvider.currentTime = TEST_TIME
        val observerActive = MutableStateFlow(true)
        replica.observe(ObserverScope(), observerActive)
        runCurrent()
        observerActive.value = false
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(TEST_TIME), state.observingTime)
    }

    @Test
    fun `is now when observer became active`() = runTest {
        val replica = replicaProvider.replica()

        val observerActive = MutableStateFlow(false)
        replica.observe(ObserverScope(), observerActive)
        observerActive.update { true }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is never when observer canceled and was not active yet`() = runTest {
        val replica = replicaProvider.replica()

        val observer = replica.observe(ObserverScope(), MutableStateFlow(false))
        observer.cancelObserving()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when active observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        fakeTimeProvider.currentTime = TEST_TIME
        val observer = replica.observe(ObserverScope(), MutableStateFlow(true))
        runCurrent()
        observer.cancelObserving()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(TEST_TIME), state.observingTime)
    }

    @Test
    fun `is never when observer scope canceled and observer was not active yet`() = runTest {
        val replica = replicaProvider.replica()

        val observerScope = ObserverScope()
        replica.observe(observerScope, MutableStateFlow(false))
        runCurrent()
        observerScope.cancel()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when active observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        fakeTimeProvider.currentTime = TEST_TIME
        val observerScope = ObserverScope()
        replica.observe(observerScope, MutableStateFlow(true))
        runCurrent()
        observerScope.cancel()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(TEST_TIME), state.observingTime)
    }

    @Test
    fun `is now when second active observer became inactive`() = runTest {
        val replica = replicaProvider.replica()

        val observerActive = MutableStateFlow(true)
        replica.observe(ObserverScope(), MutableStateFlow(true))
        replica.observe(ObserverScope(), observerActive)
        observerActive.update { false }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is now when second active observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        replica.observe(ObserverScope(), MutableStateFlow(true))
        val observer2 = replica.observe(ObserverScope(), MutableStateFlow(true))
        observer2.cancelObserving()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is now when second active observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observerScope = ObserverScope()
        replica.observe(ObserverScope(), MutableStateFlow(true))
        replica.observe(observerScope, MutableStateFlow(true))
        runCurrent()
        observerScope.cancel()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }
}