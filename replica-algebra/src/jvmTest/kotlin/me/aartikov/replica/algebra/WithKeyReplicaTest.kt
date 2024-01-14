package me.aartikov.replica.algebra

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.algebra.normal.withKey
import me.aartikov.replica.algebra.utils.KeyedReplicaProvider
import me.aartikov.replica.algebra.utils.MainCoroutineRule
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.single.currentState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WithKeyReplicaTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `receives data are same with keyedReplica after withKeyReplica refresh`() = runTest {
        val keyedReplica = replicaProvider.replica()

        val withKeyReplica = keyedReplica.withKey(DEFAULT_KEY)
        val actualData = withKeyReplica.getData()

        assertEquals(keyedReplica.getData(DEFAULT_KEY), actualData)
    }

    @Test
    fun `observes data after withKeyReplica refreshed`() = runTest {
        val keyedReplica = replicaProvider.replica()

        val withKeyReplica = keyedReplica.withKey(DEFAULT_KEY)
        val observer = withKeyReplica.observe(TestScope(), MutableStateFlow(true))
        withKeyReplica.refresh()
        runCurrent()

        assertEquals(
            Loadable(data = KeyedReplicaProvider.testData(DEFAULT_KEY)),
            observer.currentState
        )
    }

    @Test
    fun `observes data after original replica refreshed`() = runTest {
        val keyedReplica = replicaProvider.replica()

        val withKeyReplica = keyedReplica.withKey(DEFAULT_KEY)
        val observer = withKeyReplica.observe(TestScope(), MutableStateFlow(true))
        keyedReplica.refresh(DEFAULT_KEY)
        runCurrent()

        assertEquals(
            Loadable(data = KeyedReplicaProvider.testData(DEFAULT_KEY)),
            observer.currentState
        )
    }

    @Test
    fun `observes new data after original replica changed data`() = runTest {
        val keyedReplica = replicaProvider.replica()
        val newData = "new data"
        val withKeyReplica = keyedReplica.withKey(DEFAULT_KEY)

        val observer = withKeyReplica.observe(TestScope(), MutableStateFlow(true))
        keyedReplica.refresh(DEFAULT_KEY)
        runCurrent()
        keyedReplica.setData(DEFAULT_KEY, newData)
        runCurrent()

        assertEquals(
            Loadable(data = newData),
            observer.currentState
        )
    }

    @Test
    fun `revalidates data when active withKeyObserver observes when there is revalidateOnActiveObserverAdded flag`() =
        runTest {
            val keyedReplica = replicaProvider.replica(
                childReplicaSettings = {
                    ReplicaSettings(
                        staleTime = null,
                        revalidateOnActiveObserverAdded = true
                    )
                }
            )

            val withKeyReplica = keyedReplica.withKey(DEFAULT_KEY)
            val withKeyObserver = withKeyReplica.observe(TestScope(), MutableStateFlow(true))
            runCurrent()

            assertEquals(
                Loadable(data = KeyedReplicaProvider.testData(DEFAULT_KEY)),
                withKeyObserver.currentState
            )
        }
}