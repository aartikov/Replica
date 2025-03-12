package me.aartikov.replica.single.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.aartikov.replica.common.ObservingTime
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.TestObserverHost
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

        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is never when inactive observer is added`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = false)
        replica.observe(observerHost)
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when observer became inactive`() = runTest {
        val replica = replicaProvider.replica()

        fakeTimeProvider.currentTime = TEST_TIME
        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        runCurrent()
        observerHost.active = false
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(TEST_TIME), state.observingTime)
    }

    @Test
    fun `is now when observer became active`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = false)
        replica.observe(observerHost)
        observerHost.active = true
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is never when observer canceled and was not active yet`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = false)
        val observer = replica.observe(observerHost)
        observer.cancelObserving()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when active observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        fakeTimeProvider.currentTime = TEST_TIME
        val observerHost = TestObserverHost(active = true)
        val observer = replica.observe(observerHost)
        runCurrent()
        observer.cancelObserving()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(TEST_TIME), state.observingTime)
    }

    @Test
    fun `is never when observer scope canceled and observer was not active yet`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = false)
        replica.observe(observerHost)
        runCurrent()
        observerHost.cancelCoroutineScope()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Never, state.observingTime)
    }

    @Test
    fun `is in past when active observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        fakeTimeProvider.currentTime = TEST_TIME
        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        runCurrent()
        observerHost.cancelCoroutineScope()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.TimeInPast(TEST_TIME), state.observingTime)
    }

    @Test
    fun `is now when second active observer became inactive`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost1 = TestObserverHost(active = true)
        val observerHost2 = TestObserverHost(active = true)
        replica.observe(observerHost1)
        replica.observe(observerHost2)
        observerHost2.active = false
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is now when second active observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        val observer2 = replica.observe(observerHost)
        observer2.cancelObserving()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }

    @Test
    fun `is now when second active observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost1 = TestObserverHost(active = true)
        val observerHost2 = TestObserverHost(active = true)
        replica.observe(observerHost1)
        replica.observe(observerHost2)
        runCurrent()
        observerHost2.cancelCoroutineScope()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(ObservingTime.Now, state.observingTime)
    }
}