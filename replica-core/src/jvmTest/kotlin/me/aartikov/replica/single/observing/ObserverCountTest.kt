package me.aartikov.replica.single.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.TestObserverHost
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserverCountTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `has no observers initially`() = runTest {
        val replica = replicaProvider.replica()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has observer when observe has called`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = false)
        replica.observe(observerHost)
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has active observer when active observe has called`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(1, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has observer when observer became inactive`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        observerHost.active = false
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has active observer when observer became active`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = false)
        replica.observe(observerHost)
        observerHost.active = true
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(1, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has no observers when observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        val observer = replica.observe(observerHost)
        observer.cancelObserving()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has no observers when observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        replica.observe(observerHost)
        runCurrent()
        observerHost.cancelCoroutineScope()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has multiple observers when multiple observers observe`() = runTest {
        val replica = replicaProvider.replica()
        val activeObserversCount = 3
        val inactiveObserversCount = 5

        repeat(activeObserversCount) { replica.observe(TestObserverHost(active = true)) }
        repeat(inactiveObserversCount) { replica.observe(TestObserverHost(active = false)) }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(activeObserversCount, state.activeObserverCount)
        assertEquals(activeObserversCount + inactiveObserversCount, state.observerCount)
    }

    @Test
    fun `has no observers when multiple observers canceled observing`() = runTest {
        val replica = replicaProvider.replica()

        val observers = (0 until 10)
            .map { replica.observe(TestObserverHost(active = true)) }
        observers.forEach { it.cancelObserving() }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has no observers when scopes of multiple observers canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observerHosts = (0 until 10)
            .map {
                val observerHost = TestObserverHost(active = true)
                replica.observe(observerHost)
                observerHost
            }
        observerHosts.forEach { it.cancelCoroutineScope() }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has active observer when two observers change active state`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost1 = TestObserverHost(active = false)
        val observerHost2 = TestObserverHost(active = true)
        replica.observe(observerHost1)
        replica.observe(observerHost2)
        observerHost1.active = true
        observerHost2.active = false
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(2, state.observerCount)
        assertEquals(1, state.activeObserverCount)
    }
}