package me.aartikov.replica.single.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.MainCoroutineRule
import me.aartikov.replica.single.utils.ReplicaProvider
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

        replica.observe(TestScope(), MutableStateFlow(false))
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has active observer when active observe has called`() = runTest {
        val replica = replicaProvider.replica()

        replica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(1, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has observer when observer became inactive`() = runTest {
        val replica = replicaProvider.replica()

        val observerActive = MutableStateFlow(true)
        replica.observe(TestScope(), observerActive)
        observerActive.update { false }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has active observer when observer became active`() = runTest {
        val replica = replicaProvider.replica()

        val observerActive = MutableStateFlow(false)
        replica.observe(TestScope(), observerActive)
        observerActive.update { true }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(1, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has no observers when observer canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        observer.cancelObserving()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has no observers when observer scope canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observerScope = TestScope()
        replica.observe(observerScope, MutableStateFlow(true))
        observerScope.cancel()
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has multiple observers when multiple observers observe`() = runTest {
        val replica = replicaProvider.replica()
        val observersCount = 5
        val activeObserversCount = 3

        repeat(observersCount) { replica.observe(TestScope(), MutableStateFlow(false)) }
        repeat(activeObserversCount) { replica.observe(TestScope(), MutableStateFlow(true)) }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(activeObserversCount, state.activeObserverCount)
        assertEquals(observersCount + activeObserversCount, state.observerCount)
    }

    @Test
    fun `has no observers when multiple observers canceled observing`() = runTest {
        val replica = replicaProvider.replica()

        val observers = (0 until 10)
            .map { replica.observe(TestScope(), MutableStateFlow(true)) }
        observers.forEach { it.cancelObserving() }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has no observers when scopes of multiple observers canceled`() = runTest {
        val replica = replicaProvider.replica()

        val observerScopes = (0 until 10)
            .map {
                val observerScope = TestScope()
                replica.observe(observerScope, MutableStateFlow(true))
                observerScope
            }
        observerScopes.forEach { it.cancel() }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has active observer when two observers change active state`() = runTest {
        val replica = replicaProvider.replica()

        val observerActive1 = MutableStateFlow(false)
        val observerActive2 = MutableStateFlow(true)
        replica.observe(TestScope(), observerActive1)
        replica.observe(TestScope(), observerActive2)
        observerActive1.update { true }
        observerActive2.update { false }
        runCurrent()

        val state = replica.currentState.observingState
        assertEquals(2, state.observerCount)
        assertEquals(1, state.activeObserverCount)
    }
}