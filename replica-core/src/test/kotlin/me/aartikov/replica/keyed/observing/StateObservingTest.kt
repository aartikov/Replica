package me.aartikov.replica.keyed.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.MainCoroutineRule
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

        val activeObserver = MutableStateFlow(true)
        val observer = replica.observe(
            TestScope(),
            activeObserver,
            MutableStateFlow(DEFAULT_KEY)
        )
        activeObserver.update { false }
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `active observers observe new data`() = runTest {
        val replica = replicaProvider.replica()

        val observer = replica.observe(
            TestScope(),
            MutableStateFlow(true),
            MutableStateFlow(DEFAULT_KEY)
        )
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(data = KeyedReplicaProvider.testData(DEFAULT_KEY)), state)
    }

    @Test
    fun `inactive observer doesn't observe new data`() = runTest {
        val replica = replicaProvider.replica()

        val observer = replica.observe(
            TestScope(),
            MutableStateFlow(false),
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

        val observerActive = MutableStateFlow(false)
        val observer = replica.observe(
            TestScope(),
            observerActive,
            MutableStateFlow(DEFAULT_KEY)
        )

        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        observerActive.value = true
        delay(DEFAULT_DELAY)

        assertEquals(
            Loadable<Any>(data = KeyedReplicaProvider.testData(DEFAULT_KEY)),
            observer.currentState
        )
    }

    @Test
    fun `active observer observes data when change observer key`() = runTest {
        val replica = replicaProvider.replica()

        val observerKey = MutableStateFlow<Int?>(null)
        val observer = replica.observe(
            TestScope(),
            MutableStateFlow(true),
            observerKey
        )
        observerKey.update { DEFAULT_KEY }
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        runCurrent()

        assertEquals(
            Loadable<Any>(data = KeyedReplicaProvider.testData(DEFAULT_KEY)),
            observer.currentState
        )
    }

    @Test
    fun `active observer isn't observes data when change observer key to another key`() = runTest {
        val replica = replicaProvider.replica()

        val observerKey = MutableStateFlow(DEFAULT_KEY)
        val observer = replica.observe(
            TestScope(),
            MutableStateFlow(true),
            observerKey
        )
        observerKey.update { DEFAULT_KEY + 1 }
        replica.setData(DEFAULT_KEY, KeyedReplicaProvider.testData(DEFAULT_KEY))
        runCurrent()

        assertEquals(Loadable<Any>(), observer.currentState)
    }
}