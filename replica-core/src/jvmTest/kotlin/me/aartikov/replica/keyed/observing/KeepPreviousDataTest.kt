package me.aartikov.replica.keyed.observing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.keyed.KeyedReplica
import me.aartikov.replica.keyed.keepPreviousData
import me.aartikov.replica.keyed.utils.KeyedReplicaProvider
import me.aartikov.replica.single.Loadable
import me.aartikov.replica.single.ReplicaSettings
import me.aartikov.replica.utils.MainCoroutineRule
import me.aartikov.replica.utils.ObserverScope
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KeepPreviousDataTest {

    private val replicaProvider = KeyedReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
        private const val DEFAULT_KEY = 0
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `keeps previous data during refresh with keepPreviousData`() = runTest {
        val replica: KeyedReplica<Int, String> = replicaProvider.replica(
            fetcher = { k ->
                delay(DEFAULT_DELAY)
                KeyedReplicaProvider.testData(k)
            },
            childReplicaSettings = {
                ReplicaSettings(
                    staleTime = null,
                    revalidateOnActiveObserverAdded = true
                )
            }
        )

        val observerKey = MutableStateFlow(DEFAULT_KEY)
        val observer = replica
            .keepPreviousData()
            .observe(
                observerCoroutineScope = ObserverScope(),
                observerActive = MutableStateFlow(true),
                key = observerKey
            )
        delay(DEFAULT_DELAY + 1) // waiting until loading complete
        observerKey.value = DEFAULT_KEY + 1
        delay(DEFAULT_DELAY - 1) // loading not complete yet

        val expectedLoadable = Loadable(
            loading = true,
            data = KeyedReplicaProvider.testData(DEFAULT_KEY)
        )
        assertEquals(expectedLoadable, observer.stateFlow.value)
    }

    @Test
    fun `no keeps previous data during refresh without keepPreviousData`() = runTest {
        val replica: KeyedReplica<Int, String> = replicaProvider.replica(
            fetcher = { k ->
                delay(DEFAULT_DELAY)
                KeyedReplicaProvider.testData(k)
            },
            childReplicaSettings = {
                ReplicaSettings(
                    staleTime = null,
                    revalidateOnActiveObserverAdded = true
                )
            }
        )

        val observerKey = MutableStateFlow(DEFAULT_KEY)
        val observer = replica
            .observe(
                observerCoroutineScope = ObserverScope(),
                observerActive = MutableStateFlow(true),
                key = observerKey
            )
        delay(DEFAULT_DELAY + 1) // waiting until loading complete
        observerKey.value = DEFAULT_KEY + 1
        delay(DEFAULT_DELAY - 1) // loading not complete yet

        val expectedLoadable = Loadable(
            loading = true,
            data = null
        )
        assertEquals(expectedLoadable, observer.stateFlow.value)
    }
}