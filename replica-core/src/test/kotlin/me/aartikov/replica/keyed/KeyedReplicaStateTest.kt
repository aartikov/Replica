package me.aartikov.replica.keyed

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KeyedReplicaStateTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        const val DEFAULT_DELAY = 100L
        const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `no replicas initially`() = runTest {
        val replica = replicaProvider.replica()

        val state = replica.currentState
        assertEquals(0, state.replicaCount)
        assertEquals(0, state.replicaWithObserversCount)
        assertEquals(0, state.replicaWithActiveObserversCount)
    }

    @Test
    fun `has replicas when set data is called`() = runTest {
        val replica = replicaProvider.replica()
        val replicasCount = 10

        repeat(replicasCount) { i ->
            replica.setData(i, KeyedReplicaProvider.testData(i))
        }
        runCurrent()
        assertEquals(replicasCount, replica.currentState.replicaCount)
    }

    @Test
    fun `has replica with observer when inactive observers are added in child replica`() = runTest {
        val replica = replicaProvider.replica()

        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        replica.observe(TestScope(), MutableStateFlow(false), MutableStateFlow(DEFAULT_KEY))
        runCurrent()

        assertEquals(1, replica.currentState.replicaCount)
        assertEquals(1, replica.currentState.replicaWithObserversCount)
    }

    @Test
    fun `has replica with observers when inactive observer are added in child replicas`() =
        runTest {
            val replica = replicaProvider.replica()
            val replicasWithObserverCount = 10

            repeat(replicasWithObserverCount) {
                replica.setData(it, "test_data_$it")
                replica.observe(TestScope(), MutableStateFlow(false), MutableStateFlow(it))
            }
            runCurrent()

            assertEquals(replicasWithObserverCount, replica.currentState.replicaCount)
            assertEquals(replicasWithObserverCount, replica.currentState.replicaWithObserversCount)
        }

    @Test
    fun `has replica with active observer when active observer are added in child replica`() =
        runTest {
            val replica = replicaProvider.replica()

            replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
            replica.observe(TestScope(), MutableStateFlow(true), MutableStateFlow(DEFAULT_KEY))
            runCurrent()

            assertEquals(1, replica.currentState.replicaCount)
            assertEquals(1, replica.currentState.replicaWithActiveObserversCount)
        }

    @Test
    fun `has replica with active observer when inactive observer became active in child replica`() =
        runTest {
            val replica = replicaProvider.replica()

            replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
            val observerActive = MutableStateFlow(false)
            replica.observe(TestScope(), observerActive, MutableStateFlow(DEFAULT_KEY))
            observerActive.update { true }
            runCurrent()

            val state = replica.currentState
            assertEquals(1, state.replicaCount)
            assertEquals(1, state.replicaWithObserversCount)
            assertEquals(1, state.replicaWithActiveObserversCount)
        }

    @Test
    fun `has no replica with active observer when active observer became inactive in child replica`() =
        runTest {
            val replica = replicaProvider.replica()

            replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
            val observerActive = MutableStateFlow(true)
            replica.observe(TestScope(), observerActive, MutableStateFlow(DEFAULT_KEY))
            observerActive.update { false }
            runCurrent()

            val state = replica.currentState
            assertEquals(1, state.replicaCount)
            assertEquals(1, state.replicaWithObserversCount)
            assertEquals(0, state.replicaWithActiveObserversCount)
        }

    @Test
    fun `has replica with active observer when active observer changed key`() =
        runTest {
            val replica = replicaProvider.replica()
            val key0 = 0
            val key1 = 1

            replica.setData(key0, KeyedReplicaProvider.testData(key0))
            replica.setData(key1, KeyedReplicaProvider.testData(key1))
            val observingKey = MutableStateFlow(key0)
            replica.observe(TestScope(), MutableStateFlow(true), observingKey)
            observingKey.update { key1 }
            runCurrent()

            val state = replica.currentState
            assertEquals(2, state.replicaCount)
            assertEquals(1, state.replicaWithObserversCount)
            assertEquals(1, state.replicaWithActiveObserversCount)
        }

    @Test
    fun `has no replica with active observer when active observer scope canceled`() =
        runTest {
            val replica = replicaProvider.replica()

            replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
            val observerScope = TestScope()
            replica.observe(observerScope, MutableStateFlow(true), MutableStateFlow(DEFAULT_KEY))
            observerScope.cancel()
            runCurrent()

            val state = replica.currentState
            assertEquals(1, state.replicaCount)
            assertEquals(0, state.replicaWithObserversCount)
            assertEquals(0, state.replicaWithActiveObserversCount)
        }

    @Test
    fun `has no replica with active observer when active observer canceled`() =
        runTest {
            val replica = replicaProvider.replica()

            replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
            val observer = replica.observe(
                TestScope(),
                MutableStateFlow(true),
                MutableStateFlow(DEFAULT_KEY)
            )
            runCurrent()
            observer.cancelObserving()
            runCurrent()

            val state = replica.currentState
            assertEquals(1, state.replicaCount)
            assertEquals(0, state.replicaWithObserversCount)
            assertEquals(0, state.replicaWithActiveObserversCount)
        }
}