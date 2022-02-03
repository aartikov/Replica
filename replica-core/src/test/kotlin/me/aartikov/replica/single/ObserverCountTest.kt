package me.aartikov.replica.single

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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
    fun `is initially no active observers`() = runTest {
        val replica = testPhysicalReplica()

        val activeObserverCount = replica.stateFlow.firstOrNull()
            ?.observingState
            ?.activeObserverCount

        assertEquals(0, activeObserverCount)
    }

    @Test
    fun `is observer when observe is called`() = runTest {
        val replica = testPhysicalReplica()

        replica.observe(TestScope(), MutableStateFlow(false))

        val state = replica.stateFlow.take(2).lastOrNull()?.observingState
        assertEquals(0, state?.activeObserverCount)
        assertEquals(1, state?.observerCount)
    }

    @Test
    fun `is active observer when active observe is called`() = runTest {
        val replica = testPhysicalReplica()

        replica.observe(TestScope(), MutableStateFlow(true))

        val state = replica.stateFlow.take(2).lastOrNull()?.observingState
        assertEquals(1, state?.activeObserverCount)
        assertEquals(1, state?.observerCount)
    }

    @Test
    fun `is observer when observer became inactive`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observerActive = MutableStateFlow(true)
            replica.observe(TestScope(), observerActive)
            delay(DEFAULT_DELAY)
            observerActive.update { false }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.stateFlow.firstOrNull()?.observingState
        assertEquals(0, state?.activeObserverCount)
        assertEquals(1, state?.observerCount)
    }

    @Test
    fun `is active observer when observer became active`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observerActive = MutableStateFlow(false)
            replica.observe(TestScope(), observerActive)
            delay(DEFAULT_DELAY)
            observerActive.update { true }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.stateFlow.firstOrNull()?.observingState
        assertEquals(1, state?.activeObserverCount)
        assertEquals(1, state?.observerCount)
    }

    @Test
    fun `is no observers when observer canceled`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observer = replica.observe(TestScope(), MutableStateFlow(true))
            delay(DEFAULT_DELAY)
            observer.cancelObserving()
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.stateFlow.firstOrNull()?.observingState
        assertEquals(0, state?.activeObserverCount)
        assertEquals(0, state?.observerCount)
    }

    @Test
    fun `is multiple observers when multiple observers observe`() = runTest {
        val replica = testPhysicalReplica()
        val observersCount = 5
        val activeObserversCount = 3

        launch {
            repeat(observersCount) { replica.observe(TestScope(), MutableStateFlow(false)) }
            repeat(activeObserversCount) { replica.observe(TestScope(), MutableStateFlow(true)) }
        }
        delay(DEFAULT_DELAY)

        val state = replica.stateFlow.firstOrNull()?.observingState
        assertEquals(activeObserversCount, state?.activeObserverCount)
        assertEquals(observersCount + activeObserversCount, state?.observerCount)
    }

    @Test
    fun `is no observers when multiple observers canceled observing`() = runTest {
        val replica = testPhysicalReplica()

        launch {
            val observers = (0 until 10)
                .map { replica.observe(TestScope(), MutableStateFlow(true)) }
            delay(DEFAULT_DELAY)
            observers.forEach { it.cancelObserving() }
        }
        delay(DEFAULT_DELAY * 2)

        val state = replica.stateFlow.firstOrNull()?.observingState
        assertEquals(0, state?.activeObserverCount)
        assertEquals(0, state?.observerCount)
    }

    @Test
    fun `is active observer when two observers change active state`() = runTest {
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

        val state = replica.stateFlow.firstOrNull()?.observingState
        assertEquals(2, state?.observerCount)
        assertEquals(1, state?.activeObserverCount)
    }

    private fun testPhysicalReplica(): PhysicalReplica<Any> {
        val replicaClient = ReplicaClient()
        return replicaClient.createReplica(
            name = "test",
            settings = ReplicaSettings(staleTime = 1.seconds)
        ) { Any() }
    }
}