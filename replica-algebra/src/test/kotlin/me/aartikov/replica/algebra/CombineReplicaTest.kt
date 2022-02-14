package me.aartikov.replica.algebra

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.algebra.utils.LoadingFailedException
import me.aartikov.replica.algebra.utils.MainCoroutineRule
import me.aartikov.replica.algebra.utils.ReplicaProvider
import me.aartikov.replica.common.CombinedLoadingError
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
        val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
        combinedReplica.refresh()
        runCurrent()

        assertEquals(Loadable(data = data1 + data2), observer.currentState)
    }

    @Test
    fun `no data if one of replicas no data`() = runTest {
        val data1 = "test1"
        val data2 = "test2"
        val replica1 = replicaProvider.replica(fetcher = { data1 })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
        replica1.refresh()
        runCurrent()

        assertEquals(data1, replica1.currentState.data?.value)
        assertNull(replica2.currentState.data?.value)
        assertEquals(Loadable<String>(), observer.currentState)
    }

    @Test
    fun `part of data if one of replicas no data and replica combined eagerly`() = runTest {
        val data1 = "test1"
        val data2 = "test2"
        val replica1 = replicaProvider.replica(fetcher = { data1 })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combineEager(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
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
        val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
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
            val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
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
        val error = LoadingFailedException()
        val replica1 = replicaProvider.replica(fetcher = { throw error })
        val replica2 = replicaProvider.replica(fetcher = { data2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
        combinedReplica.refresh()
        runCurrent()

        assertEquals(Loadable<String>(error = CombinedLoadingError(error)), observer.currentState)
    }

    @Test
    fun `error, part of data if one of replicas throws error and replicas combined eagerly`() =
        runTest {
            val data2 = "test2"
            val error = LoadingFailedException()
            val replica1 = replicaProvider.replica(fetcher = { throw error })
            val replica2 = replicaProvider.replica(fetcher = { data2 })

            val combinedReplica = combineEager(replica1, replica2) { d1, d2 -> d1 + d2 }
            val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
            combinedReplica.refresh()
            runCurrent()

            assertEquals(
                Loadable(data = "null$data2", error = CombinedLoadingError(error)),
                observer.currentState
            )
        }

    @Test
    fun `multiple errors, if multiple replicas throws error`() = runTest {
        val error1 = LoadingFailedException("error1")
        val error2 = LoadingFailedException("error2")
        val replica1 = replicaProvider.replica(fetcher = { throw error1 })
        val replica2 = replicaProvider.replica(fetcher = { throw error2 })

        val combinedReplica = combine(replica1, replica2) { d1, d2 -> d1 + d2 }
        val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
        combinedReplica.refresh()
        runCurrent()

        assertEquals(
            Loadable<String>(error = CombinedLoadingError(listOf(error1, error2))),
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
        val observer = combinedReplica.observe(TestScope(), MutableStateFlow(true))
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