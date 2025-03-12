package me.aartikov.replica.algebra

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.algebra.normal.associate
import me.aartikov.replica.algebra.normal.map
import me.aartikov.replica.algebra.utils.LoadingFailedException
import me.aartikov.replica.algebra.utils.MainCoroutineRule
import me.aartikov.replica.algebra.utils.ReplicaProvider
import me.aartikov.replica.algebra.utils.TestObserverHost
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingReason
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssociateReplicaTest {

    private val replicaProvider = ReplicaProvider()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `receives data by definite key`() = runTest {
        val data = "abcde"
        val replica = replicaProvider.replica(fetcher = { data })

        val associatedReplica = associate { key: Int ->
            replica.map { it.getOrNull(key)?.toString() ?: "no data" }
        }
        val key = 0
        val actualData = associatedReplica.getData(key)

        assertEquals("a", actualData)
    }

    @Test
    fun `observes data by definite key`() = runTest {
        val data = "abcde"
        val replica = replicaProvider.replica(fetcher = { data })

        val associatedReplica = associate { key: Int ->
            replica.map { it.getOrNull(key)?.toString() ?: "no data" }
        }
        val key = 0
        val observerHost = TestObserverHost(active = true)
        val observer = associatedReplica.observe(observerHost, MutableStateFlow(key))
        associatedReplica.refresh(key)
        runCurrent()

        assertEquals(Loadable(data = "a"), observer.currentState)
    }

    @Test
    fun `observes error state when error throws`() = runTest {
        val data = "abcde"
        val replica = replicaProvider.replica(fetcher = { data })
        val exception = LoadingFailedException()

        val associatedReplica = associate { key: Int ->
            replica.map { it.getOrElse(key) { throw  exception } }
        }
        val key = data.length + 1
        val observerHost = TestObserverHost(active = true)
        val observer = associatedReplica.observe(observerHost, MutableStateFlow(key))
        associatedReplica.refresh(key)
        runCurrent()

        assertEquals(
            Loadable<String>(error = CombinedLoadingError(LoadingReason.Normal, exception)),
            observer.currentState
        )
    }

    @Test
    fun `observes new data when key is changed`() = runTest {
        val data = "abcde"
        val replica = replicaProvider.replica(fetcher = { data })
        val associatedReplica = associate { key: Int ->
            replica.map { it.getOrNull(key)?.toString() ?: "no data" }
        }

        val firstKey = 0
        val secondKey = 1
        val observerKey = MutableStateFlow(firstKey)
        val observerHost = TestObserverHost(active = true)
        val observer = associatedReplica.observe(observerHost, observerKey)
        associatedReplica.refresh(firstKey)
        observerKey.value = secondKey
        runCurrent()

        assertEquals(Loadable(data = "b"), observer.currentState)
    }

    @Test
    fun `observes changed data when original replica changed data`() = runTest {
        val data = "abcde"
        val replica = replicaProvider.replica(fetcher = { data })
        val associatedReplica = associate { key: Int ->
            replica.map { it.getOrNull(key)?.toString() ?: "no data" }
        }

        val key = 0
        val observerHost = TestObserverHost(active = true)
        val observer = associatedReplica.observe(observerHost, MutableStateFlow(key))
        associatedReplica.refresh(key)
        runCurrent()
        replica.setData("ABCDE")
        runCurrent()

        assertEquals(Loadable(data = "A"), observer.currentState)
    }
}