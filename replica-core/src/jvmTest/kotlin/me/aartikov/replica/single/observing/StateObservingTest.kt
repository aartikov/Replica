package me.aartikov.replica.single.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.TestObserverHost
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StateObservingTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `active observer observes data initially`() = runTest {
        val replica = replicaProvider.replica()
        val newData = ("new data")

        replica.setData(newData)
        val observerHost = TestObserverHost(active = true)
        val observer = replica.observe(observerHost)
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(data = newData), state)
    }

    @Test
    fun `inactive observer doesn't observe data initially`() = runTest {
        val replica = replicaProvider.replica()
        val newData = ("new data")

        replica.setData(newData)
        val observerHost = TestObserverHost(active = false)
        val observer = replica.observe(observerHost)

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `active observer doesn't observe data when became inactive`() = runTest {
        val replica = replicaProvider.replica()

        val observerHost = TestObserverHost(active = true)
        val observer = replica.observe(observerHost)
        observerHost.active = false
        replica.setData("test")

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `active observers observe new data`() = runTest {
        val replica = replicaProvider.replica()
        val newData = "new data"

        val observerHost = TestObserverHost(active = true)
        val observer = replica.observe(observerHost)
        delay(DEFAULT_DELAY)
        replica.setData(newData)
        delay(DEFAULT_DELAY)

        val state = observer.currentState
        assertEquals(Loadable<Any>(data = newData), state)
    }

    @Test
    fun `inactive observer doesn't observe new data`() = runTest {
        val replica = replicaProvider.replica()
        val newData = "new data"

        val observerHost = TestObserverHost(active = false)
        val observer = replica.observe(observerHost)
        delay(DEFAULT_DELAY)
        replica.setData(newData)
        delay(DEFAULT_DELAY)

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `inactive observer observes new data when became active`() = runTest {
        val replica = replicaProvider.replica()
        val newData = "new data"

        val observerHost = TestObserverHost(active = false)
        val observer = replica.observe(observerHost)

        replica.setData(newData)
        observerHost.active = true
        delay(DEFAULT_DELAY)

        assertEquals(Loadable<Any>(data = newData), observer.currentState)
    }
}