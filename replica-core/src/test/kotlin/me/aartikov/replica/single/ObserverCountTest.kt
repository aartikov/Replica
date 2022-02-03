package me.aartikov.replica.single

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.MainCoroutineRule
import me.aartikov.replica.client.ReplicaClient
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ObserverCountTest {

    companion object {
        const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `has no observers initially`() = runTest {
        val replica = testPhysicalReplica()

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has observer when observe has called`() = runTest {
        val replica = testPhysicalReplica()

        replica.observe(TestScope(), MutableStateFlow(false))
        delay(DEFAULT_DELAY)

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has active observer when active observe has called`() = runTest {
        val replica = testPhysicalReplica()

        replica.observe(TestScope(), MutableStateFlow(true))
        delay(DEFAULT_DELAY)

        val state = replica.currentState.observingState
        assertEquals(1, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has observer when observer became inactive`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observerActive = MutableStateFlow(true)
            replica.observe(TestScope(), observerActive)
            delay(DEFAULT_DELAY)
            observerActive.update { false }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has active observer when observer became active`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observerActive = MutableStateFlow(false)
            replica.observe(TestScope(), observerActive)
            delay(DEFAULT_DELAY)
            observerActive.update { true }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(1, state.activeObserverCount)
        assertEquals(1, state.observerCount)
    }

    @Test
    fun `has no observers when observer canceled`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observer = replica.observe(TestScope(), MutableStateFlow(true))
            delay(DEFAULT_DELAY)
            observer.cancelObserving()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has no observers when observer scope canceled`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observerScope = TestScope()
            replica.observe(observerScope, MutableStateFlow(true))
            delay(DEFAULT_DELAY)
            observerScope.cancel()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has multiple observers when multiple observers observe`() = runTest {
        val replica = testPhysicalReplica()
        val observersCount = 5
        val activeObserversCount = 3

        launch {
            repeat(observersCount) { replica.observe(TestScope(), MutableStateFlow(false)) }
            repeat(activeObserversCount) { replica.observe(TestScope(), MutableStateFlow(true)) }
        }
        delay(DEFAULT_DELAY)

        val state = replica.currentState.observingState
        assertEquals(activeObserversCount, state.activeObserverCount)
        assertEquals(observersCount + activeObserversCount, state.observerCount)
    }

    @Test
    fun `has no observers when multiple observers canceled observing`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observers = (0 until 10)
                .map { replica.observe(TestScope(), MutableStateFlow(true)) }
            delay(DEFAULT_DELAY)
            observers.forEach { it.cancelObserving() }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has no observers when scopes of multiple observers canceled`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observerScopes = (0 until 10)
                .map {
                    val observerScope = TestScope()
                    replica.observe(observerScope, MutableStateFlow(true))
                    observerScope
                }
            delay(DEFAULT_DELAY)
            observerScopes.forEach { it.cancel() }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(0, state.activeObserverCount)
        assertEquals(0, state.observerCount)
    }

    @Test
    fun `has active observer when two observers change active state`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observerActive1 = MutableStateFlow(false)
            val observerActive2 = MutableStateFlow(true)
            replica.observe(TestScope(), observerActive1)
            replica.observe(TestScope(), observerActive2)
            delay(DEFAULT_DELAY)
            observerActive1.update { true }
            observerActive2.update { false }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.currentState.observingState
        assertEquals(2, state.observerCount)
        assertEquals(1, state.activeObserverCount)
    }

    private fun testPhysicalReplica(): PhysicalReplica<Any> {
        val replicaClient = ReplicaClient()
        return replicaClient.createReplica(
            name = "test",
            settings = ReplicaSettings(staleTime = 1.seconds)
        ) { Any() }
    }
}