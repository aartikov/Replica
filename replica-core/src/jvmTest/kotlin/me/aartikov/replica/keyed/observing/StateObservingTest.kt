package me.aartikov.replica.keyed.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.TestObserverHost
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StateObservingTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `active observer doesn't observe data when became inactive`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        val observer = replica.observe(observerHost, MutableStateFlow(DEFAULT_KEY))
        observerHost.active = false
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `active observers observe new data`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        val observer = replica.observe(observerHost, MutableStateFlow(DEFAULT_KEY))
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(data = KeyedReplicaProvider.testData(DEFAULT_KEY)), state)
    }

    @Test
    fun `inactive observer doesn't observe new data`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = false)
        val observer = replica.observe(
            observerHost,
            MutableStateFlow(DEFAULT_KEY)
        )
        delay(DEFAULT_DELAY)
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        delay(DEFAULT_DELAY)

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `inactive observer observes new data when became active`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = false)
        val observer = replica.observe(observerHost, MutableStateFlow(DEFAULT_KEY))

        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        observerHost.active = true
        delay(DEFAULT_DELAY)

        assertEquals(
            Loadable<Any>(data = KeyedReplicaProvider.testData(DEFAULT_KEY)),
            observer.currentState
        )
    }

    @Test
    fun `active observer observes data when change observer key`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        val observerKey = MutableStateFlow<Int?>(null)
        val observer = replica.observe(observerHost, observerKey)
        observerKey.update { DEFAULT_KEY }
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        runCurrent()

        assertEquals(
            Loadable<Any>(data = KeyedReplicaProvider.testData(DEFAULT_KEY)),
            observer.currentState
        )
    }

    @Test
    fun `active observer doesn't observe data when change observer key to another key`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        val observerKey = MutableStateFlow(DEFAULT_KEY)
        val observer = replica.observe(observerHost, observerKey)
        observerKey.update { DEFAULT_KEY + 1 }
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        runCurrent()

        assertEquals(Loadable<Any>(), observer.currentState)
    }
}