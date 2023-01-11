package me.aartikov.replica.single.storage

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.aartikov.replica.single.currentState
import me.aartikov.replica.single.utils.FakeStorage
import me.aartikov.replica.single.utils.ReplicaProvider
import me.aartikov.replica.utils.MainCoroutineRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StorageTest {

    private val replicaProvider = ReplicaProvider()

    companion object {
        private const val DEFAULT_DELAY = 100L
    }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `writes data in storage after refresh call`() = runTest {
        val storage = FakeStorage()
        val replica = replicaProvider.replica(storage = storage)

        replica.refresh()
        runCurrent()

        val storageData = storage.read()
        assertEquals(ReplicaProvider.TEST_DATA, storageData)
    }

    @Test
    fun `clears data in storage after clear call with removeFromStorage flag`() = runTest {
        val storage = FakeStorage()
        val replica = replicaProvider.replica(storage = storage)

        replica.refresh()
        runCurrent()
        replica.clear(removeFromStorage = true)

        val storageData = storage.read()
        assertNull(storageData)
    }

    @Test
    fun `not clears data in storage after clear call without removeFromStorage flag`() = runTest {
        val storage = FakeStorage()
        val replica = replicaProvider.replica(storage = storage)

        replica.refresh()
        runCurrent()
        replica.clear(removeFromStorage = false)

        val storageData = storage.read()
        assertEquals(ReplicaProvider.TEST_DATA, storageData)
    }

    @Test
    fun `new replica receives stale data from storage`() = runTest {
        val storage = FakeStorage()
        val replica = replicaProvider.replica(storage = storage)
        val data = "data"

        replica.setData(data)
        val newReplica = replicaProvider.replica(
            storage = storage,
            fetcher = {
                delay(DEFAULT_DELAY)
                ReplicaProvider.TEST_DATA
            }
        )
        newReplica.refresh()
        delay(DEFAULT_DELAY / 2) // loading not complete yet

        val newReplicaState = newReplica.currentState
        assertFalse(newReplicaState.hasFreshData)
        assertEquals(data, newReplicaState.data?.value)
    }
}