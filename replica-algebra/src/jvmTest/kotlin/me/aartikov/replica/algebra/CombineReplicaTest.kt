package me.aartikov.replica.algebra

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.algebra.normal.combine
import me.aartikov.replica.algebra.normal.combineEager
import me.aartikov.replica.algebra.utils.LoadingFailedException
import me.aartikov.replica.algebra.utils.MainCoroutineRule
import me.aartikov.replica.algebra.utils.ReplicaProvider
import me.aartikov.replica.algebra.utils.TestObserverHost
import me.aartikov.replica.common.CombinedLoadingError
import me.aartikov.replica.common.LoadingError
import me.aartikov.replica.common.LoadingReason
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.currentState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CombineReplicaTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `active observer observes combined data`() = runTest {
        val data1 = "test1"
        val data2 = "test2"
        val replica1 = replicaProvider.replica(fetcher = { data1 })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observerHost = TestObserverHost(active = true)
        val observer = combinedReplica.observe(observerHost)
        combinedReplica.refresh()
        runCurrent()

        assertEquals(Loadable(data = data1 + data2), observer.currentState)
    }

    @Test
    fun `no data if one of replicas has no data`() = runTest {
        val data1 = "test1"
        val data2 = "test2"
        val replica1 = replicaProvider.replica(fetcher = { data1 })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observerHost = TestObserverHost(active = true)
        val observer = combinedReplica.observe(observerHost)
        replica1.refresh()
        runCurrent()

        assertEquals(data1, replica1.currentState.data?.value)
        assertNull(replica2.currentState.data?.value)
        assertEquals(Loadable<String>(), observer.currentState)
    }

    @Test
    fun `part of data if one of replicas has no data and replica combined eagerly`() = runTest {
        val data1 = "test1"
        val data2 = "test2"
        val replica1 = replicaProvider.replica(fetcher = { data1 })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combineEager(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observerHost = TestObserverHost(active = true)
        val observer = combinedReplica.observe(observerHost)
        replica1.refresh()
        runCurrent()

        assertEquals(Loadable(data = data1 + null), observer.currentState)
    }

    @Test
    fun `loading, no data if one of replicas is loading`() = runTest {
        val data1 = "test1"
        val data2 = "test2"
        val replica1 = replicaProvider.replica(fetcher = {
            delay(DEFAULT_DELAY)
            data1
        })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observerHost = TestObserverHost(active = true)
        val observer = combinedReplica.observe(observerHost)
        combinedReplica.refresh()
        delay(DEFAULT_DELAY - 1) // loading not completes yet

        assertEquals(Loadable<String>(loading = true), observer.currentState)
    }

    @Test
    fun `loading, part of data if one of replicas is loading and replicas combined eagerly`() =
        runTest {
            val data1 = "test1"
            val data2 = "test2"
            val replica1 = replicaProvider.replica(fetcher = {
                delay(DEFAULT_DELAY)
                data1
            })
            val replica2 = replicaProvider.replica(fetcher = { data2 })

            val combinedReplica = combineEager(replica1, replica2) { d1, d2 -> d1 + d2 }
            val observerHost = TestObserverHost(active = true)
            val observer = combinedReplica.observe(observerHost)
            combinedReplica.refresh()
            delay(DEFAULT_DELAY - 1) // loading not completes yet

            assertEquals(
                Loadable(
                    data = "null$data2",
                    loading = true
                ), observer.currentState
            )
        }

    @Test
    fun `error, no data if one of replicas throws error`() = runTest {
        val data2 = "test2"
        val exception = LoadingFailedException()
        val replica1 = replicaProvider.replica(fetcher = { throw exception })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observerHost = TestObserverHost(active = true)
        val observer = combinedReplica.observe(observerHost)
        combinedReplica.refresh()
        runCurrent()

        assertEquals(
            Loadable<String>(error = CombinedLoadingError(LoadingReason.Normal, exception)),
            observer.currentState
        )
    }

    @Test
    fun `error, part of data if one of replicas throws error and replicas combined eagerly`() =
        runTest {
            val data2 = "test2"
            val exception = LoadingFailedException()
            val replica1 = replicaProvider.replica(fetcher = { throw exception })
            val replica2 = replicaProvider.replica(fetcher = { data2 })

            val combinedReplica = combineEager(replica1, replica2) { d1, d2 -> d1 + d2 }
            val observerHost = TestObserverHost(active = true)
            val observer = combinedReplica.observe(observerHost)
            combinedReplica.refresh()
            runCurrent()

            assertEquals(
                Loadable(data = "null$data2", error = CombinedLoadingError(LoadingReason.Normal, exception)),
                observer.currentState
            )
        }

    @Test
    fun `multiple errors, if multiple replicas throws error`() = runTest {
        val exception1 = LoadingFailedException("error1")
        val exception2 = LoadingFailedException("error2")
        val replica1 = replicaProvider.replica(fetcher = { throw exception1 })
        val replica2 = replicaProvider.replica(fetcher = { throw exception2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observerHost = TestObserverHost(active = true)
        val observer = combinedReplica.observe(observerHost)
        combinedReplica.refresh()
        runCurrent()

        val expectedErrors = listOf(
            LoadingError(LoadingReason.Normal, exception1),
            LoadingError(LoadingReason.Normal, exception2)
        )
        assertEquals(
            Loadable<String>(error = CombinedLoadingError(expectedErrors)),
            observer.currentState
        )
    }

    @Test
    fun `observes new data when one of replica data is changed`() = runTest {
        val data1 = "test1"
        val data2 = "test2"
        val data2Changed = "test2Changed"
        val replica1 = replicaProvider.replica(fetcher = { data1 })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observerHost = TestObserverHost(active = true)
        val observer = combinedReplica.observe(observerHost)
        combinedReplica.refresh()
        runCurrent()
        replica2.setData(data2Changed)
        runCurrent()

        assertEquals(
            Loadable(data = data1 + data2Changed),
            observer.currentState
        )
    }
}