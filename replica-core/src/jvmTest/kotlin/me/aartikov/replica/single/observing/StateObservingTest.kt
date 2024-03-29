package me.aartikov.replica.single.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
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
        val observer = replica.observe(TestScope(), MutableStateFlow(true))
        runCurrent()

        val state = observer.currentState
        assertEquals(Loadable<Any>(data = newData), state)
    }

    @Test
    fun `inactive observer doesn't observe data initially`() = runTest {
        val replica = replicaProvider.replica()
        val newData = ("new data")

        replica.setData(newData)
        val observer = replica.observe(TestScope(), MutableStateFlow(false))

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `active observer doesn't observe data when became inactive`() = runTest {
        val replica = replicaProvider.replica()

        val activeObserver = MutableStateFlow(true)
        val observer = replica.observe(TestScope(), activeObserver)
        activeObserver.update { false }
        replica.setData("test")

        val state = observer.currentState
        assertEquals(Loadable<Any>(), state)
    }

    @Test
    fun `active observers observe new data`() = runTest {
        val replica = replicaProvider.replica()
        val newData = "new data"

        val observer = replica.observe(TestScope(), MutableStateFlow(true))
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

        val observer = replica.observe(TestScope(), MutableStateFlow(false))
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

        val observerActive = MutableStateFlow(false)
        val observer = replica.observe(TestScope(), observerActive)

        replica.setData(newData)
        observerActive.value = true
        delay(DEFAULT_DELAY)

        assertEquals(Loadable<Any>(data = newData), observer.currentState)
    }
}