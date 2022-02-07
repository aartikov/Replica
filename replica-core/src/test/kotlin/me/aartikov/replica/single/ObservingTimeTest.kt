package me.aartikov.replica.single

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.MainCoroutineRule
import me.aartikov.replica.common.ObservingTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObservingTimeTest {

    private val replicaProvider = ReplicaProvider()
    private val fakeTime = FakeTimeProvider().currentTime

    companion object {
        private const val DEFAULT_DELAY = 100L
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

        replica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is never when inactive observer is added`() = runTest {
        val replica = replicaProvider.replica()

        replica.observe(TestScope(), MutableStateFlow(false))
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when observer became inactive`() = runTest {
        val replica = replicaProvider.replica()

        val observerActive = MutableStateFlow(true)
        replica.observe(TestScope(), observerActive)
        runCurrent()
        observerActive.value = false
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(fakeTime), state.observingTime)
    }

    @Test
    fun `is now when observer became active`() = runTest {
        val replica = replicaProvider.replica()

        launch {
            val observerActive = MutableStateFlow(false)
            replica.observe(TestScope(), observerActive)
            delay(DEFAULT_DELAY)
            observerActive.update { true }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is never when observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        launch {
            val observer = replica.observe(TestScope(), MutableStateFlow(false))
            delay(DEFAULT_DELAY)
            observer.cancelObserving()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when active observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        launch {
            val observer = replica.observe(TestScope(), MutableStateFlow(true))
            delay(DEFAULT_DELAY)
            observer.cancelObserving()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(fakeTime), state.observingTime)
    }

    @Test
    fun `is never when observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        launch {
            val observerScope = TestScope()
            replica.observe(observerScope, MutableStateFlow(false))
            delay(DEFAULT_DELAY)
            observerScope.cancel()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when active observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        launch {
            val observerScope = TestScope()
            replica.observe(observerScope, MutableStateFlow(true))
            delay(DEFAULT_DELAY)
            observerScope.cancel()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(fakeTime), state.observingTime)
    }

    @Test
    fun `is now when second active observer became inactive`() = runTest {
        val replica = replicaProvider.replica()

        launch {
            val observerActive = MutableStateFlow(true)
            replica.observe(TestScope(), MutableStateFlow(true))
            replica.observe(TestScope(), observerActive)
            delay(DEFAULT_DELAY)
            observerActive.update { false }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is now when second active observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        launch {
            replica.observe(TestScope(), MutableStateFlow(true))
            val observer2 = replica.observe(TestScope(), MutableStateFlow(true))
            delay(DEFAULT_DELAY)
            observer2.cancelObserving()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is now when second active observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        launch {
            val observerScope = TestScope()
            replica.observe(TestScope(), MutableStateFlow(true))
            replica.observe(observerScope, MutableStateFlow(true))
            delay(DEFAULT_DELAY)
            observerScope.cancel()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }
}